package org.binas.ws.it;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

/**
 * Class that tests Ping operation
 */
public class GetCreditIT extends BaseIT {

    @Before
    public void setUp() {
        try {
            client.testInitStation("T01_Station1", 27, 7, 6, 2);
            client.testInitStation("T01_Station2", 80, 20, 12, 1);
            client.testInitStation("T01_Station3", 50, 50, 10, 0);
            client.testInit(10);
        } catch (org.binas.ws.BadInit_Exception bie) {

        }
    }

    @Test
    public void success() {
        try {
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }
        try {
            Assert.assertEquals(client.getCredit("email@example.com"), 10);
        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void fail() {
        try {
            Assert.assertEquals(client.getCredit("email@example.com"), 10);
            Assert.fail();
        } catch (UserNotExists_Exception unee) {
        }
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}