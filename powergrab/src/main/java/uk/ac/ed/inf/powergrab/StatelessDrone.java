package uk.ac.ed.inf.powergrab;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

public class StatelessDrone implements Drone {
    private Position position = null;
    private double coins;
    private double power;
    private int randomSeed;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Random rand = new Random();

    // Initialise the drone, set the initial position, coins and power
    public StatelessDrone(double initialLatitude, double initialLongitude, int randomSeed, String year, String month, String day) {
        setPosition(new Position(initialLatitude, initialLongitude));
        setCoins(0.0);
        setPower(250.0);
        setRandomSeed(randomSeed);
        setStepCount(0);
        this.rand.setSeed(this.randomSeed);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public StatelessDrone() {
        setCoins(0.0);
        setPower(250.0);
        setStepCount(0);
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

    // Functions to get and set random seed
    public int getRandomSeed() { return this.randomSeed; }
    public void setRandomSeed(int randomSeed) { this.randomSeed = randomSeed; }


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


    // Play the nextStep, updating the coins and power and output the step information to txt document
    public void nextStep(ArrayList<Station> stations) {
        Direction nextDirection = decideDirection(stations);
        Position prePos = getPosition();

        this.stepCount += 1;
        setPower(getPower() - 1.25);
        setPosition(this.position.nextPosition(nextDirection));

        // Output the txt document
        String filename = "/Users/waylon/Desktop/ILP Output/stateless-" + day + "-" + month + "-" + year + ".txt";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true)));
            out.write(prePos.latitude + "," + prePos.longitude + ","
                    + nextDirection + ","
                    + getPosition().latitude + "," + getPosition().longitude + ","
                    + getCoins() + "," + getPower() + "\n");
            out.flush();
            out.close();
        } catch (Exception a) {
            System.out.println("Output error!");
        }

    }


    // Check if the game goes to the end
    public boolean checkEnd() {
        return stepCount >= 250 || getPower() <= 0.0;
    }


    // Function to calculate the distance between two positions
    protected double distance(Position position1, Position position2) {
        return Math.sqrt(Math.pow(position1.latitude - position2.latitude, 2) + Math.pow(position1.longitude - position2.longitude, 2));
    }


    // Choose one direction from the 16 directions to go depending on taking a glance of the next step
    private Direction decideDirection(ArrayList<Station> stations) {
        Direction positiveDirection = null;
        Direction minNegtiveDirection = null;
        ArrayList<Direction> neutralDirections = new ArrayList<Direction>();
        Station maxPositiveSta = null;
        Station minNegativeSta = null;

        for (Direction d : Direction.values()) {
            Position nextPos = this.position.nextPosition(d);
            Station nearestSta = checkNearby(nextPos, stations);

            // Check if it is still in playarea
            if (!nextPos.inPlayArea()) continue;

            // See if the nearest station contain positive coins and power
            if (nearestSta == null || nearestSta.getExplored()) neutralDirections.add(d);
            else if (nearestSta.getPositive()) {
                // Choose the positive station with max amount of power
                if (positiveDirection == null) {
                    positiveDirection = d;
                    maxPositiveSta = nearestSta;
                } else if (nearestSta.getPower() > maxPositiveSta.getPower()) {
                    positiveDirection = d;
                    maxPositiveSta = nearestSta;
                }
            } else if (!nearestSta.getPositive() && nearestSta.getPower() < 0) {
                if (minNegativeSta == null) {
                    minNegtiveDirection = d;
                    minNegativeSta = nearestSta;
                } else if (nearestSta.getPower() > minNegativeSta.getPower()) {
                    minNegtiveDirection = d;
                    minNegativeSta = nearestSta;
                }
            }
        }

        // Generate a random number from randomSeed to decide a direction
        if (positiveDirection == null) {
            if (neutralDirections.size() > 0) {
                int randomNum = rand.nextInt((neutralDirections.size()));
                return neutralDirections.get(randomNum);
            } else {
                setCoins(getCoins() + minNegativeSta.getCoins());
                setPower(getPower() + minNegativeSta.getPower());
                checkEnd();
                minNegativeSta.setCoins(0.0);
                minNegativeSta.setPower(0.0);
                minNegativeSta.setExplored(true);
                return minNegtiveDirection;
            }
        }
        // If we have a direction which can reach to a positive station, then choose this direction
        setCoins(getCoins() + maxPositiveSta.getCoins());
        setPower(getPower() + maxPositiveSta.getPower());
        if (maxPositiveSta != null) {
            maxPositiveSta.setCoins(0.0);
            maxPositiveSta.setPower(0.0);
            maxPositiveSta.setExplored(true);
        }
        return positiveDirection;
    }

}
