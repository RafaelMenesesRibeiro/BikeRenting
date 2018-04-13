package org.binas.ws;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;

import org.binas.domain.BinasManager;
import org.binas.domain.StationsComparator;
import org.binas.domain.User;
import org.binas.domain.UsersManager;
import org.binas.domain.exception.BadInitException;
import org.binas.domain.exception.InsufficientCreditsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.StationNotFoundException;
import org.binas.domain.exception.UserAlreadyExistsException;
import org.binas.domain.exception.UserAlreadyHasBinaException;
import org.binas.domain.exception.UserHasNoBinaException;
import org.binas.domain.exception.UserNotFoundException;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;

import org.binas.station.ws.cli.StationClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

@WebService(
		endpointInterface = "org.binas.ws.BinasPortType",
        wsdlLocation = "binas.wsdl",
        name ="BinasWebService",
        portName = "BinasPort",
        targetNamespace="http://ws.binas.org/",
        serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType {
	
	// end point manager
	private BinasEndpointManager endpointManager;

	public BinasPortImpl(BinasEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	@Override
	public UserView activateUser(String email) throws InvalidEmail_Exception, EmailExists_Exception {
		try {
			User user = BinasManager.getInstance().createUser(email);
			
			//Create and populate userView
			UserView userView = new UserView();
			userView.setEmail(user.getEmail());
			userView.setCredit(user.getCredit());
			userView.setHasBina(user.getHasBina());
			return userView;
		} catch (UserAlreadyExistsException e) {
			throwEmailExists("Email already exists: " + email);
		} catch (InvalidEmailException e) {
			throwInvalidEmail("Invalid email: " + email);
		}
		return null;
	}

	@Override
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		if(stationId == null || stationId.trim().isEmpty())
			throwInvalidStation("Station IDs can not be empty!");
		
		StationClient stationCli;
		try {
			stationCli = BinasManager.getInstance().getStation(stationId);
			return newStationView(stationCli.getInfo());
		} catch (StationNotFoundException e) {
			throwInvalidStation("No Station found with ID: " + stationId);
			return null;
		}
		
	}

	@Override
	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
		List<StationView> stationViews = new ArrayList<StationView>();
		Collection<String> stations = BinasManager.getInstance().getStations();
		String uddiUrl = BinasManager.getInstance().getUddiURL();
		StationClient sc = null;
		org.binas.station.ws.StationView sv = null;
		
		if(numberOfStations <= 0 || coordinates == null)
			return stationViews;
		
		for (String s : stations) {
			try {
				sc = new StationClient(uddiUrl, s);
				sv = sc.getInfo();
				stationViews.add(newStationView(sv));
			} catch(StationClientException e) {
				continue;
			}
		}
		Collections.sort(stationViews, new StationsComparator(coordinates));
		
		if(numberOfStations > stationViews.size())
			return stationViews;
		else
			return stationViews.subList(0, numberOfStations);
	}

	@Override
	public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception,
			NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		
		try {
			BinasManager.getInstance().rentBina(stationId,email);
		} catch (UserNotFoundException e) {
			throwUserNotExists("User not found: " + email);
		} catch (InsufficientCreditsException e) {
			throwNoCredit("User has insufficient credits: " + email);
		} catch (UserAlreadyHasBinaException e) {
			throwAlreadyHasBina("User already has bina: " + email);
		} catch (StationNotFoundException e) {
			throwInvalidStation("Station not found: " + stationId);
		} catch (org.binas.station.ws.NoBinaAvail_Exception e) {
			throwNoBinaAvail("Station has no Binas available: " + stationId);
		}
	}


	@Override
	public void returnBina(String stationId, String email)
			throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		try {
			BinasManager.getInstance().returnBina(stationId,email);
		} catch (UserNotFoundException e) {
			throwUserNotExists("User not found: " + email);
		} catch (NoSlotAvail_Exception e) {
			throwFullStation("Station has NO docks available: " + stationId);
		} catch (UserHasNoBinaException e) {
			throwNoBinaRented("User has NO bina: " + email);
		} catch (StationNotFoundException e) {
			throwInvalidStation("Station not found: " + stationId);
		}
	}

	@Override
	public int getCredit(String email) throws UserNotExists_Exception {
		try {
			User user = BinasManager.getInstance().getUser(email);	
			return user.getCredit();
		} catch (UserNotFoundException e) {
			throwUserNotExists("User not found: " + email);
		}
		return 0;
	}
	
	// Auxiliary operations --------------------------------------------------
	
	@Override
	public String testPing(String inputMessage) {
		final String EOL = String.format("%n");
		StringBuilder sb = new StringBuilder();

		sb.append("Hello ");
		if (inputMessage == null || inputMessage.length()==0)
			inputMessage = "friend";
		sb.append(inputMessage);
		sb.append(" from ");
		sb.append(endpointManager.getWsName());
		sb.append("!");
		sb.append(EOL);
		
		Collection<String> stationUrls = null;
		try {
			UDDINaming uddiNaming = endpointManager.getUddiNaming();
			stationUrls = uddiNaming.list(BinasManager.getInstance().getStationTemplateName() + "%");
			sb.append("Found ");
			sb.append(stationUrls.size());
			sb.append(" stations on UDDI.");
			sb.append(EOL);
		} catch(UDDINamingException e) {
			sb.append("Failed to contact the UDDI server:");
			sb.append(EOL);
			sb.append(e.getMessage());
			sb.append(" (");
			sb.append(e.getClass().getName());
			sb.append(")");
			sb.append(EOL);
			return sb.toString();
		}

		for(String stationUrl : stationUrls) {
			sb.append("Ping result for station at ");
			sb.append(stationUrl);
			sb.append(":");
			sb.append(EOL);
			try {
				StationClient client = new StationClient(stationUrl);
				String supplierPingResult = client.testPing(endpointManager.getWsName());
				sb.append(supplierPingResult);
			} catch(Exception e) {
				sb.append(e.getMessage());
				sb.append(" (");
				sb.append(e.getClass().getName());
				sb.append(")");
			}
			sb.append(EOL);
		}
		
		return sb.toString();
	}

	@Override
	public void testClear() {
		//Reset Binas
		BinasManager.getInstance().reset();

		//Reset All Stations
		Collection<String> stations = BinasManager.getInstance().getStations();
		String uddiUrl = BinasManager.getInstance().getUddiURL();
		StationClient sc = null;

		for (String s : stations) {
			try {
				sc = new StationClient(uddiUrl, s);
				sc.testClear();
			} catch(StationClientException e) {
				continue;
			}
		}
	}

	@Override
	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize)
			throws BadInit_Exception {
		
		try {
			BinasManager.getInstance().testInitStation(stationId,x,y,capacity,returnPrize);
		} catch (BadInitException e) {
			throwBadInit("Bad init values");
		} catch (StationNotFoundException e) {
			throwBadInit("No Station found with ID: " + stationId);
		}
	}

	@Override
	public void testInit(int userInitialPoints) throws BadInit_Exception {
		try {
			BinasManager.getInstance().init(userInitialPoints);
		} catch (BadInitException e) {
			throwBadInit("Bad init values: " + userInitialPoints);
		}
	}
	
	
	// View helpers ----------------------------------------------------------
	
	private StationView newStationView(org.binas.station.ws.StationView sv) {
		StationView retSv = new StationView();
		CoordinatesView coordinates = new CoordinatesView();
		coordinates.setX(sv.getCoordinate().getX());
		coordinates.setY(sv.getCoordinate().getY());
		
		retSv.setCapacity(sv.getCapacity());
		retSv.setCoordinate(coordinates);
		retSv.setAvailableBinas(sv.getAvailableBinas());
		retSv.setFreeDocks(sv.getFreeDocks());
		retSv.setId(sv.getId());
		retSv.setTotalGets(sv.getTotalGets());
		retSv.setTotalReturns(sv.getTotalReturns());
		return retSv;
	}
	
	// Exception helpers -----------------------------------------------------
	
	private void throwInvalidEmail(final String message) throws InvalidEmail_Exception {
		InvalidEmail faultInfo = new InvalidEmail();
		faultInfo.setMessage(message);
		throw new InvalidEmail_Exception(message, faultInfo);
	}
	
	private void throwEmailExists(final String message) throws EmailExists_Exception {
		EmailExists faultInfo = new EmailExists();
		faultInfo.setMessage(message);
		throw new EmailExists_Exception(message, faultInfo);
	}
	
	private void throwInvalidStation(final String message) throws InvalidStation_Exception {
		InvalidStation faultInfo = new InvalidStation();
		faultInfo.setMessage(message);
		throw new InvalidStation_Exception(message, faultInfo);
	}
	
	private void throwUserNotExists(final String message) throws UserNotExists_Exception {
		UserNotExists faultInfo = new UserNotExists();
		faultInfo.setMessage(message);
		throw new UserNotExists_Exception(message, faultInfo);
	}
	
	private void throwNoCredit(final String message) throws NoCredit_Exception {
		NoCredit faultInfo = new NoCredit();
		faultInfo.setMessage(message);
		throw new NoCredit_Exception(message, faultInfo);
	}
	
	private void throwAlreadyHasBina(final String message) throws AlreadyHasBina_Exception {
		AlreadyHasBina faultInfo = new AlreadyHasBina();
		faultInfo.setMessage(message);
		throw new AlreadyHasBina_Exception(message, faultInfo);
	}
	
	private void throwNoBinaAvail(final String message) throws NoBinaAvail_Exception {
		NoBinaAvail faultInfo = new NoBinaAvail();
		faultInfo.setMessage(message);
		throw new NoBinaAvail_Exception(message, faultInfo);
	}
	
	private void throwNoBinaRented(final String message) throws NoBinaRented_Exception {
		NoBinaRented faultInfo = new NoBinaRented();
		faultInfo.setMessage(message);
		throw new NoBinaRented_Exception(message, faultInfo);
	}
	
	private void throwFullStation(final String message) throws FullStation_Exception {
		FullStation faultInfo = new FullStation();
		faultInfo.setMessage(message);
		throw new FullStation_Exception(message, faultInfo);
	}

	private void throwBadInit(final String message) throws BadInit_Exception {
		BadInit faultInfo = new BadInit();
		faultInfo.setMessage(message);
		throw new BadInit_Exception(message, faultInfo);
	}
}
