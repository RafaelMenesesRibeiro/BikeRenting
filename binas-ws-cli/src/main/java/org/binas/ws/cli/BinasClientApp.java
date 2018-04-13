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
			client.testClear();
			client.activateUser("a@test.com");
			List<StationView> list = client.listStations(1, c);
			for (StationView v : list) {
				System.out.println("Credit before rent: " + client.getCredit("a@test.com"));
				client.rentBina(v.getId(), "a@test.com");
				System.out.println("Credit after rent: " + client.getCredit("a@test.com"));
				client.returnBina(v.getId(), "a@test.com");
				System.out.println("Credit after return: " + client.getCredit("a@test.com"));
			}

			list = client.listStations(3, c);
			for (StationView v2 : list) {
				System.out.println(v2.getId() + " " + v2.getCoordinate().getX() + ", " + v2.getCoordinate().getY());
			}
		}
		catch (Exception e) { System.out.println(e.getMessage()); }
	 }
}
