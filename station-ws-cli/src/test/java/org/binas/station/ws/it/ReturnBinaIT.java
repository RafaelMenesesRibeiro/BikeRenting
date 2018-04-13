package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite
 */
public class ReturnBinaIT extends BaseIT {
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

	/** Try to return a Bina but no space available. */
	@Test(expected = NoSlotAvail_Exception.class)
	public void returnBinaNoSlotTest() throws NoSlotAvail_Exception, BadInit_Exception {
		client.returnBina();
	}

	@Test
	public void returnBinaOneTest() throws NoSlotAvail_Exception, BadInit_Exception, NoBinaAvail_Exception {
		client.getBina();
		client.returnBina();

		StationView view = client.getInfo();
		assertNotNull(view);

		assertEquals(CAPACITY, view.getAvailableBinas());
	}

}
