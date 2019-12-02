package uk.ac.ed.inf.powergrab;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;


/**
 * Class for stateful drone, defining the decision of direction and movement strategy
 * @author s1891340
 */
public class StatefulDrone extends StatelessDrone {
    /**
     * @param position position of the drone
     * @param coins number of coins of the drone
     * @param power amount of power of the drone
     * @param lastAngle the last step's angle before reaching to the last positive station
     * @param preAngle the previous step's angle
     * @param singleStepCount the step count for finding single station
     * @param stepCount step count for the entire game
     * @param year year of the map
     * @param month month of the map
     * @param day day of the map
     * @param nextStation the next station to go
     * @param lastSta if the station the drone is going to reach is the last positive station in the map
     * @param rand the random object to get random number
     * @param radians a hashmap which we can get directions by offering angles, it is created in PosCalculator class
     * @param unexploredStas the positive stations which have not been explored
     */
    private Position position = null;
    private double coins;
    private double power;
    private double lastAngle = -10.0;
    private double preAngle = -10.0;
    private int singleStepCount;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Station nextStation = null;
    private boolean lastSta = false;
    private Random rand = new Random();
    public static final HashMap<Double, Direction> radians = PosCalculator.getGradians();
    private ArrayList<Station> unexploredStas = null;


    /**
     * Constructor of stateful drone class
     * @param initialLatitude the initial latitude of the stateful drone
     * @param initialLongitude the initial longitude of the stateful drone
     * @param seed the initial random seed of the stateful drone
     * @param year the year of the map
     * @param month the month of the map
     * @param day the day of the map
     */
    public StatefulDrone(double initialLatitude, double initialLongitude, int seed, String year, String month, String day) {
        setPosition(new Position(initialLatitude, initialLongitude));
        setCoins(0.0);
        setPower(250.0);
        setStepCount(0);
        this.year = year;
        this.month = month;
        this.day = day;
        this.lastSta = false;
        this.singleStepCount = 0;
        this.rand.setSeed(seed);
    }

    /**
     * @return the position of the drone
     */
    @Override
    public Position getPosition() {
        return this.position;
    }

    /**
     * @param newPosition the position to be set for the drone
     */
    @Override
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }


    /**
     * @return the coins of the drone
     */
    @Override
    public double getCoins() {
        return this.coins;
    }

    /**
     * @param newCoins the coins number to be set for the drone
     */
    @Override
    public void setCoins(double newCoins) {
        this.coins = newCoins;
    }


    /**
     * @return the power of the drone
     */
    @Override
    public double getPower() {
        return this.power;
    }

    /**
     * @param newPower the amount of power to be set for the drone
     */
    @Override
    public void setPower(double newPower) {
        this.power = newPower;
    }


    /**
     * @return the step count of the drone
     */
    @Override
    public int getStepCount() {
        return this.stepCount;
    }

    /**
     * @param stepCount the step count number to be set for the drone
     */
    @Override
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }


    /**
     * Play the nextStep, updating the coins and power and output the step information to txt document
     * 1. see if the drone needs to update next station to go
     * 2. decide the next direction based on the position of next station
     * 3. finish step, check if there's station nearby after moving, updating the coins, power and step count
     * 4. append information to the txt file
     * @param stations the list containing all the stations
     */
    @Override
    public void nextStep(ArrayList<Station> stations) {
        Position prePos = getPosition();

        // update the next station to go if we still have positive station and the drone does not have a target station
        if (this.lastSta == false && this.nextStation == null) {
            this.nextStation = decideNextStation(stations);
        }

        double nextAngle = decideNextDirection(stations);
        // update the pre angle information
        this.preAngle = nextAngle;
        finishStep(nextAngle, stations);

        // output txt file
        String filename = "./stateful-" + day + "-" + month + "-" + year + ".txt";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true)));
            out.write(prePos.latitude + "," + prePos.longitude + ","
                    + radians.get(Math.toRadians(nextAngle)) + ","
                    + getPosition().latitude + "," + getPosition().longitude + ","
                    + getCoins() + "," + getPower() + "\n");
            out.flush();
            out.close();
        } catch (Exception a) {
            System.out.println("Output error!");
        }
    }


    /**
     * decide the next station to go
     * @param stations the list containing all the stations
     * @return the next station to go
     */
    private Station decideNextStation(ArrayList<Station> stations) {
        this.unexploredStas = new ArrayList<Station>(stations);
        // use a filter to get positive unexplored stations
        this.unexploredStas.removeIf(Station -> (!Station.getPositive()));
        this.unexploredStas.removeIf(Station::getExplored);

        // call getStation() to get the target station
        return getStation(unexploredStas, true);
    }


    /**
     * get the target station based on distance
     * @param unexploredStas the positive stations which have not been explored
     * @param min true - get the station with shortest distance, false - get the station with longest distance
     * @return the station to go
     */
    private Station getStation(ArrayList<Station> unexploredStas, boolean min) {
        if (unexploredStas.size() == 1) {
            this.lastSta = true;
        }

        Station returnSta = null;

        if (min) {
            // return the station with shortest distance if min == true
            returnSta = Collections.min(unexploredStas, new Comparator<Station>() {
                public int compare(Station sta1, Station sta2) {
                    double score1 = distance(sta1.getPosition(), getPosition());
                    double score2 = distance(sta2.getPosition(), getPosition());
                    if (approxEq(score1, score2)) return 0;
                    else if (score1 > score2) return 1;
                    return -1;
                }
            });
        } else {
            // return the station with longest distance if min == false
            returnSta = Collections.max(unexploredStas, new Comparator<Station>() {
                public int compare(Station sta1, Station sta2) {
                    double score1 = distance(sta1.getPosition(), getPosition());
                    double score2 = distance(sta2.getPosition(), getPosition());
                    if (approxEq(score1, score2)) return 0;
                    else if (score1 > score2) return 1;
                    return -1;
                }
            });
        }
        return returnSta;
    }


    /**
     * Choose one direction from the 16 directions to go depending on taking a glance of the next step
     * The basic idea is:
     * 1. if all the positive stations are explored, the drone will repeatedly go to the previous station and go back to last position
     *
     * 2. decide a first trying direction d
     * 2.1 check if the drone gets stuck while trying to reach a target station, every time it takes 8 steps, try a random direction d
     * 2.2 if the drone spends over 24 steps for one station, and it is not the last station, try the furthest station instead
     * 2.3 if the drone spends over 24 steps for one station, and it is the last station, go several steps towards the centre, then try again
     * 2.4 if it does not get stuck, choose the direction d which can be closer to the target station
     *
     * 3. create a trylist based on d, deciding the order of trying other directions if d is not feasible
     * (the direction which goes back to the previous position is put to the end of the list because the drone tries not to go back unless there is no better option)
     *
     * 4. loop the trylist, see if there is positive station or negative station nearby, return the first feasible angle
     *
     * 5. if there is no feasible angle, return the direction angle which has the minimum negative station nearby
     * @param stations the list containing all the stations
     * @return the angle of direction to move forward
     */
    private double decideNextDirection(ArrayList<Station> stations) {

        if (this.lastAngle >= 0) {
            this.lastAngle = (this.lastAngle + 180) % 360;
            return this.lastAngle;
        }

        double nextAng = 0;
        double minNegativeAng = -1;
        double minNegativePow = 99999;
        double shortest = 99999;
        Position centre = new Position(55.944425, -3.188396);
        Station cenSta = new Station(centre, 1.0, 1.0);

        // decide the first trying angle like what is explained in 2
        if ((this.singleStepCount != 0) && (this.singleStepCount % 8 == 0) && this.lastAngle < 0) {
            // check if the drone gets stuck while trying to reach a target station, every time it takes 8 steps, try a random direction d
            int randomNum = rand.nextInt(15);
            nextAng = randomNum * 22.5;
        } else if ((this.singleStepCount > 24) && ((!lastSta) ||
                (this.nextStation.getPosition().latitude == centre.latitude
                        && this.nextStation.getPosition().longitude == centre.longitude))) {
            // if the drone spends over 24 steps for one station, and it is not the last station, try the furthest station instead
            this.nextStation = getStation(this.unexploredStas, false);
            this.singleStepCount = 0;
        } else if ((this.singleStepCount > 24) && (lastSta)) {
            // if the drone spends over 24 steps for one station, and it is the last station, go several steps towards the centre, then try again
            this.nextStation = cenSta;
            singleStepCount -= 8;
        } else {
            // if it does not get stuck, choose the direction d which can be closer to the target station
            for (double r = 0; r < 360; r += 22.5) {
                Position tempPos = getPosition().nextPosition(radians.get(Math.toRadians(r)));
                double dis = distance(tempPos, this.nextStation.getPosition());
                if (dis < shortest) {
                    nextAng = r;
                    shortest = dis;
                }
            }
        }

        // create the try list
        ArrayList<Double> tryList = createTryList(nextAng);
        int i = 0;

        while (i < 16) {

            // put the angle which goes back to previous position to the end of the list
            if ((tryList.get(i) == (this.preAngle + 180) % 360) && (i < 15)) {
                tryList.remove(i);
                tryList.add((this.preAngle + 180) % 360);
                continue;
            }
            nextAng = tryList.get(i);

            Direction nextDir = radians.get(Math.toRadians(nextAng));
            Position nextPos = getPosition().nextPosition(nextDir);
            Station nextNearest = checkNearby(nextPos, stations);

            if (!nextPos.inPlayArea()) {
                // if this angle leads to a position outside the map
                i++;
                continue;
            } else if (nextNearest == null) {
                // if there is no station nearby, it is feasible
                return nextAng;
            } else if (nextNearest.getExplored() || nextNearest.getPositive()) {
                // if there is an explored station or positive station nearby, it is feasible as well
                return nextAng;
            } else if (!nextNearest.getPositive()) {
                // if there is a negative station nearby, record the min negative angle and try the next one
                if (nextNearest.getPower() < minNegativePow) {
                    minNegativePow = nextNearest.getPower();
                    minNegativeAng = nextAng;
                }
                i++;
                continue;
            }
        }

        // if all the directions have negative station nearby, return the angle with minimum negative station nearby
        return minNegativeAng;
    }


    /**
     * Finish the step. Detect the nearby station, update coins, power and step count.
     * @param angle the angle of direction to move forward
     * @param stations the list containing all the stations
     */
    private void finishStep(double angle, ArrayList<Station> stations) {

        // Update coordinate and get the nearby station
        setPosition(getPosition().nextPosition(radians.get(Math.toRadians(angle))));
        Station nearestSta = checkNearby(getPosition(), stations);


        if ((nearestSta != null) && (!nearestSta.getExplored())) {
            // update the coins and power
            setCoins(getCoins() + nearestSta.getCoins());
            setPower(getPower() + nearestSta.getPower());

            // give information for debugging if the drone touches a negative station
            if (!nearestSta.getPositive()) {
                System.out.println("Negative!!!!");

                // Update the coins, power and exploration for the connecting station
                double staCoins = nearestSta.getCoins();
                double staPow = nearestSta.getPower();
                if (Math.abs(staCoins) > getCoins()) {
                    nearestSta.setCoins(staCoins + getCoins());
                }else nearestSta.setCoins(0);

                if (Math.abs(staPow) > getPower()) {
                    nearestSta.setPower(staPow + getPower());
                }else nearestSta.setPower(0);
            }else {
                nearestSta.setCoins(0);
                nearestSta.setPower(0);
            }

            // reset the step count for reaching a single station when linking to a nearby station
            this.singleStepCount = -1;

            // the drone cannot contain negative number of coins and power
            if (getCoins() < 0.0) setCoins(0.0);
            if (getPower() < 0.0) setPower(0.0);

            // if the drone already took all the coins and power, set the station to be explored
            if (approxEq(nearestSta.getCoins(), 0.0) && approxEq(nearestSta.getPower(), 0.0)) {
                nearestSta.setExplored(true);
            }

            // if all the positive stations are explored, set value for last angle
            if (this.lastSta) {
                this.lastAngle = angle;
            }

            this.nextStation = null;
        }

        // update the power for step cost and step count
        setPower(getPower() - 1.25);
        setStepCount(getStepCount() + 1);

        // update step count for single station before the drone finishes exploring all the positive station
        if (this.lastAngle < 0) {
            this.singleStepCount += 1;
        }
    }



    /**
     * Check if the game goes to the end
     *
     * @return true - the game comes to end, false - the game can still continue
     */
    @Override
    public boolean checkEnd() {
        return this.stepCount >= 250 || getPower() <= 0.0;
    }


    /**
     * Create the try list for deciding next direction
     * Need to decide whether the drone tries the angle in clockwise or anticlockwise based on:
     * 1. avoid being stuck near the map boundary (try getting closer to centre)
     * 2. try to get closer to the target station
     * @param angle the first trying angle
     * @return a list containing the ordered 16 trying angles
     */
    private ArrayList<Double> createTryList(double angle) {
        ArrayList<Double> output = new ArrayList<Double>();

        // firstly add the first trying angle to the list
        output.add(angle);
        double step = 0;

        Position centre = new Position(55.944425, -3.188396);
        Position plusPos = getPosition().nextPosition(radians.get(Math.toRadians((angle + 22.5) % 360)));
        Position minusPos = getPosition().nextPosition(radians.get(Math.toRadians((angle + 360 - 22.5) % 360)));

        int sign = 1;

        // decide clockwise or anticlockwise by comparing the distance to centre and target station
        // then assign value to sign variable
        if (distance(plusPos, centre) + 3 * distance(plusPos, this.nextStation.getPosition()) <
                distance(minusPos, centre) + 3 * distance(minusPos, this.nextStation.getPosition())) {
            sign = 1;
        } else sign = -1;

        // create the try list, append 4 angles for each of the clockwise and anticlockwise directions
        while (output.size() < 17) {
            output.add((angle + 360 + sign * (step + 22.5)) % 360);
            output.add((angle + 360 + sign * (step + 45)) % 360);
            output.add((angle + 360 + sign * (step + 67.5)) % 360);
            output.add((angle + 360 + sign * (step + 90)) % 360);
            output.add((angle + 360 - sign * (step + 22.5)) % 360);
            output.add((angle + 360 - sign * (step + 45)) % 360);
            output.add((angle + 360 - sign * (step + 67.5)) % 360);
            output.add((angle + 360 - sign * (step + 90)) % 360);
            step += 90;
        }

        // remove the last redundant angle
        output.remove(16);
        return output;
    }

}
