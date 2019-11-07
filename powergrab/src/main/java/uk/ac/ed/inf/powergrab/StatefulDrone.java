package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        if (this.nextStation == null)
            this.nextStation = decideNextStation(stations);



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


//    private Direction decideNextDirection() {
//
//    }


    private boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }


    public void finishLink() {
        // TODO update the coins and power, set station explored, set nextStation to null
    }

//    public void meetNegative(){
//
//    }

}
