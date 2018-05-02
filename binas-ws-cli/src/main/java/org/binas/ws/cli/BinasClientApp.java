package org.binas.ws.cli;

import java.util.List;

import org.binas.ws.ActivateUser;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.StationView;
import org.binas.ws.UserNotExists_Exception;
import org.binas.ws.UserView;

import java.util.Scanner;

/**
 * Class that contains the main of the BinasClient
 * 
 * Looks for Binas using arguments that come from pom.xm
 *
 */
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
        System.out.println("Invoking activateUser()...");
        client.activateUser("aaaa@gmail.com");

        Scanner reader = new Scanner(System.in);
        int n = -1;

        while(n != 0) {
            System.out.printf("%n%n");
            System.out.println("[0] Exit");
            System.out.println("[1] Activate user");
            System.out.println("[2] Get station information");
            System.out.println("[3] List stations");
            System.out.println("[4] Rent bina");
            System.out.println("[5] Return bina");
            System.out.println("[6] Get credit");
            System.out.println("Enter a number: ");
            n = reader.nextInt();
            System.out.printf("%n%n");
            switch (n) {
            case 1:
                System.out.print("Input email: ");
                String email = reader.next();
                UserView user = client.activateUser(email);
                System.out.println("User email: " + user.getEmail());
                System.out.println("User hasBina: " + user.isHasBina());
                System.out.println("User credit: " + user.getCredit().toString());
                break;
            case 2:
                System.out.print("Input stationId: ");
                String stationId = reader.next();
                StationView station = client.getInfoStation(stationId);
                System.out.println("Station id: " + station.getId());
                System.out.println("Station coordinate x: " + station.getCoordinate().getX());
                System.out.println("Station coordinate y: " + station.getCoordinate().getY());
                System.out.println("Station capacity: " + station.getCapacity());
                System.out.println("Station totalGets: " + station.getTotalGets());
                System.out.println("Station totalReturns: " + station.getTotalReturns());
                System.out.println("Station availableBinas: " + station.getAvailableBinas());
                System.out.println("Station freeDocks: " + station.getFreeDocks());
                break;
            case 3:
                CoordinatesView c = new CoordinatesView();
                c.setX(0);
                c.setY(0);
                List<StationView> svs = client.listStations(3, c);
                for (StationView station3 : svs) {
                    System.out.println("Station id: " + station3.getId());
                    System.out.println("Station coordinate x: " + station3.getCoordinate().getX());
                    System.out.println("Station coordinate y: " + station3.getCoordinate().getY());
                    System.out.println("Station capacity: " + station3.getCapacity());
                    System.out.println("Station totalGets: " + station3.getTotalGets());
                    System.out.println("Station totalReturns: " + station3.getTotalReturns());
                    System.out.println("Station availableBinas: " + station3.getAvailableBinas());
                    System.out.println("Station freeDocks: " + station3.getFreeDocks());
                    System.out.println("-------------------------------------");
                }
                break;
            case 4:
                System.out.print("Input stationId: ");
                String stationId4 = reader.next();
                System.out.print("Input email: ");
                String email4 = reader.next();
                client.rentBina(stationId4, email4);
                break;
            case 5:
                System.out.print("Input stationId: ");
                String stationId5 = reader.next();
                System.out.print("Input email: ");
                String email5 = reader.next();
                client.returnBina(stationId5, email5);
                break;
            case 6:
                System.out.print("Input email: ");
                String email6 = reader.next();
                int credit = client.getCredit(email6);
                System.out.println("User credit: " + credit);
                break;
            
                default:
                    break;
            }
        }

        reader.close();
     }
}