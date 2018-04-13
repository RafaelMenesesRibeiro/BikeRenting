package org.binas.domain;

import java.util.Comparator;

import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

/**
 * This class compares the coordinates of Two different stations by implementing the euclidean distance 
 *
 */
public class StationsComparator implements Comparator<StationView> {
	private CoordinatesView coordinates;
	
	public StationsComparator(CoordinatesView coordinates) {
		this.coordinates = coordinates;
	}

	// Comparison based on Euclidean Distance
	public int compare(StationView s1, StationView s2) { 
		CoordinatesView coordsS1 = s1.getCoordinate();
		CoordinatesView coordsS2 = s2.getCoordinate();
		double distS1 = Math.sqrt(Math.pow(coordsS1.getY() - this.coordinates.getY(), 2) +
								  Math.pow(coordsS1.getX() - this.coordinates.getX(), 2));
		double distS2 = Math.sqrt(Math.pow(coordsS2.getY() - this.coordinates.getY(), 2) +
				  				  Math.pow(coordsS2.getX() - this.coordinates.getX(), 2));
		return Double.compare(distS1, distS2);
	}

}
