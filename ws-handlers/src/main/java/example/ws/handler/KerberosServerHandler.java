package example.ws.handler;

import java.util.Set;
import java.util.Iterator;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;

public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {

	//Server info
	private String serverName = "binas@T01.binas.org";
	private String serverPassword = "psRwi6fM";

	public static final String RESPONSE_PROPERTY = "my.request.property";

    /*
	* Defines the Message Context Property name where the Client's nams
	*/
	private final String TICKET_X_CONTEXT_PROP_NAME = "ticketX";
	/*
	* Defines the Message Context Property name where the Key known by both
	* the Server and the Client is stored.
	*/
	private final String AUTH_X_CONTEXT_PROP_NAME = "authX";

    //
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		kerberosInteraction(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		kerberosInteraction(smc);
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}


	private void kerberosInteraction(SOAPMessageContext smc) {
		System.out.println("Kerberos Server Handler says hello!");

		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (!outbound) {

			//Declaring variables initialized in try blocks
			Key serverKey = null;
			SessionKeyAndTicketView result = null;
			SessionKey sessionKey = null;
			Ticket ticket = null;
			Auth auth = null;

			try {
				serverKey = SecurityHelper.generateKeyFromPassword(serverPassword);
			} catch(NoSuchAlgorithmException nsae) {
				System.out.println("Could not generate the client's key: " + nsae.getMessage());
				return;
			} catch (InvalidKeySpecException ikse) {
				System.out.println("Could not generate the client's key: " + ikse.getMessage());
				return;			
			}
			
			//PHASE 1: Open the ticket and validate it.
			//Decrypts the session key using the server's private key.
			try {
				ticket = new Ticket((new CipherClerk()).cipherFromXMLNode(getNode(smc, "ticket")), serverKey);
			} catch(JAXBException jaxbe) {
				System.out.println("Could not create XML: " + jaxbe.getMessage());
				return;
			} catch(KerbyException ke) {
				System.out.println("Could not create the ticket: " + ke.getMessage());
				return;			
			}

			smc.put(TICKET_X_CONTEXT_PROP_NAME, ticket.getX());
			smc.setScope(TICKET_X_CONTEXT_PROP_NAME, Scope.APPLICATION);

			if (!validateTicket(ticket)) {
				System.out.println("Server --- The ticket was not valid.");
				return;
			}
			System.out.println("Server --- The ticket was valid.");

			Key ticketMasterKey = ticket.getKeyXY();
			
			try {
				auth = new Auth(getNode(smc, "auth"));
				System.out.println(auth.authToString());
			} catch(JAXBException jaxbe) {
				System.out.println("Could not create XML: " + jaxbe.getMessage());
				return;
			}
			if (!validateAuth(auth, ticket)) {
				System.out.println("Server --- The auth was not valid.");
				return;
			}

			smc.put(AUTH_X_CONTEXT_PROP_NAME, auth.getX());
			smc.setScope(AUTH_X_CONTEXT_PROP_NAME, Scope.APPLICATION);

			RequestTime requestTime = new RequestTime(auth.getTimeRequest());
			CipheredView requestCiphered = null;
			try {
				requestCiphered = requestTime.cipher(ticketMasterKey);
			} catch (KerbyException ke) {
				System.out.println("Could not create ciphered request time: " + ke.getMessage());
			}
			smc.put(RESPONSE_PROPERTY, requestCiphered);
			smc.setScope(RESPONSE_PROPERTY, Scope.APPLICATION);

		} else {
			CipheredView requestCiphered = (CipheredView) smc.get(RESPONSE_PROPERTY);
			
			try{

				//Gets the SOAP envelope.
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				//Adds the Header.
				SOAPHeader sh = se.getHeader();
				if (sh == null) { sh = se.addHeader(); }
				//Adds the Header element (name, namespace prefix, namespace). 
				Name name = se.createName("details", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);
				

				//Adds the Header's element value - the server-key-encrypted
				element.appendChild(element.getOwnerDocument().importNode((new CipherClerk()).cipherToXMLNode(requestCiphered, "requestTime").getFirstChild(), true));
				
			} catch(JAXBException jaxbe) {
				System.out.println("Could not create XML: " + jaxbe.getMessage());
				return;
			} catch (SOAPException e) { 
				System.out.println("Cauch SOAPException in KerberosClientHandler."); 
				return;
			}
		}
	}

	private Node getNode(SOAPMessageContext smc, String node) {
		SOAPHeader sh = null;
		//Gets the SOAP envelope.
		try {
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			sh = se.getHeader();
			Node child = sh.getFirstChild();
			System.out.println(child.getTextContent());
			NodeList it = child.getChildNodes();
			if(node == "ticket")
				return it.item(0);
			if(node == "auth")
				return it.item(1);
			return null;
		} catch(SOAPException se) {
			System.out.println("Could not get header from SOAP");
			return null;
		}
	}

	/**
	* Validates the session ticket sent by the client to verify its identity.
	*/
	private boolean validateTicket(Ticket ticket) {
		if (ticket == null) { return false; }

		//Gets the server's name within the ticket.
		String ticketY = ticket.getY();
		//Validates the ticket.
		if (!serverName.equals(ticketY)) {
			System.out.println("The Server name in the Ticket is incorrect.");
			return false;
		}
		System.out.println("The Server name in the Ticket is correct.");
		return true;
	}

	/**
	* Validates the auth sent by the client to verify its identity.
	*/
	private boolean validateAuth(Auth auth, Ticket ticket) {
		if (auth == null) { return false; }

		String ticketClient = ticket.getX();
		String authClient = auth.getX();
		
		if (!authClient.equals(ticketClient)) {
			System.out.println("The Client name in the Auth does not match the Client name in the Session Ticket.");
			return false;
		}
		System.out.println("The Client name in the Auth matches the Client name in the Session Ticket.");
		return true;
	}
}

