package org.binas.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jws.WebService;

import org.binas.station.ws.cli.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;



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
		/*
		UDDINaming uddiNaming =  this.endpointManager.getUddiNaming();
		ArrayList<UDDIRecord> list = null;
		List<StationView> response = new ArrayList<StationView>();
		try {
			list = (ArrayList<UDDIRecord>) uddiNaming.listRecords("T01_Station%");
		}
		catch (UDDINamingException une) {  }

		for (UDDIRecord uddiRecord : list) {
			StationClient stationClient = null;
			try {
				stationClient = new StationClient(uddiRecord.getUrl());
				StationView view = (StationView) stationClient.getInfo();
				response.add(view);
			}
			catch (StationClientException sce) {
				continue;
			}
		}
		return response;
		*/
		return null;
	}

	@Override
	public StationView getInfoStation(String stationID) throws InvalidStation_Exception {
		/*
		try {
			StationClient station = this.getStation(stationID);
			StationView view = station.getInfo();
			return view;
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while doing lookup() on StationClient!", new InvalidStation());
		}
		*/
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
	public void rentBina(String stationID, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		/*
		try {
			//TODO: Chck if Client exists.
			//throw new UserNotExists_Exception();
			//TODO: Check if Client already has bike.
			//throw new AlreadyHasBina_Exception();
			//TODO: Check if Client has credit.
			//throw new NoCredit_Exception();
			StationClient station = this.getStation(stationID);
			station.getBina();
			//TODO: Client now has bike.
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while doing lookup() on stationClient!", new InvalidStation());
		}
		catch (NoBinaAvail_Exception nbae) {
			throw new NoBinaAvail_Exception("Caught NoBinaAvail_Exception while trying to rent a Bina on stationClient", new NoBinaAvail());
			//TODO.
		}
		*/
	}

	@Override
	public void returnBina(String stationID, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		/*
		try {
			//TODO: Chck if Client exists.
			//throw new UserNotExists_Exception();
			//TODO: Check if Client already has bike.
			//throw new NoBinaRented_Exception();
			StationClient station = this.getStation(stationID);
			int bonus = station.returnBina();
			if (bonus == -1) {
				throw new FullStation_Exception("Caught FullStation_Exception while trying to return Bina to stationClient", new FullStation());
			}
			else {
				//TODO: Add bonus to Client's credit.
			}
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while doing lookup() on StationClient!", new InvalidStation());
		}
		*/
	}

	@Override
	public String testPing(String inputMessage) {
	   
		String out = "";
		UDDINaming uddiNaming =  this.endpointManager.getUddiNaming();
		ArrayList<UDDIRecord> col = null;
		try{
			col = (ArrayList<UDDIRecord>) uddiNaming.listRecords("T01_Station%");
		} catch (UDDINamingException une) {

		}

		for (UDDIRecord uddiRecord : col) {
			StationClient stationClient = null;
			try {
				stationClient = new StationClient(uddiRecord.getUrl());
				out += stationClient.testPing("HELLOLLL") + '\n';
			} catch (StationClientException sce) {
				continue;
			}
		}
		return out;
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
