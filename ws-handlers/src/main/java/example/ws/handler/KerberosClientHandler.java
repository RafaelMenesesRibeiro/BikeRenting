package example.ws.handler;

import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.Key;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.BadTicketRequest_Exception;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;

public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {	
    
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
		System.out.println("Kerberos Client Handler says hello!");

		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {

			//User and Server info
			String clientName = "alice@T01.binas.org";
			String clientPassword = "grTXx8T";
			String serverName = "binas@T01.binas.org";

			//Declaring variables initialized in try blocks
			KerbyClient client = null;
			Key clientKey = null;
			SessionKeyAndTicketView result = null;
			SessionKey sessionKey = null;
			
			//Creates a KerbiClient to interact with
			try {
				client = new KerbyClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby");
			} catch(KerbyClientException kce) {
				System.out.println("Could not create a kerby client: " + kce.getMessage());
				return;
			}


			//PHASE 1: Authenticate client. receive session key and ticket.
			//Gets the client's private key.
			try {
				clientKey = SecurityHelper.generateKeyFromPassword(clientPassword);
			} catch(NoSuchAlgorithmException nsae) {
				System.out.println("Could not generate the client's key: " + nsae.getMessage());
				return;
			} catch (InvalidKeySpecException ikse) {
				System.out.println("Could not generate the client's key: " + ikse.getMessage());
				return;			
			}
			//Generates a nounce.
			long nounce = (new SecureRandom()).nextLong();
			//Gets the encrypted session key and ticket view.
			try {
				result = client.requestTicket(clientName, serverName, nounce, 30);
			} catch(BadTicketRequest_Exception btre) {
				System.out.println("Could not request the ticket: " + btre.getMessage());
				return;
			}

			//PHASE 2: Open session key (Kcs) with private key (Ks)
			//Gets the encrypted session key.
			CipheredView cipheredSessionKey = result.getSessionKey();
			//Decrypts the session key using the client's private key.
			try {
				sessionKey = new SessionKey(cipheredSessionKey, clientKey);
				if (sessionKey.getNounce() != nounce) {
					System.out.println("Nounces are different!");
					return;
				}
				smc.put("keyXY", sessionKey.getKeyXY());

			} catch(KerbyException ke) {
				System.out.println("Could not create the session key: " + ke.getMessage());
				return;			
			}
			//Gets the encrypted ticket.
			CipheredView cipheredTicket = result.getTicket();

			//Create authenticator to send to server.
			Date timeRequest = new Date();
			Auth auth = new Auth(clientName, timeRequest);

			//PHASE 3: Add the ticket and auth views to SOAP headers
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
				element.appendChild(element.getOwnerDocument().importNode((new CipherClerk()).cipherToXMLNode(cipheredTicket, "ticket").getFirstChild(), true));
				element.appendChild(element.getOwnerDocument().importNode(auth.toXMLNode("auth").getFirstChild(), true));
				
			} catch(JAXBException jaxbe) {
				System.out.println("Could not create XML: " + jaxbe.getMessage());
				return;
			} catch (SOAPException e) { 
				System.out.println("Cauch SOAPException in KerberosClientHandler."); 
				return;
			}
			
			smc.setScope("keyXY", Scope.APPLICATION);
		} else {
			try {
				CipheredView requestCiphered = (new CipherClerk()).cipherFromXMLNode(getNode(smc, "requestTime"));
				RequestTime requestTime = new RequestTime(requestCiphered, (Key) smc.get("keyXY"));
				System.out.println(requestTime.requestTimeToString());
			} catch (JAXBException jaxbe) {
				System.out.println("Could not create XML: " + jaxbe.getMessage());
				return;		
			} catch (KerbyException ke) {
				System.out.println("Could not create the session key: " + ke.getMessage());
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
			if(node == "requestTime")
				return it.item(0);
			return null;
		} catch(SOAPException se) {
			System.out.println("Could not get header from SOAP");
			return null;
		}
	}
}