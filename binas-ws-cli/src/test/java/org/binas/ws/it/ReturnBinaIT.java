package org.binas.ws.it;

import static org.junit.Assert.assertEquals;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.FullStation_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoBinaRented_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class that exercises the return of a Bina 
 *
 */
public class ReturnBinaIT extends BaseIT {
	
	private static final int USER_POINTS = 10;
	private static final int STATION_REWARD = 10;
	private static final String STATION_1 = stationBaseName + "1";

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadInit_Exception {
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
		client.testInitStation(STATION_1, /*x*/5, /*y*/5, /*capacity*/20, /*reward*/STATION_REWARD);
		client.activateUser(VALID_USER);
	}

	@After
	public void tearDown() {
	}


	// tests
		
	@Test
	public void returnBinaOkTest() throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
		client.rentBina(STATION_1, VALID_USER);
		client.returnBina(STATION_1, VALID_USER);
		int credit = client.getCredit(VALID_USER);
		assertEquals(USER_POINTS - 1 + STATION_REWARD, credit);
	}
		 
		 

	@Test(expected = NoBinaRented_Exception.class)
	public void returnBinaNoBinaRentedTest() throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception, AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception   {
		final String USER_2 = "sd.teste2@tecnico.ulisboa";
		client.activateUser(USER_2);
		
		/* Rent with another user first to avoid FullStation Exception */
		client.rentBina(STATION_1, USER_2);
		client.returnBina(STATION_1, VALID_USER);
	}
		 
		 

	@Test(expected = UserNotExists_Exception.class)
	public void returnBinaUserNotExistsTest() throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception, AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception   {
		final String USER_2 = "sd.teste2@tecnico.ulisboa";
		final String USER_3 = "sd.teste3@tecnico.ulisboa";
		
		client.activateUser(USER_2);
		
		/* Rent with another user first to avoid FullStation Exception */
		client.rentBina(STATION_1, USER_2);
						 	 
		client.returnBina(STATION_1, USER_3);
	}
		 
		 	 


	@Test(expected = FullStation_Exception.class)
	public void returnBinaNoSlotAvailTest() throws BadInit_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
		final String STATION_2 = stationBaseName + "2";
		client.testInitStation(STATION_2, /*x*/5, /*y*/5, /*capacity*/20, /*reward*/STATION_REWARD);
			         
		client.rentBina(STATION_1, VALID_USER);				 	 
		client.returnBina(STATION_2, VALID_USER);
		 	
	 }

}
