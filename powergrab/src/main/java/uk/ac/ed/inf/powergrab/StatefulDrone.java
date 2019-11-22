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
    private static final HashMap<Double, Direction> radians = PosCalculator.getGradians();


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
        if (this.nextStation == null)
            this.nextStation = decideNextStation(stations);

        Direction nextDirection = decideNextDirection(stations);
        finishStep(nextDirection, stations);

    }


    private Station decideNextStation(ArrayList<Station> stations) {
        ArrayList<Station> unexploredStas = new ArrayList<Station>(stations);
        unexploredStas.removeIf(Station::getExplored);
        return getMaxStation(unexploredStas);
    }

    private Station getMaxStation(ArrayList<Station> unexploredStas) {
        // Define a score value to measure which station has the most profit and sort the station
        // TODO score = powerEarn - powerCost + coinsEarn + AroundPositiveStations * 10
        Station MaxStation = Collections.max(unexploredStas, new Comparator<Station> (){
            public int compare (Station sta1, Station sta2) {
                double score1 = stationScore(sta1, unexploredStas);
                double score2 = stationScore(sta2, unexploredStas);
                if (approxEq(score1, score2)) return 0;
                else if (score1 > score2) return 1;
                return -1;
            }
        });

        return MaxStation;
    }

    // Calculate the score value for a station
    private double stationScore(Station station, ArrayList<Station> stations) {
        double score = 0.0;
        int stationsAround = 0;
        final double AROUND_RADIUS = 0.0012;

        // Find the number of stations in range of 0.0012
        for (int i = 0; i < stations.size(); i++) {
            Station s = stations.get(i);
            if ((super.distance(station.getPosition(), s.getPosition()) < AROUND_RADIUS) && (station != s)) {
                stationsAround += 1;
            }
        }

        // score = powerEarn - powerCost + coinsEarn + AroundPositiveStations * 10
        score = station.getPower() - (super.distance(getPosition(), station.getPosition())) * (1.25/0.0003) + station.getCoins() +
                (double) stationsAround * 10 - (super.distance(getPosition(), station.getPosition())) * 10000;
        return score;
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
        Position currentPOS = this.getPosition();
        Position nextStaPos = nextSta.getPosition();
        double[] vector = new double[2];
        vector[0] = nextStaPos.latitude - currentPOS.latitude;
        vector[1] = nextStaPos.longitude - currentPOS.longitude;
        double angle = Math.atan(vector[0]/vector[1]);

        // Find the direction to go towards our target station
        Direction nextDir = null;
        double nextDirAngle = 0.0;
        for(double r = 0.0; r < 360.0; r+=22.5){
            if (angle <= r) {
                nextDir = radians.get(Math.toRadians(r));
                nextDirAngle = r;
                break;
            }
        }

        // See if this direction has a negative station
        // i is for avoiding infinite loops
        int i = 0;
        Position nextPos = getPosition().nextPosition(nextDir);
        Station nextNearestSta =checkNearby(nextPos, stations);
        if ((nextNearestSta == null) | nextNearestSta.getExplored())
            return nextDir;
        While((!nextNearestSta.getPositive()) && (i <= 15)){
            nextDirAngle = (nextDirAngle + 22.5) % 360;
            nextDir = radians.get(Math.toRadians(nextDirAngle));
            nextPos = getPosition().nextPosition(nextDir);
            i++;
        }

        return nextDir;
    }


    private boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }


    public void finishStep(Direction d,  ArrayList<Station> stations) {
        // TODO update the coins and power, set station explored, set nextStation to null

        // Update coordinate
        setPosition(getPosition().nextPosition(d));
        Station nearestSta = checkNearby(getPosition(), stations);

        if((nearestSta != null) && (!nearestSta.getExplored())){
            setCoins(getCoins() + nearestSta.getCoins());
            setPower(getPower() + nearestSta.getPower());

            // Update the coins and power for the connecting station
            nearestSta.setCoins(nearestSta.getCoins() - (nearestSta.getCoins() - getCoins()));
            nearestSta.setPower(nearestSta.getPower() - (nearestSta.getPower() - getPower()));

            if (getCoins() < 0.0) setCoins(0.0);
            if (getPower() < 0.0) setPower(0.0);

            // If the drone already took all the coins and power, set the station to be explored
            if (approxEq(nearestSta.getCoins(), 0.0) && approxEq(nearestSta.getPower(), 0.0))
                nearestSta.setExplored(true);

        }

    }

//    public void meetNegative(){
//
//    }

}
