package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Random;

public class StatelessDrone implements Drone {
    private Position position = null;
    private double coins;
    private double power;
    private int randomSeed;

    // Initialise the drone, set the initial position, coins and power
    public StatelessDrone(double initialLatitude, double initialLongitude, int randomSeed) {
        this.position = new Position(initialLatitude, initialLongitude);
        this.coins = 0.0;
        this.power = 250.0;
        this.randomSeed = randomSeed;
    }

    // Function to update position
    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }

    // Function to get current position
    public Position getPosition() {
        return this.position;
    }

    // Function to update number of coins
    public void updateCoins(double newCoins) {
        this.coins = newCoins;
    }

    // Function to get the current number of coins
    public double getCoins() {
        return this.coins;
    }

    // Function to update amount of power
    public void updatePower(double newPower) {
        this.power = newPower;
    }

    // Function to get the current amount of power
    public double getPower() {
        return this.power;
    }

    // Check if there are some stations nearby, get the nearest one
    public Feature checkNearby(Position position, ArrayList<Feature> stations) {
        Feature nearestSta = null;
        double shortestdis = -1.0;
        // Find the nearest station
        for (int i = 0; i < 50; i++) {
            Point p = (Point) stations.get(i).geometry();
            Position staPos = new Position(p.coordinates().get(1), p.coordinates().get(0));
            double dis = distance(position, staPos);
            if (dis < 0.00025)
                if (shortestdis < 0.0 || dis < shortestdis) nearestSta = stations.get(i);
        }
        return nearestSta;
    }

    public void nextStep(ArrayList<Feature> stations) {
        Direction nextDirection = decidePosition(stations);
    }

    // Function to calculate two positions
    private double distance(Position position1, Position position2) {
        return Math.sqrt(Math.pow(position1.latitude - position2.latitude, 2) + Math.pow(position1.longitude - position2.longitude, 2));
    }

    private Direction decidePosition(ArrayList<Feature> stations) {
        Direction positiveDirection = null;
        ArrayList<Direction> neutralDirections = new ArrayList<Direction>();
        double maxPower = 0.0;


        for (Direction d : Direction.values()) {
            Position nextPos = this.position.nextPosition(d);
            Feature nearestSta = checkNearby(nextPos, stations);

            // See if the nearest station contain positive coins and power
            if (nearestSta == null) neutralDirections.add(d);
            else if (nearestSta.getProperty("coins").getAsDouble() > 0.0) {
                // Choose the positive station with max amount of power
                double staPower = nearestSta.getProperty("power").getAsDouble();
                if (positiveDirection == null) {
                    positiveDirection = d;
                    maxPower = staPower;
                } else if (positiveDirection == null) {
                    if (staPower > maxPower) {
                        positiveDirection = d;
                        maxPower = staPower;
                    }
                }
            }
        }

        // Generate a random number from randomSeed to decide a direction
        if (positiveDirection == null) {
            Random rand = new Random();
            rand.setSeed(this.randomSeed);
            int randomNum = rand.nextInt((neutralDirections.size()));
            System.out.println(neutralDirections.size());
            System.out.println(randomNum);
            return neutralDirections.get(randomNum);

        }
        // If we have a direction which can reach to a positive station, then choose this direction
        return positiveDirection;
    }
}
