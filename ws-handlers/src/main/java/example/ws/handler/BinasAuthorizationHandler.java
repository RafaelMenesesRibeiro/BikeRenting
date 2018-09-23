package example.ws.handler;

import java.util.Iterator;
import java.util.Set;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
* This SOAPHandler confirms that the Client as authorization to access to
* the services provided by the Server. It does this by comparing the 
* following fields:
* - X field (client email) in the Ticket sent.
* - X field (client email) in the Auth sent.
* - Email in the SOAP Message with the request.
*
* This is done after the integrity of the message is confirmed (with MACHandler).
*/
public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {
	/*
	* Defines the Message Context Property name where the Client's nams
	*/
	private final String TICKET_X_CONTEXT_PROP_NAME = "ticketX";
	/*
	* Defines the Message Context Property name where the Key known by both
	* the Server and the Client is stored.
	*/
	private final String AUTH_X_CONTEXT_PROP_NAME = "authX";

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
		System.out.println("Auth Handler says hello!");
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (!outboundElement) {
			try {
				String ticketX = (String) smc.get(TICKET_X_CONTEXT_PROP_NAME);
				String authX = (String) smc.get(AUTH_X_CONTEXT_PROP_NAME);

				if (!ticketX.equals(authX)) { 
					System.out.println("BinasAuthorizationHandler: The Client name in the Session Ticket and the Auth didn't match.");
					System.out.println("BinasAuthorizationHandler: Exiting normal message processing.");
					return false; 
				}

				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				SOAPBody sb = msg.getSOAPBody();
				NodeList nodes = sb.getElementsByTagName("email");
				Node node = nodes.item(0);
				String nodeValue = (String) node.getTextContent();

				if (!ticketX.equals(nodeValue)) {
					System.out.println("BinasAuthorizationHandler: The Client name in the Session Ticket and the Request didn't match.");
					System.out.println("BinasAuthorizationHandler: Exiting normal message processing.");
					return false;
				}
				else if (!authX.equals(nodeValue)) {
					System.out.println("BinasAuthorizationHandler: The Client name in the Auth and the Request didn't match.");
					System.out.println("BinasAuthorizationHandler: Exiting normal message processing.");
					return false;	
				}
				return true;
			}
			catch (Exception e) {
				System.out.print("Caught exception in handleMessage: ");
				System.out.println(e);
				System.out.println("Continue normal processing...");
			}
			return false;
		}
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
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
}
