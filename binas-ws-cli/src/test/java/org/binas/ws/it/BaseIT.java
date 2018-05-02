package org.binas.ws.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.cli.BinasClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


/*
 * Base class of tests
 * Loads the properties in the file
 */
public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;

	protected static BinasClient client;
	protected static String stationBaseName;
	protected static String binasClearStations;
	protected static final String VALID_USER = "sd.test@tecnico.ulisboa";

	private static String uddiURL;
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		testProps = new Properties();
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Loaded test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String uddiEnabled = testProps.getProperty("uddi.enabled");
		final String verboseEnabled = testProps.getProperty("verbose.enabled");

		
		final String wsName = testProps.getProperty("ws.name");
		final String wsURL = testProps.getProperty("ws.url");
		
		uddiURL = testProps.getProperty("uddi.url");
		stationBaseName = testProps.getProperty("station.ws.name");
		binasClearStations = testProps.getProperty("clear.stations.enabled");
		
		if ("true".equalsIgnoreCase(uddiEnabled)) {
			client = new BinasClient(uddiURL, wsName);
		} else {
			client = new BinasClient(wsURL);
		}
		client.setVerbose("true".equalsIgnoreCase(verboseEnabled));

	}

	@AfterClass
	public static void cleanup() {
	}
	
	private static Collection<String> getStations() {
		Collection<UDDIRecord> records = null;
		Collection<String> stations = new ArrayList<String>();
		try {
			UDDINaming uddi = new UDDINaming(uddiURL);
			records = uddi.listRecords(stationBaseName + "%");
			for (UDDIRecord u : records)
				stations.add(u.getOrgName());
		} catch (UDDINamingException e) {
		}
		return stations;
	}
	
	protected static void binasTestClear() {
		client.testClear();
		
		if ("true".equalsIgnoreCase(binasClearStations)) {
			Collection<String> stations = getStations();
			StationClient sc = null;

			for (String s : stations) {
				try {
					sc = new StationClient(uddiURL, s);
					sc.testClear();
				} catch(StationClientException e) {
					continue;
				}
			}
		}
	}


}
