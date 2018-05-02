package org.binas.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


// List Station is a class that tests the ListStation operation that should deliver stations in an order from the nearest one to the farthest one
public class ListStationsIT extends BaseIT {
	private final static int X1 = 1;
	private final static int Y1 = 1;
	private final static int X2 = 2;
	private final static int Y2 = 2;
	private final static int X3 = 3;
	private final static int Y3 = 3;
	private final static int CAPACITY = 20;
	private final static int RETURN_PRIZE = 0;
	private static CoordinatesView testCoords;
	
	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadInit_Exception {
		binasTestClear();
		client.testInitStation(stationBaseName + "1", X1, Y1, CAPACITY, RETURN_PRIZE);
		client.testInitStation(stationBaseName + "2", X2, Y2, CAPACITY, RETURN_PRIZE);
		client.testInitStation(stationBaseName + "3", X3, Y3, CAPACITY, RETURN_PRIZE); 
		testCoords = new CoordinatesView();
		testCoords.setX(0);
		testCoords.setY(0);
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
		
		 
	@Test
	public void listStationsSingleTest() {
		List<StationView> result = client.listStations(/* number of stations*/ 1, testCoords);
		StationView view = result.get(0);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(stationBaseName + "1", view.getId());
	}
	
	@Test
	public void listStationsAllTest() {
		List<StationView> result = client.listStations(/* number of stations*/ 3, testCoords);
		StationView view1 = result.get(0);
		StationView view2 = result.get(1);
		StationView view3 = result.get(2);
		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals(stationBaseName + "1", view1.getId());
		assertEquals(stationBaseName + "2", view2.getId());
		assertEquals(stationBaseName + "3", view3.getId());
	}
	
	@Test
	public void listStationsNullTest() {
		List<StationView> result = client.listStations(/* number of stations*/ 1, null);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void listStationsZeroTest() {
		List<StationView> result = client.listStations(/* number of stations*/ 0 , testCoords);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	 
	@Test
	public void listStationsFourTest() {
		List<StationView> result = client.listStations(/* number of stations*/ 4, testCoords);
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	 
}
 
