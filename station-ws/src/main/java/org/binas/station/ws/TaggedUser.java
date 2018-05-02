package org.binas.station.ws;
	
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Class TaggedUser that represents user in station-ws.
 *
 */
public class TaggedUser {
	private final String email;
	private AtomicInteger balance;
	private Tag tag;

	/** Create taggedUser with arguments given. */
	public TaggedUser(String email, int balance, int seq, int cid) {
		this.email = email;
		this.balance = new AtomicInteger(balance);
		this.tag = new Tag(seq, cid);
	}
	/** Retrieve email of user. */
	public String getEmail() { return this.email; }
	/** Retrieve balance of user. */
	public int getBalance() { return this.balance.get(); }
	/** Set balance of user. */
	public void setBalance(int balance) { this.balance = new AtomicInteger(balance); }
	/** Retrieve tag of user. */
	public Tag getTag() { return this.tag; }
	/** Set tag of user. */
	public void setTag(int seq, int cid) { this.tag.setNewValues(seq, cid); }
}
