package org.binas.ws.cli;

import org.binas.ws.*;

import org.binas.domain.BinasManager;
import org.binas.domain.User;

import java.util.List;
import java.util.ArrayList;

public class BinasClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BinasClientApp.class.getName()
					+ " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}

		System.out.println(BinasClientApp.class.getSimpleName() + " running");

		// Create client
		BinasClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new BinasClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n",
				uddiURL, wsName);
			client = new BinasClient(uddiURL, wsName);
		}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		//System.out.println("Invoke ping()...");
		//String result = client.testPing("client");
		//System.out.print(result);
		CoordinatesView c = new CoordinatesView();
		c.setX(10);
		c.setY(11);

		try {
			User u = new User("a@test.com", 10);
			List<StationView> list = client.listStations(1, c);
			for (StationView v : list) {
				User u2 = BinasManager.getUser("a@test.com");
				System.out.println(u2.getEmail());
				client.rentBina(v.getId(), u.getEmail());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	 }
}
