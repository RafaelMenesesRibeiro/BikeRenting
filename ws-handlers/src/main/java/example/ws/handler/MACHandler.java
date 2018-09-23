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

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import javax.crypto.Mac;

/**
* This SOAPHandler veryfies the integrity of SOAP messages.
*
* A header is created in an outbound message with the MAC of the SOAP Body.
* That value is read in the inbound message.
*
* When a message is received, the MAC for the SOAP Body is generated and
* compared with the value in the header.
*/
public class MACHandler implements SOAPHandler<SOAPMessageContext> {
	/*
	* Declares the MAC to generate MAC codes for the messages.
	*/
	private Mac cipher;
	/*
	* Defines the Header name where the MAC for the message is stored.
	*/
	private final String MAC_HEADER_NAME = "MessageAuthenticationCode";
	/*
	* Defines the Message Context Property name where the Key known by both
	* the Server and the Client is stored.
	*/
	private final String KEY_CONTEXT_PROP_NAME = "keyXY";

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
		System.out.println("Mac Handler says hello!");

		try {
			Key macKey = (Key) smc.get(KEY_CONTEXT_PROP_NAME);

			cipher = Mac.getInstance("HmacSHA256");
			cipher.init(macKey);
		}
		catch (NoSuchAlgorithmException nsae) { System.out.println("Caught NoSuchAlgorithmException in MACHandler."); }
		catch (InvalidKeyException ike)  { System.out.println("Caught InvalidKeyException in MACHandler."); }

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		try {
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();

			//Generates a MAC for the message received.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			msg.writeTo(baos);
			String messageString = baos.toString();
			byte[] messageBytes = messageString.getBytes();

			//Calculate MAC.
			byte[] macBytes = cipher.doFinal(messageBytes);
			//Convert MAC to String.
			String macString = new String(macBytes, "UTF-8");

			if (outboundElement) {
				System.out.println("Writing header to OUTbound SOAP message...");

				if (sh == null) { sh = se.addHeader(); }
				
				Name name = se.createName(MAC_HEADER_NAME, "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				element.addTextNode(macString);
			} 
			else {
				System.out.println("Reading header from INbound SOAP message...");

				// get SOAP envelope header
				if (sh == null) { return false; }

				//Gets the Client Generated MAC.
				Name name = se.createName(MAC_HEADER_NAME, "d", "http://demo");
				Iterator<?> it = sh.getChildElements(name);
				if (!it.hasNext()) { return false; }
				SOAPElement element = (SOAPElement) it.next();
				String clientGeneratedMAC = element.getValue();
				
				//Compares both.
				if (clientGeneratedMAC.equals(macString)) { return true; }
			}
		}
		catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}
		return false;
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
