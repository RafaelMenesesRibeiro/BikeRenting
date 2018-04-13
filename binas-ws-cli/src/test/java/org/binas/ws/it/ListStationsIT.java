package org.binas.ws.it;

import java.util.List;

import org.binas.ws.CoordinatesView;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.StationView;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

public class ListStationsIT extends BaseIT {

    @Before
    public void setUp() {
        try {
            client.testInitStation("T01_Station1", 27, 7, 6, 2);
            client.testInitStation("T01_Station2", 80, 20, 12, 1);
            client.testInitStation("T01_Station3", 50, 50, 10, 0);
        } catch (org.binas.ws.BadInit_Exception bie) {

        }
    }

    @Test
    public void success() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(27);
        cv.setY(7);
        List<StationView> svs = client.listStations(new Integer(2), cv);
        Assert.assertEquals(svs.get(0).getId(), "T01_Station1");
        Assert.assertEquals(svs.get(1).getId(), "T01_Station3");

        cv.setX(80);
        cv.setY(20);
        svs = client.listStations(new Integer(1), cv);
        Assert.assertEquals(svs.get(0).getId(), "T01_Station2");

        cv.setX(50);
        cv.setY(50);
        svs = client.listStations(new Integer(3), cv);
        Assert.assertEquals(svs.get(1).getId(), "T01_Station3");
        Assert.assertEquals(svs.get(0).getId(), "T01_Station2");
        Assert.assertEquals(svs.get(0).getId(), "T01_Station1");
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}