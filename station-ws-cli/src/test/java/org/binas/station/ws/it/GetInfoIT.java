package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.CoordinatesView;
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
public class GetInfoIT extends BaseIT {
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

	@Test
	public void getInfoValidTest() {
		StationView view = client.getInfo();
		
		assertNotNull(view);
		assertEquals(CAPACITY, view.getAvailableBinas());
		assertEquals(CAPACITY, view.getCapacity());
		assertEquals(X, view.getCoordinate().getX());
		assertEquals(Y, view.getCoordinate().getY());
		assertEquals(0, view.getFreeDocks());
		assertEquals(0, view.getTotalGets());
		assertEquals(0, view.getTotalReturns());
		assertTrue(view.getId().matches("[ATC][0-9X][0-9X]_Station[0-9]"));
	}

	/**
	 * Test to assert that the station correctly stores the number of gets.
	 */
	@Test
	public void getInfoGetBinaTest() throws BadInit_Exception, NoBinaAvail_Exception, NoSlotAvail_Exception {
		client.getBina();

		StationView view = client.getInfo();
		assertNotNull(view);

		assertEquals(CAPACITY - 1, view.getAvailableBinas());
		assertEquals(1, view.getTotalGets());
		assertEquals(1, view.getFreeDocks());
	}

	/**
	 * Test to assert that the station correctly stores the number of binas
	 * returned.
	 */
	@Test
	public void getInfoReturnBinaTest() throws BadInit_Exception, NoBinaAvail_Exception, NoSlotAvail_Exception {
		client.getBina();
		client.returnBina();

		StationView view = client.getInfo();
		assertNotNull(view);

		assertEquals(CAPACITY, view.getAvailableBinas());
		assertEquals(1, view.getTotalGets());
		assertEquals(1, view.getTotalReturns());
		assertEquals(0, view.getFreeDocks());
	}

}
