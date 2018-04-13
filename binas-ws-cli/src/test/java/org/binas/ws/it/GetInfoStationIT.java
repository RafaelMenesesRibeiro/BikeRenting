package org.binas.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.binas.ws.*;
import org.junit.*;

/*
 * This class should return info about the station
 */
public class GetInfoStationIT extends BaseIT  {
	private final static int X1 = 5;
	private final static int Y1 = 5;
	private final static int X2 = 5;
	private final static int Y2 = 5;
	private final static int X3 = 5;
	private final static int Y3 = 5;
	private final static int CAPACITY = 20;
	private final static int RETURN_PRIZE = 0;
	
	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadInit_Exception {
		binasTestClear();
		client.testInitStation(stationBaseName + "1", X1, Y1, CAPACITY, RETURN_PRIZE);
		client.testInitStation(stationBaseName + "2", X2, Y2, CAPACITY, RETURN_PRIZE);
		client.testInitStation(stationBaseName + "3", X3, Y3, CAPACITY, RETURN_PRIZE);
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

			
			 
	// tests
		
	@Test
    public void getInfoStationSingleValidTest() throws InvalidStation_Exception {
        StationView view = client.getInfoStation(stationBaseName + "1");
	 	
		assertNotNull(view);
		assertEquals(CAPACITY, view.getAvailableBinas());
		assertEquals(CAPACITY, view.getCapacity());
		assertEquals(X1, view.getCoordinate().getX().intValue());
		assertEquals(Y1, view.getCoordinate().getY().intValue());
		assertEquals(0, view.getFreeDocks());
		assertEquals(0, view.getTotalGets());
		assertEquals(0, view.getTotalReturns());
		assertEquals(stationBaseName + "1", view.getId());
    }
	
	@Test
    public void getInfoStationAllValidTest() throws InvalidStation_Exception {
        StationView view1 = client.getInfoStation(stationBaseName + "1");
        StationView view2 = client.getInfoStation(stationBaseName + "2");
        StationView view3 = client.getInfoStation(stationBaseName + "3");
	 	
        assertEquals(X1, view1.getCoordinate().getX().intValue());
		assertEquals(Y1, view1.getCoordinate().getY().intValue());
		assertEquals(X2, view2.getCoordinate().getX().intValue());
		assertEquals(Y2, view2.getCoordinate().getY().intValue());
		assertEquals(X3, view3.getCoordinate().getX().intValue());
		assertEquals(Y3, view3.getCoordinate().getY().intValue());
        
    }
	 
	 
	@Test(expected = InvalidStation_Exception.class)
	public void getInfoStationUnknownTest() throws InvalidStation_Exception {
		client.getInfoStation("Unknown");
	}
	
	@Test(expected = InvalidStation_Exception.class)
	public void getInfoStationNullTest() throws InvalidStation_Exception {
		client.getInfoStation(null);
	}
		 

}
