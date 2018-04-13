package org.binas.ws.it;

import static org.junit.Assert.assertEquals;

import org.binas.ws.*;
import org.junit.*;
import org.junit.Test;


/*
 * Class that tests renting a Bina
 */
public class RentBinaIT extends BaseIT {
	private static final int USER_POINTS = 10;
	private static final String STATION_1 = stationBaseName + "1";
	
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
	public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception {
		binasTestClear();
		client.testInit(USER_POINTS);
		client.testInitStation(STATION_1, /*x*/5, /*y*/5, /*capacity*/20, /*reward*/0);
		client.activateUser(VALID_USER);
	}

	@After
	public void tearDown() {
	}
	 
	// tests
		
	/*
	 * Valid user rents Bina
	 * Should not raise any Exception
	 */
	@Test
	public void rentBinaValidTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {			         
	 	client.rentBina(STATION_1, VALID_USER);
	 	assertEquals(USER_POINTS - 1, client.getCredit(VALID_USER));
    }
	 
	 
	/*
	 * Class that exercises the fact that a User cannot rent a new Bina if he already has one
	 */
	@Test(expected = AlreadyHasBina_Exception.class)
	public void rentBinaAlreadyHasBinaTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {         
	 	client.rentBina(STATION_1, VALID_USER);
	 	client.rentBina(STATION_1, VALID_USER);
	 	
    }
	 
	 

	/*
	 * User tries to rent a Bina from an InvalidStation
	 */
	@Test(expected = InvalidStation_Exception.class)
	public void rentBinaInvalidStationTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {	         
	 	client.rentBina("Invalid Station", VALID_USER);
    }
	
	/*
	 * User tries to rent a Bina from an InvalidStation
	 */
	@Test(expected = InvalidStation_Exception.class)
	public void rentBinaNullStationTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {	         
	 	client.rentBina(null, VALID_USER);
    }
 
	 	 

	/*
	 * User tries to rent Bina but there is no Bina available in the station
	 * Expected to throw exception
	 */
	@Test(expected = NoBinaAvail_Exception.class)
	public void rentBinaNoBinaAvailTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {
		client.testInitStation(stationBaseName + "1", /*x*/5, /*y*/5, /*capacity*/0, /*reward*/10);
		client.rentBina(STATION_1, VALID_USER);	 	
    }
	 
	 	 		 
	/*
	 * User tries to rent a Bina but has no credit
	 */
	@Test(expected = NoCredit_Exception.class)
	public void rentBinaNoCreditTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception  {
		binasTestClear();
		client.testInit(0);
		client.activateUser(VALID_USER);
	 	client.rentBina(STATION_1, VALID_USER);
	 	
    } 		 		 
		 
}
