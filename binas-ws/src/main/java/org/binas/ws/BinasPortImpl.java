package org.binas.ws;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;

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
import org.binas.domain.exception.QuorumConsensusException;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.UserNotFound_Exception;
import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.TagView;
import org.binas.station.ws.BalanceView;

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

	//Used to check if the asynchronous calls are done.
	int isFinished = 0;
	//Used to check if user exists after all the asynchronous calls.
	boolean isAlreadyUser = false;
	//Used to represent the sequence number in Quorum Consensus.
	int sequenceNumber = 1;
	//Used to represent the maximum sequence number of all the stations.
	int  maxSeq = 0;
	//Used to represent the minimum station id with the maximum sequence number.
	int minCid = 0;

	int cid = 1;


	public BinasPortImpl(BinasEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	@Override
	public UserView activateUser(String email) throws InvalidEmail_Exception, EmailExists_Exception {
		isFinished = 0;
		isAlreadyUser = false;
		maxSeq = 0;
		minCid = 0;

		try {
			int stationNumber = BinasManager.getInstance().getStations().size();
			int minStationAnswers = (int) Math.floor(stationNumber / 2) + 1;
			System.out.println("The minimum number of station answers is " + minStationAnswers);

			CoordinatesView coordinatesView = new CoordinatesView();
			coordinatesView.setX(0);
			coordinatesView.setY(0);
			List<StationView> stations = this.listStations(stationNumber, coordinatesView);

			System.out.println("Found " + stationNumber + " stations running.");			
			for (StationView stationView : stations) {
				try {
					String stationId = stationView.getId();
					StationClient stationCli = BinasManager.getInstance().getStation(stationId);
					//Asynchronous call with callback.
					stationCli.getBalanceAsync(email, new AsyncHandler<GetBalanceResponse>() {
						@Override
						public void handleResponse(Response<GetBalanceResponse> response) {
							try {
								System.out.println("Asynchronous call result arrived: ");
								String className = response.get().getBalanceInfo().getClass().getName();
								if (className.equals("org.binas.station.ws.BalanceView")) {
									isAlreadyUser = true;
									int balance = response.get().getBalanceInfo().getBalance();
									int seq = response.get().getBalanceInfo().getTag().getSeq();
									int cid = response.get().getBalanceInfo().getTag().getCid();
									if (seq > maxSeq) {
										maxSeq = seq;
										minCid = cid;
									}
									else if (seq == maxSeq && cid < minCid) { minCid = cid;	 }
									System.out.println("Balance of user " + email + " is " + balance + ". According to station \"" + stationId + "\"");
								}
								isFinished += 1;
							}
							catch (InterruptedException ie) { System.out.println("Caught interrupted exception.\nCause: " + ie.getCause()); }
							catch (ExecutionException ee) { isFinished += 1; }
						}
					});
				}
				catch (StationNotFoundException e) { /*Do nothing. Continue.*/ }
			}
			
			while (isFinished != stationNumber) {
				try {
					Thread.sleep(100);
					System.out.print(".");
					System.out.flush();
				}
				catch (Exception e) { System.out.println("Caught exception.\n " + "Cause: " + e.getCause()); }
			}

			System.out.println("\nAll asynchronous calls completed.");

			//If at least on of the stations report a user with the given email already exists, throws exception.
			if (isAlreadyUser) { throw new UserAlreadyExistsException(); }

			//If the user doens't exist in any of the available stations, creates one.
			User user = BinasManager.getInstance().createUser(email);
			
			//Sets the sequence number to the maxSeq + 1 found from all the stations.
			this.sequenceNumber = maxSeq + 1;

			isFinished = 0;
			int credit = user.getCredit();
			//And creates one in all the stations.
			for (StationView stationView : stations) {
				try {
					String stationId = stationView.getId();
					StationClient stationCli = BinasManager.getInstance().getStation(stationId);
					TagView newTag = getTag(email);
					newTag.setSeq(newTag.getSeq());
					newTag.setCid(cid);
					//Asynchronous call with callback.
					stationCli.setBalanceAsync(email, credit, newTag, new AsyncHandler<SetBalanceResponse>() {
						@Override
						public void handleResponse(Response<SetBalanceResponse> response) { isFinished += 1; }
					});
				}
				catch (StationNotFoundException e) { /*Do nothing. Continue.*/ }
				catch (UserNotExists_Exception enee) { /*Do nothing. Continue.*/ }
 			}
			
			while (isFinished != stationNumber) {
				try {
					Thread.sleep(100);
					System.out.print(".");
					System.out.flush();
				}
				catch (Exception e) { System.out.println("Caught exception.\n " + "Cause: " + e.getCause()); }
			}

			//Create and populate userView
			UserView userView = new UserView();
			userView.setEmail(user.getEmail());
			userView.setCredit(user.getCredit());
			userView.setHasBina(user.getHasBina());
			return userView;
		}catch (UserAlreadyExistsException e) {
			throwEmailExists("Email already exists: " + email);
		} catch (InvalidEmailException e) {
			throwInvalidEmail("Invalid email: " + email);
		}
		//catch (QuorumConsensusException qce) { throwQuorumConsensus(qce.getMessage()); }
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

	private BalanceView getBalanceView(String email) throws UserNotExists_Exception {
        ArrayList<BalanceView> bvArray = new ArrayList<BalanceView>();
        int stationNumber = BinasManager.getInstance().getStations().size();
        int minStationAnswers = (int) Math.floor(stationNumber / 2) + 1;
        System.out.println("The minimum number of station answers is " + minStationAnswers);
        CoordinatesView coordinatesView = new CoordinatesView();
        coordinatesView.setX(0);
        coordinatesView.setY(0);
        List<StationView> stations = this.listStations(stationNumber, coordinatesView);

        System.out.println("Found " + stationNumber + " stations running.");
        for (StationView stationView : stations) {
            try {
                String stationId = stationView.getId();
                StationClient stationCli = BinasManager.getInstance().getStation(stationId);
                stationCli.getBalanceAsync(email, new AsyncHandler<GetBalanceResponse>() {
                    @Override
                    public void handleResponse(Response<GetBalanceResponse> response) {
                        try {
                            System.out.println("Asynchronous call result arrived: ");
                            String className = response.get().getBalanceInfo().getClass().getName();
                            if (className.equals("org.binas.station.ws.BalanceView")) {
                                bvArray.add(response.get().getBalanceInfo());
                            }
                        }

                        catch (InterruptedException ie) { System.out.println("Caught interrupted exception.\nCause: " + ie.getCause()); }
                        catch (ExecutionException ee) { isFinished += 1; }                        
                    }
                });
            } catch (StationNotFoundException e) {}
        }

        try {
            while (bvArray.size() < minStationAnswers) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ie) {
            System.out.println("Caught interrupted exception.\nCause: " + ie.getCause());
        }

        int maxSeq = -1;
        int maxCid = -1;
        BalanceView maxBV = null;

        for (BalanceView bv : bvArray) {
            if (maxSeq < bv.getTag().getSeq() || (maxSeq == bv.getTag().getSeq() && maxCid < bv.getTag().getCid())) {
                maxSeq = bv.getTag().getSeq();
                maxCid = bv.getTag().getCid();
                maxBV = bv;
            }
        }

        return maxBV;
	}

	@Override
    public int getCredit(String email) throws UserNotExists_Exception {
    	return getBalanceView(email).getBalance();
    }

    public TagView getTag(String email) throws UserNotExists_Exception {
    	return getBalanceView(email).getTag();
    }

    public void setCredit(String email, int credit) throws UserNotExists_Exception{
		TagView newTag = getTag(email);
		newTag.setSeq(newTag.getSeq());
		newTag.setCid(cid);
    	ArrayList<Response> responsesArray = new ArrayList<Response>();
        int stationNumber = BinasManager.getInstance().getStations().size();
        int minStationAnswers = (int) Math.floor(stationNumber / 2) + 1;
        System.out.println("The minimum number of station answers is " + minStationAnswers);
        CoordinatesView coordinatesView = new CoordinatesView();
        coordinatesView.setX(0);
        coordinatesView.setY(0);
        List<StationView> stations = this.listStations(stationNumber, coordinatesView);

        System.out.println("Found " + stationNumber + " stations running.");
		for (StationView stationView : stations) {
			try {
				String stationId = stationView.getId();
				StationClient stationCli = BinasManager.getInstance().getStation(stationId);
				stationCli.setBalanceAsync(email, credit, newTag, new AsyncHandler<SetBalanceResponse>() {
					@Override
					public void handleResponse(Response<SetBalanceResponse> response) {
						System.out.println("Asynchronous call result arrived: ");
						responsesArray.add(response);
					}
				});
			} catch (StationNotFoundException e) {
			}
		}
		
		try {
			while (responsesArray.size() < minStationAnswers) {
				Thread.sleep(100);
			}
		} catch (InterruptedException ie) {
			System.out.println("Caught interrupted exception.\nCause: " + ie.getCause());
		}

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

	private void throwQuorumConsensus(final String message) { //throws QuorumConsensus_Exception {
		//QuorumConsensus faultInfo = new QuorumConsensus();
		//faultInfo.setMessage(message);
		//throw new QuorumConsensus_Exception(message, fault);
		System.out.println(message);
	}
}
