package org.binas.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jws.WebService;

import org.binas.station.ws.cli.*;
import org.binas.domain.BinasManager;
import org.binas.domain.User;
import org.binas.exception.UserException;
import org.binas.exception.EmailExistsException;
import org.binas.exception.InvalidEmailException;

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

	public StationClient getStation(String stationID) throws UDDINamingException, StationClientException {
		try {
			UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
			String wsURL = uddiNaming.lookup(stationID);
			return new StationClient(wsURL);
		}
		catch (UDDINamingException une) { return null; }
		catch (StationClientException sce) { throw new StationClientException(); }
	}

	public List<StationView> listAllStations() {
		UDDINaming uddiNaming =  this.endpointManager.getUddiNaming();
		ArrayList<UDDIRecord> list = null;
		List<StationView> response = new ArrayList<StationView>();
		try { list = (ArrayList<UDDIRecord>) uddiNaming.listRecords("T01_Station%"); }
		catch (UDDINamingException une) { une.getMessage(); }

		int j = 0;
		for (UDDIRecord uddiRecord : list) {
			try {
				StationClient stationClient = new StationClient(uddiRecord.getUrl());
				StationView view = converter2BinasStationView(stationClient.getInfo());
				response.add(view);
				j++;
			}
			catch (StationClientException sce) { continue; }
		}
		return response;
	}

	@Override
	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
		
		UDDINaming uddiNaming =  this.endpointManager.getUddiNaming();
		ArrayList<UDDIRecord> list = null;
		List<StationView> response = new ArrayList<StationView>();
		try { list = (ArrayList<UDDIRecord>) uddiNaming.listRecords("T01_Station%"); }
		catch (UDDINamingException une) { une.getMessage(); }

		int j = 0;
		for (UDDIRecord uddiRecord : list) {
			try {
				StationClient stationClient = new StationClient(uddiRecord.getUrl());
				StationView view = converter2BinasStationView(stationClient.getInfo());
				response.add(view);
				j++;
				if (j == numberOfStations) { return response; }
			}
			catch (StationClientException sce) { continue; }
		}
		return response;
	}

	@Override
	public StationView getInfoStation(String stationID) throws InvalidStation_Exception {
		try {
			StationClient station = this.getStation(stationID);
			StationView view = converter2BinasStationView(station.getInfo());
			System.out.println(view.getId());
			return view;
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while getting info on StationClient!", new InvalidStation());
		}
		catch (StationClientException sce) { 
			throw new InvalidStation_Exception("Caught StationClientException while getting info on StationClient!", new InvalidStation());
		}
	}

	@Override
	public synchronized int getCredit(String email) throws UserNotExists_Exception {
		try {
			User user = BinasManager.getUser(email);
			return user.getCredit();
		}
		catch (UserException ue) {
			throw new UserNotExists_Exception("Caught UserException while trying to get its credit.", new UserNotExists());
		}
	}

	@Override
	public synchronized UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
		try { new User(email, 10); }
		catch (InvalidEmailException iee) { throw new InvalidEmail_Exception("Invalid email", new InvalidEmail()); }
		catch (EmailExistsException eee) { throw new EmailExists_Exception("Email exists", new EmailExists()); }
		catch (UserException ue) {  }
		return null;
	}

	@Override
	public synchronized void rentBina(String stationID, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		try {
			User user = BinasManager.getUser(email);
			if (user.getHasBike()) { throw new AlreadyHasBina_Exception("User already has a bike.", new AlreadyHasBina()); }
			if (user.getCredit() < 1) { throw new NoCredit_Exception("User doesn't have enough credit to rent a bike.", new NoCredit()); }

			StationClient station = this.getStation(stationID);
			station.getBina();

			user.setCredit(user.getCredit() - 1);
			user.setHasBike(true);
		}
		catch (StationClientException sce) {
			throw new InvalidStation_Exception("Caught StationClientException while trying to rent a Bina.", new InvalidStation());
		}
		catch (org.binas.station.ws.NoBinaAvail_Exception nbae) {
			throw new NoBinaAvail_Exception("Caught NoBinaAvail_Exception while trying to rent a Bina on stationClient", new NoBinaAvail());
		}
		catch (UserException ue) {
			throw new UserNotExists_Exception("Caught UserException while trying to rent a Bina.", new UserNotExists());
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while doing lookup() on stationClient!", new InvalidStation());
		}
	}

	@Override
	public synchronized void returnBina(String stationID, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		try {
			User user = BinasManager.getUser(email);
			if (!user.getHasBike()) { throw new NoBinaRented_Exception("User doesn't have a bike to return.", new NoBinaRented()); }
			
			StationClient station = this.getStation(stationID);
			int bonus = station.returnBina();

			user.setHasBike(false);
			user.setCredit(user.getCredit() + bonus);
		}
		catch (StationClientException sce) {
			throw new InvalidStation_Exception("Caught StationClientException while trying to return a Bina.", new InvalidStation());
		}
		catch (org.binas.station.ws.NoSlotAvail_Exception nsae) {
			throw new FullStation_Exception("Tried to return bike in a station with no free slots", new FullStation());
		}
		catch (UserException ue) {
			throw new UserNotExists_Exception("Caught UserException while trying to return a Bina.", new UserNotExists());
		}
		catch (UDDINamingException une) {
			throw new InvalidStation_Exception("Caught UDDINamingException while doing lookup() on stationClient!", new InvalidStation());
		}
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
		/*
		try {
			List<StationView> stations = this.listAllStations();
			for (StationView view : stations) {
				String id = view.getId();
				StationClient station = this.getStation(id);
				station.testClear();	
			}
		}
		catch (UDDINamingException une) { System.out.println(une.getMessage()); }
		catch (StationClientException sce) { System.out.println(sce.getMessage()); }
		*/
	}

	@Override
	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception {

	}

	@Override
	public void testInit(int userInitialPoints) throws BadInit_Exception {

    }
    
    public StationView converter2BinasStationView(org.binas.station.ws.StationView stationView) {
        StationView binasStationView = new StationView();
        binasStationView.setId(stationView.getId());
        binasStationView.setCoordinate(converter2BinasCoordinatesView(stationView.getCoordinate()));
        binasStationView.setCapacity(stationView.getCapacity());
        binasStationView.setTotalGets(stationView.getTotalGets());
        binasStationView.setTotalReturns(stationView.getTotalReturns());
        binasStationView.setAvailableBinas(stationView.getAvailableBinas());
        binasStationView.setFreeDocks(stationView.getFreeDocks());
        return binasStationView;
    }

    public CoordinatesView converter2BinasCoordinatesView(org.binas.station.ws.CoordinatesView coordinatesView) {
        CoordinatesView binasCoordinatesView = new CoordinatesView();
        binasCoordinatesView.setX(coordinatesView.getX());
        binasCoordinatesView.setY(coordinatesView.getY());
        return binasCoordinatesView;
    }
}
