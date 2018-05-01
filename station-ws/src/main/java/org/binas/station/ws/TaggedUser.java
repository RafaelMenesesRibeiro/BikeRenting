package org.binas.station.ws;
	
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Class TaggedUser that represents user in station-ws.
 *
 */
public class TaggedUser {
	private final String email;
	private AtomicInteger balance;
	private int tag;

	/** Create taggedUser with arguments given. */
	public TaggedUser(String email, int balance, int tag) {
		this.email = email;
		this.balance = new AtomicInteger(balance);
		this.tag = tag;
	}
	/** Retrieve email of user. */
	public String getEmail() { return this.email; }
	/** Retrieve balance of user. */
	public int getBalance() { return this.balance.get(); }
	/** Set balance of user. */
	public void setBalance(int balance) { this.balance = new AtomicInteger(balance); }
	/** Retrieve tag of user. */
	public int getTag() { return this.tag; }
	/** Set tag of user. */
	public void setTag(int newTag) { this.tag = newTag; }
}
