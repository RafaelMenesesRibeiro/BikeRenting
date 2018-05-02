package org.binas.station.ws;

import org.binas.station.domain.Station;

/**
 * The application is where the service starts running. The program arguments
 * are processed here. Other configurations can also be done here.
 */
public class StationApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + StationApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		
		
		// Create server implementation object, according to options
		StationEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new StationEndpointManager(wsURL);
			Station.getInstance().setId(wsURL);

		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new StationEndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
			Station.getInstance().setId(wsName);
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}