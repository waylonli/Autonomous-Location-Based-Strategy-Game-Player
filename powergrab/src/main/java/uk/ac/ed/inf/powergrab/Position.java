package uk.ac.ed.inf.powergrab;

public class Position {
    public double latitude;
    public double longitude;


    public Position(double latitude, double longitude) {
        // update the coordinate of the drone
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position nextPosition(Direction direction) {
    	
    	// define new latitude and longitude to save the latest coordinate
        double latitude_new = this.latitude;
        double longitude_new = this.longitude;

        switch (direction){
            case N: {
                latitude_new += 0.0003;
                break;
            }
            case S: {
                latitude_new -= 0.0003;
                break;
            }
            case E: {
                longitude_new += 0.0003;
                break;
            }
            case W: {
                longitude_new -= 0.0003;
                break;
            }

            // define how the coordinates change after moving
            case NE: {
                latitude_new += (0.0003 * Math.sin(Math.toRadians(45)));
                longitude_new += (0.0003 * Math.cos(Math.toRadians(45)));
                break;
            }
            case SE: {
                latitude_new -= (0.0003 * Math.sin(Math.toRadians(45)));
                longitude_new += (0.0003 * Math.cos(Math.toRadians(45)));
                break;
            }
            case NW: {
                latitude_new += (0.0003 * Math.sin(Math.toRadians(45)));
                longitude_new -= (0.0003 * Math.cos(Math.toRadians(45)));
                break;
            }
            case SW: {
                latitude_new -= (0.0003 * Math.sin(Math.toRadians(45)));
                longitude_new -= (0.0003 * Math.cos(Math.toRadians(45)));
                break;
            }

            case NNE: {
                latitude_new += (0.0003 * Math.cos(Math.toRadians(22.5)));
                longitude_new += (0.0003 * Math.sin(Math.toRadians(22.5)));
                break;
            }
            case ENE: {
                latitude_new += (0.0003 * Math.sin(Math.toRadians(22.5)));
                longitude_new += (0.0003 * Math.cos(Math.toRadians(22.5)));
                break;
            }
            case ESE: {
                latitude_new -= (0.0003 * Math.sin(Math.toRadians(22.5)));
                longitude_new += (0.0003 * Math.cos(Math.toRadians(22.5)));
                break;
            }
            case SSE: {
                latitude_new -= (0.0003 * Math.cos(Math.toRadians(22.5)));
                longitude_new += (0.0003 * Math.sin(Math.toRadians(22.5)));
                break;
            }
            case SSW: {
                latitude_new -= (0.0003 * Math.cos(Math.toRadians(22.5)));
                longitude_new -= (0.0003 * Math.sin(Math.toRadians(22.5)));
                break;
            }
            case WSW: {
                latitude_new -= (0.0003 * Math.sin(Math.toRadians(22.5)));
                longitude_new -= (0.0003 * Math.cos(Math.toRadians(22.5)));
                break;
            }
            case WNW: {
                latitude_new += (0.0003 * Math.sin(Math.toRadians(22.5)));
                longitude_new -= (0.0003 * Math.cos(Math.toRadians(22.5)));
                break;
            }
            case NNW: {
                latitude_new += (0.0003 * Math.cos(Math.toRadians(22.5)));
                longitude_new -= (0.0003 * Math.sin(Math.toRadians(22.5)));
            }

        }

        return new Position(latitude_new, longitude_new);
    }

    // judge if the drone is still in the playing area
    public boolean inPlayArea() {
        if (this.latitude >= 55.946233 || this.latitude <= 55.942617) return false;
        if (this.longitude <= -3.192473 || this.longitude >= -3.184319) return false;
        return true;
    }
}
