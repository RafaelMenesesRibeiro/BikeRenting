package org.binas.station.ws;
	
import java.util.concurrent.atomic.AtomicInteger;

public class TaggedUser {
	private final String email;
	private AtomicInteger balance;
	private int tag;

	public TaggedUser(String email, int balance, int tag) {
		this.email = email;
		this.balance = new AtomicInteger(balance);
		this.tag = tag;
	}

	public String getEmail() { return this.email; }
	public int getBalance() { return this.balance.get(); }
	public void setBalance(int balance) { this.balance = new AtomicInteger(balance); }
	public int getTag() { return this.tag; }
	public void setTag(int newTag) { this.tag = newTag; }
}
