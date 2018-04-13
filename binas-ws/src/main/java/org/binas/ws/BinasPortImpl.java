package org.binas.ws;

import java.util.List;

import javax.jws.WebService;

import org.binas.station.ws.cli.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;



/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
@WebService(endpointInterface = "org.binas.ws.BinasPortType", wsdlLocation = "BinasWebService.wsdl", name = "BinasWebService", portName = "BinasPort", targetNamespace = "http://ws.binas.org/", serviceName = "BinasService")
public class BinasPortImpl implements BinasPortType {

    /**
     * The Endpoint manager controls the Web Service instance during its whole
     * lifecycle.
     */
    private BinasEndpointManager endpointManager;

    /** Constructor receives a reference to the endpoint manager. */
    public BinasPortImpl(BinasEndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    @Override
    public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
        return null;
    }

    @Override
    public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
        return null;
    }

    @Override
    public int getCredit(String email) throws UserNotExists_Exception {
        return 0;
    }

    @Override
    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
        return null;
    }

    @Override
    public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
        
    }

    @Override
    public void returnBina(String stationId, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {

    }

    @Override
    public String testPing(String inputMessage) {
       
        UDDINaming uddiNaming =  this.endpointManager.getUddiNaming();

        String baseName = "T01_Station";
        int i = 1;
        String wsURL = null;
        String newName = null;
        while (true) {
            try {
                newName = baseName + Integer.toString(i);
                wsURL = uddiNaming.lookup(newName);
            } catch (UDDINamingException une) {
                System.out.println(une);
                break;
            } try {
                StationClient stationClient = new StationClient(wsURL);
                System.out.println(stationClient.testPing(newName));
                i++;
            }
            catch (StationClientException sce) {
                System.out.println(sce);
            }
        }

        return null;
    }

    @Override
    public void testClear() {

    }

    @Override
    public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception {

    }

    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception {

    }
}
