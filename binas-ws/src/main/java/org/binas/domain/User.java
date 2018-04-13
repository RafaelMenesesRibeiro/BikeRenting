package org.binas.domain;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.binas.domain.exception.InsufficientCreditsException;
import org.binas.domain.exception.UserAlreadyHasBinaException;
import org.binas.domain.exception.UserHasNoBinaException;

/**
 * 
 * Domain class that represents the User and deals with their creation, balance manipulation, email manipulation, etc.
 * 
 *
 */
public class User {

	private String email;
	private AtomicInteger balance;
	private AtomicBoolean hasBina = new AtomicBoolean(false);
	
	public User(String email, int initialBalance) {
		this.email = email;
		balance = new AtomicInteger(initialBalance);
	}
	
	public synchronized void decrementBalance() throws InsufficientCreditsException{
		 if(balance.get() > 0) {
			 balance.decrementAndGet();
		 } else {
			 throw new InsufficientCreditsException();
		 }
	}

	
	public synchronized void incrementBalance(int amount){
		 balance.getAndAdd(amount);
	}
	
	public String getEmail() {
		return email;
	}
	
	public boolean getHasBina() {
		return hasBina.get();
	}
	

	public int getCredit() {
		return balance.get();
	}

	public synchronized void validateCanRentBina() throws InsufficientCreditsException, UserAlreadyHasBinaException{
		if(getHasBina()) {
			throw new UserAlreadyHasBinaException();
		}
		if(getCredit() <= 0) {
			throw new InsufficientCreditsException();
		}
		
	}
	public synchronized void validateCanReturnBina() throws UserHasNoBinaException {
		if( ! getHasBina()) {
			throw new UserHasNoBinaException();
		}
	}

	public synchronized void effectiveRent() throws InsufficientCreditsException {
		decrementBalance();
		hasBina.set(true);
	}

	public synchronized void effectiveReturn(int prize) throws UserHasNoBinaException {
		if( ! getHasBina()) {
			throw new UserHasNoBinaException();
		}
		hasBina.set(false);
		incrementBalance(prize);
	}


	
}
