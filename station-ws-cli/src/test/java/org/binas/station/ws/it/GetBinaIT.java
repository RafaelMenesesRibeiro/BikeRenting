package org.binas.station.ws.it;

import org.binas.station.ws.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

/**
 * Class that tests Ping operation
 */
public class GetBinaIT extends BaseIT {

	@Before
	public void setUp() {
		try {
			client.testInit(27, 7, 6, 2);
		} catch (BadInit_Exception bie){

		}
	}

	@Test
	public void sucess() {
		try {
			client.getBina();
		} catch(NoBinaAvail_Exception e) {

		}
		StationView sv = client.getInfo();
		Assert.assertEquals(5, sv.getAvailableBinas());
		Assert.assertEquals(1, sv.getFreeDocks());
		Assert.assertEquals(1, sv.getTotalGets());
	}
}
