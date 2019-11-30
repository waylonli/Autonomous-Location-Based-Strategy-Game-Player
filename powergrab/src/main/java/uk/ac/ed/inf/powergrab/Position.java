package uk.ac.ed.inf.powergrab;

import java.util.HashMap;


/**
 * Represent the coordinate of drone and stations
 * @author s1891340
 */
public class Position {
    private static final HashMap<Direction, double[]> dirMap = PosCalculator.getPosChanges();
    /**
     * @param latitude the latitude of the Position object
     * @param longitude the longitude of the Position object
     * @param dirMap<Direction, double[]> map each direction to a double array with length two, indicating the change of latitude and longitude
     */
    public double latitude;
    public double longitude;



    /**
    * Constructor of Position
    * @param latitude the initial latitude
    * @param longitude the initial longitude
     */
    public Position(double latitude, double longitude) {
        // update the coordinate of the drone
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }


    /**
     * Calculate the next position after moving one step towards a direction
     * @param direction the direction to move
     * @return the new position after moving
     */
    public Position nextPosition(Direction direction) {

        // define new latitude and longitude to save the latest coordinate
        double latitude_new = this.latitude + dirMap.get(direction)[0];
        double longitude_new = this.longitude + dirMap.get(direction)[1];

        return new Position(latitude_new, longitude_new);
    }



    /**
     * Compare the latitude and longitude with the boundaries to see if the drone is still in play area
     * @return true if the drone is still in play area, false if it is not
     */
    public boolean inPlayArea() {
        return (!(this.longitude <= -3.192473)) &
                (!(this.longitude >= -3.184319)) &
                (!(this.latitude >= 55.946233)) &
                (!(this.latitude <= 55.942617));
    }
}
