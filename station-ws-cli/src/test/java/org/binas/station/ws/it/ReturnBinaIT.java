package org.binas.station.ws.it;

import org.binas.station.ws.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.After;

/**
 * Class that tests Ping operation
 */
public class ReturnBinaIT extends BaseIT {

    @Before
    public void setUp() {
        try {
            client.testInit(27, 7, 6, 2);
            client.getBina();
        } catch (BadInit_Exception bie){

        } catch(NoBinaAvail_Exception e) {

        }
    }

    @Test
    public void sucess() {
        try {
            client.returnBina();
        } catch(NoSlotAvail_Exception e) {

        }
        StationView sv = client.getInfo();
        Assert.assertEquals(6, sv.getAvailableBinas());
        Assert.assertEquals(0, sv.getFreeDocks());
        Assert.assertEquals(1, sv.getTotalGets());
        Assert.assertEquals(1, sv.getTotalReturns());
    }

    @After
    public void tearDown(){
        client.testClear();
    }
}
