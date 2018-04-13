package org.binas.ws.it;

import static org.junit.Assert.*;

import org.binas.ws.*;
import org.junit.*;

/*
 * Class tests if the user creation has succeeded or not
 */
public class ActivateUserIT extends BaseIT  {
	private static final int USER_POINTS = 10;

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadInit_Exception {
		binasTestClear();
		client.testInit(USER_POINTS);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void createUserValidTest() throws EmailExists_Exception, InvalidEmail_Exception, UserNotExists_Exception{
		client.activateUser(VALID_USER);
		assertEquals(USER_POINTS, client.getCredit(VALID_USER));
	}
		 
	@Test
	public void createUserValidTest2() throws EmailExists_Exception, InvalidEmail_Exception, UserNotExists_Exception {
		String email = new String("sd.teste@tecnico");
		client.activateUser(email);
		assertEquals(USER_POINTS, client.getCredit(email));
	}
	
	@Test
	public void createUserValidTest3() throws EmailExists_Exception, InvalidEmail_Exception, UserNotExists_Exception {
		String email = new String("sd@tecnico");
		client.activateUser(email);
		assertEquals(USER_POINTS, client.getCredit(email));
	}
	 
	 
	/*
	 * Tries to create to users with the same name, which should throw an exception
	 */
	@Test(expected = EmailExists_Exception.class)
	public void createUserDuplicateTest() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser(VALID_USER);
		client.activateUser(VALID_USER);
	}

	/*
	 * Tries to create a user with an invalid email
	 * Should throw InvalidEmail_Exception
	 */
	@Test(expected = InvalidEmail_Exception.class)
	public void createUserInvalidEmailTest1() throws EmailExists_Exception, InvalidEmail_Exception {
		String email = new String("@tecnico.ulisboa");
		client.activateUser(email);			
	}
	 
	/*
	 * Tries to create a user with an invalid email
	 * Should throw InvalidEmail_Exception
	 */
	@Test(expected = InvalidEmail_Exception.class)
	public void createUserInvalidEmailTest2() throws EmailExists_Exception, InvalidEmail_Exception {
		String email = new String("teste");
		client.activateUser(email);			
	}
	 

	/*
	 * Tries to create a user with an invalid email
	 * Should throw InvalidEmail_Exception
	 */
	@Test(expected = InvalidEmail_Exception.class)
	public void createUserInvalidEmailTest3() throws EmailExists_Exception, InvalidEmail_Exception {
		String email = new String("teste@tecnico.");
		client.activateUser(email);			
	}
	 
	
	/*
	 * Tries to create a user with an invalid email
	 * Should throw InvalidEmail_Exception
	 */
	@Test(expected = InvalidEmail_Exception.class)
	public void createUserInvalidEmailTest4() throws EmailExists_Exception, InvalidEmail_Exception {
		String email = new String("sd.@tecnico");
		client.activateUser(email);			
	}
	
	@Test(expected = InvalidEmail_Exception.class)
	public void createUserInvalidEmailTest5() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser(null);			
	}
	 	 
	 
}
