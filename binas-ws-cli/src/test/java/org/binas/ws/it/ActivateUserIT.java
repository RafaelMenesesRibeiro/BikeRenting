package org.binas.ws.it;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;


public class ActivateUserIT extends BaseIT {

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
        try {
            client.activateUser("email@example.com");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }
    }

    @Test
    public void successTwo() {
        try {
            client.activateUser(" gemail@example.com ");
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }
    }

    @Test
    public void duplicateEmail() {
        try {
            client.activateUser("email@example.com");
            client.activateUser("email@example.com");
            Assert.fail();
        } catch (EmailExists_Exception eee) {

        } catch (InvalidEmail_Exception iee) {
            Assert.fail();
        }
    }

    @Test
    public void wrongEmailOne() {
        try {
            client.activateUser("email");
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    @Test
    public void wrongEmailTwo() {
        try {
            client.activateUser("email@");
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    // @Test
    // public void wrongEmailThree() {
    //     try {
    //         client.activateUser("email@sad.");
    //         Assert.fail();
    //     } catch (EmailExists_Exception eee) {
    //         Assert.fail();
    //     } catch (InvalidEmail_Exception iee) {
    //     }
    // }

    // @Test
    // public void wrongEmailFour() {
    //     try {
    //         client.activateUser(".@sad.");
    //         Assert.fail();
    //     } catch (EmailExists_Exception eee) {
    //         Assert.fail();
    //     } catch (InvalidEmail_Exception iee) {
    //     }
    // }

    @Test
    public void wrongEmailFive() {
        try {
            client.activateUser(".@.");
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    @Test
    public void wrongEmailSix() {
        try {
            client.activateUser("email @example.com");
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    @Test
    public void wrongEmailSeven() {
        try {
            client.activateUser(" ");
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    @Test
    public void wrongEmailEight() {
        try {
            client.activateUser(null);
            Assert.fail();
        } catch (EmailExists_Exception eee) {
            Assert.fail();
        } catch (InvalidEmail_Exception iee) {
        }
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}