package org.binas.ws.it;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.StationView;
import org.binas.ws.UserNotExists_Exception;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

public class RentBinaIT extends BaseIT {

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
            client.rentBina("T01_Station1", "email@example.com");
            StationView sv = client.getInfoStation("T01_Station1");
            Assert.assertEquals(5, sv.getAvailableBinas());
            Assert.assertEquals(1, sv.getFreeDocks());
            Assert.assertEquals(1, sv.getTotalGets());        
        } catch(AlreadyHasBina_Exception ahbe) {
            Assert.fail();
        } catch(InvalidStation_Exception ise) {
            Assert.fail();
        } catch(NoBinaAvail_Exception nbae) {
            Assert.fail();
        } catch(NoCredit_Exception nce) {
            Assert.fail();
        } catch(UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void alreadyHasBina() {
        try {
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }

        try {
            client.rentBina("T01_Station1", "email@example.com");
            client.rentBina("T01_Station1", "email@example.com");
            Assert.fail();
        } catch (AlreadyHasBina_Exception ahbe) {

        } catch (InvalidStation_Exception ise) {
            Assert.fail();
        } catch (NoBinaAvail_Exception nbae) {
            Assert.fail();
        } catch (NoCredit_Exception nce) {
            Assert.fail();
        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void invalidStation() {
        try {
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }

        try {
            client.rentBina("T01_Station1", "email@example.com");
            Assert.fail();
        } catch (AlreadyHasBina_Exception ahbe) {
            Assert.fail();
        } catch (InvalidStation_Exception ise) {

        } catch (NoBinaAvail_Exception nbae) {
            Assert.fail();
        } catch (NoCredit_Exception nce) {
            Assert.fail();
        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void noBinaAvail() {
        try {
            client.activateUser("email1@example.com");
            client.activateUser("email2@example.com");
            client.activateUser("email3@example.com");
            client.activateUser("email4@example.com");
            client.activateUser("email5@example.com");
            client.activateUser("email6@example.com");
            client.activateUser("email7@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }

        try {
            client.rentBina("T01_Station1", "email1@example.com");
            client.rentBina("T01_Station1", "email2@example.com");
            client.rentBina("T01_Station1", "email3@example.com");
            client.rentBina("T01_Station1", "email4@example.com");
            client.rentBina("T01_Station1", "email5@example.com");
            client.rentBina("T01_Station1", "email6@example.com");
            client.rentBina("T01_Station1", "email7@example.com");
            Assert.fail();
        } catch (AlreadyHasBina_Exception ahbe) {
            Assert.fail();
        } catch (InvalidStation_Exception ise) {
            Assert.fail();
        } catch (NoBinaAvail_Exception nbae) {
            
        } catch (NoCredit_Exception nce) {
            Assert.fail();
        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void noCredit() {
        try {
            client.testInit(0);
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        } catch (BadInit_Exception bie) {
            
        }

        try {
            client.rentBina("T01_Station1", "email@example.com");
            Assert.fail();
        } catch (AlreadyHasBina_Exception ahbe) {
            Assert.fail();
        } catch (InvalidStation_Exception ise) {
            Assert.fail();
        } catch (NoBinaAvail_Exception nbae) {
            Assert.fail();
        } catch (NoCredit_Exception nce) {

        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @Test
    public void noUser() {
        try {
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }

        try {
            client.rentBina("T01_Station1", "emai2l@example.com");
            Assert.fail();
        } catch (AlreadyHasBina_Exception ahbe) {
            Assert.fail();
        } catch (InvalidStation_Exception ise) {
            Assert.fail();
        } catch (NoBinaAvail_Exception nbae) {
            Assert.fail();
        } catch (NoCredit_Exception nce) {
            Assert.fail();
        } catch (UserNotExists_Exception unee) {
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}