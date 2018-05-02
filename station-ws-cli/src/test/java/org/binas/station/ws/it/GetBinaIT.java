package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * class that tests Bina retrieval
 */
public class GetBinaIT extends BaseIT {
	private final static int X = 5;
	private final static int Y = 5;
	private final static int CAPACITY = 20;
	private final static int RETURN_PRIZE = 0;
	// static members

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
		client.testClear();
		client.testInit(X, Y, CAPACITY, RETURN_PRIZE);
	}

	@After
	public void tearDown() {
	}

	// main tests
	// assertEquals(expected, actual);

	/** Try to get a Bina , get one verify, one rented (less). */
	@Test
	public void getBinaOneTest() throws NoBinaAvail_Exception, BadInit_Exception {
		client.getBina();

		StationView view = client.getInfo();
		assertNotNull(view);
		assertEquals(CAPACITY - 1, view.getAvailableBinas());
	}
	
	@Test
	public void getBinaAllTest() throws NoBinaAvail_Exception, BadInit_Exception {
		for(int i = 0; i < CAPACITY; i++)
			client.getBina();

		StationView view = client.getInfo();
		assertNotNull(view);
		assertEquals(0, view.getAvailableBinas());
	}
	
	/** Try to get a Bina but no Binas available. */
	@Test(expected = NoBinaAvail_Exception.class)
	public void getBinaNoBinaTest() throws NoBinaAvail_Exception, BadInit_Exception {
		for(int i = 0; i <= CAPACITY; i++)
			client.getBina();
	}

	

}
