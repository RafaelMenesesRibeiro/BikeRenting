package org.binas.domain.it;

import org.binas.exception.UserException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.binas.domain.User;
import org.binas.ws.*;


public class UserConstructorTest {

	@Test
	public void success() {
		try {
			User user = new User("xpto@localhost.com", 10);
			Assert.assertEquals("xpto@localhost.com", user.getEmail());
			Assert.assertEquals(10, user.getCredit());
		} catch (UserException e) {
			//throw new InvalidEmail_Exception(e.getMessage(), new InvalidEmail());
		}
	}

	@Test (expected = InvalidEmail_Exception.class)
	public void emptyEmail() throws InvalidEmail_Exception {
		try {
			new User("", 10);
		} catch ( UserException e ) {
			throw new InvalidEmail_Exception(e.getMessage(), new InvalidEmail());
		}
	}

	@Test (expected = InvalidEmail_Exception.class)
	public void nullEmail() throws InvalidEmail_Exception {
		try {
			new User(null, 10);
		} catch ( UserException e ) {
			throw new InvalidEmail_Exception(e.getMessage(), new InvalidEmail());
		}
	}

	@Test (expected = InvalidEmail_Exception.class)
	public void spacesEmail() throws InvalidEmail_Exception {
		try {
			new User(" @localhost.com", 10);
		} catch ( UserException e ) {
			throw new InvalidEmail_Exception(e.getMessage(), new InvalidEmail());
		}
	}


	@Test (expected = InvalidEmail_Exception.class)
	public void matchesEmail() throws InvalidEmail_Exception {
		try {
			new User("localhost.com", 10);
		} catch ( UserException e ) {
			throw new InvalidEmail_Exception(e.getMessage(), new InvalidEmail());
		}
	}

	@Test (expected = UserException.class)
	public void negCredit() throws UserException {
		try {
			new User("xpto@localhost.com", -1);
		} catch ( UserException u ) {
			throw new UserException(u);
		}
	}


	@After
	public void tearDown() {
	//User.users.clear();
	}
}