package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class StatefulDrone extends StatelessDrone {

    private final double radius = 0.001;
    private Position position = null;
    private double coins;
    private double power;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Station nextStation = null;
    public static final HashMap<Double, Direction> radians = PosCalculator.getGradians();


    // Initilaze the stateful drone
    public StatefulDrone(double initialLatitude, double initialLongitude, String year, String month, String day) {
        setPosition(new Position(initialLatitude, initialLongitude));
        setCoins(0.0);
        setPower(250.0);
        setStepCount(0);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    // Function to get and set current position
    public Position getPosition() {
        return this.position;
    }
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }

    // Functions to get and set the current number of coins
    public double getCoins() {
        return this.coins;
    }
    public void setCoins(double newCoins) {
        this.coins = newCoins;
    }

    // Function to get the current amount of power
    public double getPower() { return this.power; }
    public void setPower(double newPower) { this.power = newPower; }

    // Functions to get and set step count
    public int getStepCount() { return this.stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }

    @Override
    public void nextStep(ArrayList<Station> stations) {
        // TODO write TXT file and use JsonWriter to record each coordinate

        if (this.nextStation == null){
            this.nextStation = decideNextStation(stations);
            System.out.println(this.nextStation.getCoins());
        }
        
        Direction nextDirection = decideNextDirection(stations);
        finishStep(nextDirection, stations);
    }

    // TODO implement if no more positive station
    private Station decideNextStation(ArrayList<Station> stations) {
        ArrayList<Station> unexploredStas = new ArrayList<Station>(stations);
        unexploredStas.removeIf(Station -> (!Station.getPositive()));
        unexploredStas.removeIf(Station::getExplored);
        System.out.println(unexploredStas.size());
        return getMaxStation(unexploredStas);
    }

    private Station getMaxStation(ArrayList<Station> unexploredStas) {
        // Define a score value to measure which station has the most profit and sort the station
        
        Station MaxStation = Collections.min(unexploredStas, new Comparator<Station> (){
            public int compare (Station sta1, Station sta2) {
                double score1 = distance(sta1.getPosition(), getPosition());
                double score2 = distance(sta2.getPosition(), getPosition());
                if (approxEq(score1, score2)) return 0;
                else if (score1 > score2) return 1;
                return -1;
            }
        });
        return MaxStation;
    }



    // Check if there are some stations nearby, get the nearest one
    protected Station checkNearby(Position position, ArrayList<Station> stations) {
        Station nearestSta = null;
        double shortestdis = -1.0;

        // Find the nearest station
        for (int i = 0; i < stations.size(); i++) {
            Position staPos = stations.get(i).getPosition();
            double dis = distance(position, staPos);
            if (dis < 0.00025)
                if ((shortestdis < 0.0 || dis < shortestdis) && (!stations.get(i).getExplored()))
                    nearestSta = stations.get(i);
        }
        return nearestSta;
    }

    private Direction decideNextDirection(ArrayList<Station> stations) {

        Station nextSta = this.nextStation;
        Position currentPOS = getPosition();
        Position nextStaPos = nextSta.getPosition();
        double[] vector = new double[2];
        vector[0] = nextStaPos.latitude - currentPOS.latitude;
        vector[1] = nextStaPos.longitude - currentPOS.longitude;
        double angle = Math.atan(vector[0]/vector[1]);
        if (angle < 0) {
            angle += Math.toRadians(360);
        }


        // Find the direction to go towards our target station
        Direction nextDir = null;
        double nextDirAngle = 0.0;
        double r = 0.0;
        while (r <= 360.0) {

            if (angle <= Math.toRadians(r)) {
                nextDir = radians.get(Math.toRadians(r%360));
                nextDirAngle = r;
                break;
            }
            r += 22.5;
        }
        
        // See if this direction has a negative station
        // i is for avoiding infinite loops

        Position nextPos = getPosition().nextPosition(nextDir);
        Station nextNearestSta = null;


        // if there is a negative station nearby, we need to change another direction
        int i = 0;
        double step = 0.0;
        Direction nullDir = Direction.N;
        while(i <= 15) {
            nextDirAngle = ((nextDirAngle + step) % 360);
            nextDir = radians.get(Math.toRadians(nextDirAngle));
            nextPos = getPosition().nextPosition(nextDir);
            nextNearestSta = checkNearby(nextPos, stations);

            if (nextPos.inPlayArea()) {
                if (nextNearestSta == null) {
                    if (distance(nextPos, this.nextStation.getPosition()) < distance(getPosition().nextPosition(nullDir), this.nextStation.getPosition())) {
                        nullDir = nextDir;
                    }
                }
                else if (nextNearestSta.getExplored()) {
                    if (distance(nextPos, this.nextStation.getPosition()) < distance(getPosition().nextPosition(nullDir), this.nextStation.getPosition())) {
                        nullDir = nextDir;
                    }
                }
                else if (nextNearestSta.getPositive()) {
                    return nextDir;
                }
            }

            i++;
            step = 22.5;
        }

        return nullDir;
    }


    private boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }


    public void finishStep(Direction d,  ArrayList<Station> stations) {

        // Update coordinate
        setPosition(getPosition().nextPosition(d));
        Station nearestSta = checkNearby(getPosition(), stations);

        if((nearestSta != null) && (!nearestSta.getExplored())) {
            System.out.println("Get!");
            System.out.println(nearestSta.getCoins());
            setCoins(getCoins() + nearestSta.getCoins());
            setPower(getPower() + nearestSta.getPower());

            // Update the coins and power for the connecting station
            nearestSta.setCoins(nearestSta.getCoins() - (nearestSta.getCoins() - getCoins()));
            nearestSta.setPower(nearestSta.getPower() - (nearestSta.getPower() - getPower()));

            nearestSta.setExplored(true);
            if (getCoins() < 0.0) setCoins(0.0);
            if (getPower() < 0.0) setPower(0.0);

            // If the drone already took all the coins and power, set the station to be explored
            if (approxEq(nearestSta.getCoins(), 0.0) && approxEq(nearestSta.getPower(), 0.0)) {
                nearestSta.setExplored(true);
            }
            this.nextStation = null;
        }
        this.stepCount += 1;
    }

    protected double distance(Position position1, Position position2) {
        return Math.sqrt(Math.pow(position1.latitude - position2.latitude, 2) + Math.pow(position1.longitude - position2.longitude, 2));
    }


    // Check if the game goes to the end
    public boolean checkEnd() {
        return stepCount >= 250 || getPower() <= 0.0;
    }

//    public void meetNegative(){
//
//    }

}
