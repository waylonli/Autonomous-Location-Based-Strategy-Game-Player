package uk.ac.ed.inf.powergrab;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;


/**
 * Class for stateless drone, defining the decision of direction and movement strategy
 *
 * @author s1891340
 */
public class StatelessDrone implements Drone {
    /**
     * @param position position of the drone
     * @param coins number of coins of the drone
     * @param power amount of power of the drone
     * @param randomSeed random seed to get random number
     * @param stepCount step count for the drone
     * @param year year of the map
     * @param month month of the map
     * @param day day of the map
     * @param rand the random object to get random number
     */
    private Position position = null;
    private double coins;
    private double power;
    private int randomSeed;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Random rand = new Random();

    /**
     * Constructor of the class. Initialise the drone, set the initial position, coins and power
     *
     * @param initialLatitude  initial latitude of the station
     * @param initialLongitude initial longitude of the station
     * @param randomSeed       the random seed to get random number
     * @param year             year of the map
     * @param month            month of the map
     * @param day              day of the map
     */
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


    /**
     * @return the position of the drone
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * @param newPosition the position to be set for the drone
     */
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }


    /**
     * @return the coins of the drone
     */
    public double getCoins() {
        return this.coins;
    }

    /**
     * @param newCoins the coins number to be set for the drone
     */
    public void setCoins(double newCoins) {
        this.coins = newCoins;
    }


    /**
     * @return the power of the drone
     */
    public double getPower() {
        return this.power;
    }

    /**
     * @param newPower the amount of power to be set for the drone
     */
    public void setPower(double newPower) {
        this.power = newPower;
    }


    /**
     * @return the step count of the drone
     */
    public int getStepCount() {
        return this.stepCount;
    }

    /**
     * @param stepCount the step count number to be set for the drone
     */
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }


    /**
     * @return the random seed of the drone
     */
    public int getRandomSeed() {
        return this.randomSeed;
    }

    /**
     * @param randomSeed the random seed value to be set
     */
    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }


    /**
     * Check if there are some stations nearby and get the nearest one
     *
     * @param position position of the checking point
     * @param stations the list containing all the stations
     * @return the nearest station for that checking point
     */
    protected Station checkNearby(Position position, ArrayList<Station> stations) {
        Station nearestSta = null;
        double shortestdis = -1.0;

        // Find the nearest station
        for (int i = 0; i < stations.size(); i++) {
            Position staPos = stations.get(i).getPosition();
            double dis = distance(position, staPos);

            // check if the station is in the linking range
            if (dis < 0.00025)
                // if the shortest distance has not been set or the coming distance is shorter than the current shortest distance
                if ((shortestdis < 0.0) || (dis < shortestdis)) {
                    nearestSta = stations.get(i);
                    shortestdis = dis;
                }
        }

        return nearestSta;
    }


    /**
     * Play the nextStep, updating the coins and power and output the step information to txt document
     *
     * @param stations the list containing all the stations
     */
    public void nextStep(ArrayList<Station> stations) {
        Direction nextDirection = decideDirection(stations);
        Position prePos = getPosition();

        // update the step count, power and position
        setStepCount(getStepCount() + 1);
        setPower(getPower() - 1.25);
        setPosition(this.position.nextPosition(nextDirection));

        // Output the txt document
        String filename = "./stateless-" + day + "-" + month + "-" + year + ".txt";
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


    /**
     * Check if the game goes to the end
     *
     * @return true - the game comes to end, false - the game can still continue
     */
    public boolean checkEnd() {
        return stepCount >= 250 || getPower() <= 0.0;
    }


    /**
     * Function to calculate the distance between two positions
     *
     * @param position1 the first position waiting to be compared
     * @param position2 the second position waiting to be compared
     * @return the Euclidean distance between the two positions
     */
    protected double distance(Position position1, Position position2) {
        return Math.sqrt(Math.pow(position1.latitude - position2.latitude, 2) + Math.pow(position1.longitude - position2.longitude, 2));
    }


    /**
     * Choose one direction from the 16 directions to go depending on taking a glance of the next step
     * The basic idea is firstly looking for positive direction, then neutral direction (with no station nearby), last negative direction
     *
     * @param stations the list containing all the stations
     * @return a direction to go in this step
     */
    private Direction decideDirection(ArrayList<Station> stations) {
        // positive direction is the direction to go which has the most positive value nearby
        // min negative direction is the directions which has the minimum negative value nearby
        Direction positiveDirection = null;
        Direction minNegtiveDirection = null;

        // neutral directions list contains the directions which have no station nearby
        ArrayList<Direction> neutralDirections = new ArrayList<Direction>();

        // these two variables store the max positive station of the max positive direction and min negative station of the min negative direction
        Station maxPositiveSta = null;
        Station minNegativeSta = null;

        for (Direction d : Direction.values()) {
            Position nextPos = this.position.nextPosition(d);
            Station nearestSta = checkNearby(nextPos, stations);

            // Check if it is still in play area
            if (!nextPos.inPlayArea()) continue;

            // See if the nearest station contain positive coins and power
            if (nearestSta == null || nearestSta.getExplored()) neutralDirections.add(d);
            else if (nearestSta.getPositive()) {
                // Choose the positive station with max amount of power, because I attach power with higher importance than coins
                if (positiveDirection == null) {
                    positiveDirection = d;
                    maxPositiveSta = nearestSta;
                } else if (nearestSta.getPower() > maxPositiveSta.getPower()) {
                    positiveDirection = d;
                    maxPositiveSta = nearestSta;
                }
            } else if (!nearestSta.getPositive() && nearestSta.getPower() < 0) {
                // get the negative station with min amount of power
                if (minNegativeSta == null) {
                    minNegtiveDirection = d;
                    minNegativeSta = nearestSta;
                } else if (nearestSta.getPower() > minNegativeSta.getPower()) {
                    minNegtiveDirection = d;
                    minNegativeSta = nearestSta;
                }
            }
        }


        if (positiveDirection == null) {
            if (neutralDirections.size() > 0) {
                // generate a random number from randomSeed to decide a direction if there is no positive direction
                int randomNum = rand.nextInt((neutralDirections.size()));
                return neutralDirections.get(randomNum);
            } else {
                // if there is no positive and neutral direction, the drone have to go to the min negative direction
                // update the coins and power for the drone
                setCoins(getCoins() + minNegativeSta.getCoins());
                setPower(getPower() + minNegativeSta.getPower());

                // update the coins and power for the station
                minNegativeSta.setCoins(minNegativeSta.getCoins() - (minNegativeSta.getCoins() - getCoins()));
                minNegativeSta.setPower(minNegativeSta.getPower() - (minNegativeSta.getPower() - getPower()));

                // Check if the amount of coins and power reach to 0
                if (getCoins() < 0) setCoins(0.0);
                if (getPower() < 0) setPower(0.0);

                // if the power and coins are collected, set it to be explored
                if (approxEq(minNegativeSta.getCoins(), 0.0) && approxEq(minNegativeSta.getPower(), 0.0))
                    minNegativeSta.setExplored(true);
                return minNegtiveDirection;
            }
        }

        // If we have a direction which can reach to a positive station, then choose this direction
        // update the coins and power for drone and the max positive station
        setCoins(getCoins() + maxPositiveSta.getCoins());
        setPower(getPower() + maxPositiveSta.getPower());
        if (maxPositiveSta != null) {
            maxPositiveSta.setCoins(0.0);
            maxPositiveSta.setPower(0.0);
            maxPositiveSta.setExplored(true);
        }
        return positiveDirection;
    }


    /**
     * compare two double number
     *
     * @param d0 the first double waiting to be compared
     * @param d1 the second double waiting to be compared
     * @return true - they are approximately equal, false - they are not approximately equal
     */
    protected boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }

}
