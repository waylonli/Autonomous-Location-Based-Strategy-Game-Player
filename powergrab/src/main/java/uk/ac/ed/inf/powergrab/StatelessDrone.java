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
    private int positivenum = 0;
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

    // Function use to copy a stateless drone
    public StatelessDrone(StatelessDrone formalDrone) {
        setPosition(formalDrone.position);
        setCoins(formalDrone.getCoins());
        setPower(formalDrone.getPower());
        this.randomSeed = formalDrone.getRandomSeed();
        this.rand.setSeed(this.randomSeed);
        this.stepCount = 0;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    // Function to set position
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }
    // Function to get current position
    public Position getPosition() {
        return this.position;
    }

    // Function to set number of coins
    public void setCoins(double newCoins) {
        this.coins = newCoins;
    }
    // Function to get the current number of coins
    public double getCoins() {
        return this.coins;
    }

    // Function to set amount of power
    public void setPower(double newPower) {
        this.power = newPower;
    }
    // Function to get the current amount of power
    public double getPower() {
        return this.power;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    public int getStepCount() {
        return this.stepCount;
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }
    public int getRandomSeed() {
        return this.randomSeed;
    }

    // Check if there are some stations nearby, get the nearest one
    public Station checkNearby(Position position, ArrayList<Station> stations) {
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

    public void nextStep(ArrayList<Station> stations) {
        Direction nextDirection = decidePosition(stations);
        Position prePos = getPosition();

        this.stepCount += 1;
        setPower(getPower() - 1.25);
        setPosition(this.position.nextPosition(nextDirection));
        

        String filename = "/Users/waylon/Desktop/ILP Output/stateless-" + day + "-" + month + "-" + year + ".txt";
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true)));
            out.write(prePos.latitude + "," + prePos.longitude + ","
            + nextDirection + ","
            + getPosition().latitude + "," + getPosition().longitude + ","
            + getCoins() + "," + getPower() + "\n");
            out.flush();
            out.close();
        }
        catch (Exception a){
            System.out.println("Output error!");
        }

    }


    public boolean checkEnd() {
        if (stepCount >= 250 || getPower() <= 0.0) return true;
        return false;
    }


    // Function to calculate the distance between two positions
    private double distance(Position position1, Position position2) {
        return Math.sqrt(Math.pow(position1.latitude - position2.latitude, 2) + Math.pow(position1.longitude - position2.longitude, 2));
    }


    private Direction decidePosition(ArrayList<Station> stations) {
        Direction positiveDirection = null;
        ArrayList<Direction> neutralDirections = new ArrayList<Direction>();
        Station maxPositiveSta = null;

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
            }
        }

        // Generate a random number from randomSeed to decide a direction
        if (positiveDirection == null) {
            int randomNum = rand.nextInt((neutralDirections.size()));
            return neutralDirections.get(randomNum);
        }
        // If we have a direction which can reach to a positive station, then choose this direction
        if (!maxPositiveSta.getPositive()) System.out.println("Touch negative station!");
        else {
            this.positivenum ++;
            System.out.println("Touch positive station " + this.positivenum + "!");
        }
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
