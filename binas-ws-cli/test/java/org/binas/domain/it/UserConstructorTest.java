package org.binas.domain.it;

import org.binas.exception.UserException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.binas.domain.User;

public class UserConstructorTest {

@Test
public void success() {
User user = new User("xpto@localhost.com", 10);
	Assert.assertEquals("xpto@localhost.com", user.getEmail());
	Assert.assertEquals(20, user.getCredit());
}

@Test (expected = InvalidEmail_Exception.class)
public void emptyEmail() throws InvalidEmail_Exception {
	try {
		new User("", 10);
	} catch ( UserException e ) {
		throw new InvalidEmail_Exception(e);
	}
}

@Test (expected = InvalidEmail_Exception.class)
public void nullEmail() throws InvalidEmail_Exception {
	try {
		new User(null, 10);
	} catch ( UserException e ) {
		throw new InvalidEmail_Exception(e);
	}
}

@Test (expected = InvalidEmail_Exception.class)
public void spacesEmail() throws InvalidEmail_Exception {
	try {
		new User(" @localhost.com", 10);
	} catch ( UserException e ) {
		throw new InvalidEmail_Exception(e.getMessage());
	}
}


@Test (expected = InvalidEmail_Exception.class)
public void matchesEmail() throws InvalidEmail_Exception {
	try {
		new User("localhost.com", 10);
	} catch ( UserException e ) {
		throw new InvalidEmail_Exception(e.getMessage());
	}
}


@After
public void tearDown() {
	//User.users.clear();
}