package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

public class Position {
    public double latitude;
    public double longitude;
    private static final HashMap<Direction, double[]> map = PosCalculator.getPosChanges();

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


    public Position nextPosition(Direction direction) {

        // define new latitude and longitude to save the latest coordinate
        double latitude_new = this.latitude + map.get(direction)[0];
        double longitude_new = this.longitude + map.get(direction)[1];

        return new Position(latitude_new, longitude_new);
    }


    // compare latitude and longitude with the boundaries to see if the drone is still in play area
    public boolean inPlayArea() {
        return (!(this.longitude <= -3.192473)) &
                (!(this.longitude >= -3.184319)) &
                (!(this.latitude >= 55.946233)) &
                (!(this.latitude <= 55.942617));
    }
}
