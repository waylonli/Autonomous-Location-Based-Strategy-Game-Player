package uk.ac.ed.inf.powergrab;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.swing.plaf.synth.SynthSpinnerUI;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.*;

public class StatefulDrone extends StatelessDrone {

    private Position position = null;
    private double coins;
    private double power;
    private int singleStepCount;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Station nextStation = null;
    private boolean lastSta = false;
    private double lastAngle = -10.0;
    private double preAngle = -10.0;
    private Random rand = new Random();
    public static final HashMap<Double, Direction> radians = PosCalculator.getGradians();
    ArrayList<Station> unexploredStas = null;


    // Initilaze the stateful drone
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
        Position prePos = getPosition();

        if (this.lastSta == false && this.nextStation == null){
            this.nextStation = decideNextStation(stations);
        }
        
        double nextAngle = decideNextDirection(stations);
        this.preAngle = nextAngle;
        finishStep(nextAngle, stations);

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



    // TODO implement if no more positive station
    private Station decideNextStation(ArrayList<Station> stations) {
        this.unexploredStas = new ArrayList<Station>(stations);
        this.unexploredStas.removeIf(Station -> (!Station.getPositive()));
        this.unexploredStas.removeIf(Station::getExplored);
        return getStation(unexploredStas, true);
    }



    private Station getStation(ArrayList<Station> unexploredStas, boolean min) {
        // Define a score value to measure which station has the most profit and sort the station
        if (unexploredStas.size() == 1) {
            this.lastSta = true;
        }
        Station returnSta = null;
        System.out.println(unexploredStas.size());
        if (min) {
             returnSta = Collections.min(unexploredStas, new Comparator<Station> (){
                public int compare (Station sta1, Station sta2) {
                    double score1 = distance(sta1.getPosition(), getPosition());
                    double score2 = distance(sta2.getPosition(), getPosition());
                    if (approxEq(score1, score2)) return 0;
                    else if (score1 > score2) return 1;
                    return -1;
                }
            });
        }
        else {
            returnSta = Collections.max(unexploredStas, new Comparator<Station> (){
                public int compare (Station sta1, Station sta2) {
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



    // Check if there are some stations nearby, get the nearest one
    protected Station checkNearby(Position position, ArrayList<Station> stations) {
        Station nearestSta = null;
        double shortestdis = -1.0;

        // Find the nearest station
        for (int i = 0; i < stations.size(); i++) {

            Position staPos = stations.get(i).getPosition();
            double dis = distance(position, staPos);
            if (dis < 0.00025)
                if ((shortestdis < 0.0) || (dis < shortestdis)) {
                    nearestSta = stations.get(i);
                    shortestdis = dis;
                }
        }
        return nearestSta;
    }

    private double decideNextDirection(ArrayList<Station> stations) {

        if (this.lastAngle >= 0) {
            this.lastAngle = (this.lastAngle + 180) % 360;
            return this.lastAngle;
        }

        Station nextSta = this.nextStation;
        double latSta = nextSta.getPosition().latitude;
        double longSta = nextSta.getPosition().longitude;
        double nextAng = 0;
        double shortest = 99999;
        Position centre = new Position(55.944425, -3.188396);
        Station cenSta = new Station(centre,1.0, 1.0);

        System.out.println(this.singleStepCount);
        if ((this.singleStepCount != 0) && (this.singleStepCount % 8 == 0) && this.lastAngle < 0) {
            int randomNum = rand.nextInt(15);
            nextAng = randomNum * 22.5;
        }
        else if ((this.singleStepCount > 24) && ((!lastSta) ||
                (this.nextStation.getPosition().latitude == centre.latitude
                        && this.nextStation.getPosition().longitude == centre.longitude))) {
            System.out.println("hhh");
            this.nextStation = getStation(this.unexploredStas, false);
            this.singleStepCount = 0;
        }
        else if ((this.singleStepCount > 24) && (lastSta)) {
            this.nextStation = cenSta;
            singleStepCount -= 8;
        }
        else {
            for (double r = 0; r < 360; r+=22.5) {
                Position tempPos = getPosition().nextPosition(radians.get(Math.toRadians(r)));
                double dis = distance(tempPos, this.nextStation.getPosition());
                if (dis < shortest) {
                    nextAng = r;
                    shortest = dis;
                }
            }
        }


        ArrayList<Double> tryList = createTryList(nextAng);
        int i = 0;

        while(i < 16) {

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
                i++;
                continue;
            }
            else if (nextNearest == null) {
                return nextAng;
            }
            else if (nextNearest.getExplored() || nextNearest.getPositive()) {
                return nextAng;
            }
            else if (!nextNearest.getPositive()) {
                i++;
                continue;
            }
        }

        return nextAng;








//        =================================================
//        Station nextSta = this.nextStation;
        // Find the direction to go towards our target station
        // See if this direction has a negative station
        // i is for avoiding infinite loops
//        Direction nextDir = null;
//        Position nextPos = null;
//        Station nextNearestSta = null;
//        double nextDirAngle = 0.0;
//
//        // if there is a negative station nearby, we need to change another direction
//        int i = 0;
//        double step = 0.0;
//        double nullAngle = -10.0;
//        double shortestDis = 99999.9;
//
//        while(i <= 15) {
//            nextDirAngle = nextDirAngle + step;
//            if (nextDirAngle == ((this.preAngle + 180) % 360)){
//                if (step != 0.0) {
//                    i++;
//                    continue;
//                }
//                else {
//                    step = 22.5;
//                    i++;
//                    continue;
//                }
//            }
//            nextDir = radians.get(Math.toRadians(nextDirAngle));
//            nextPos = getPosition().nextPosition(nextDir);
//            nextNearestSta = checkNearby(nextPos, stations);
//
//            if (nextPos.inPlayArea()) {
//                if (nextNearestSta == null) {
//                    double disToSta = distance(nextPos, this.nextStation.getPosition());
//                    if (disToSta < shortestDis) {
//                        shortestDis = disToSta;
//                        nullAngle = nextDirAngle;
//                    }
//                }
//                else if (nextNearestSta.getExplored()) {
//                    double disToSta = distance(nextPos, this.nextStation.getPosition());
//                    if (disToSta < shortestDis) {
//                        shortestDis = disToSta;
//                        nullAngle = nextDirAngle;
//                    }
//                }
//                else if (nextNearestSta.getPositive()) {
//                    this.preAngle = nextDirAngle;
//                    return nextDirAngle;
//                }
//            }
//
//            i++;
//            step = 22.5;
//        }
//        if (nullAngle < 0) {
//            nullAngle = (this.preAngle + 180) % 360;
//            return nullAngle;
//        }
//        this.preAngle = nullAngle;
//        return nullAngle;
    }


    private boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }


    public void finishStep(double angle,  ArrayList<Station> stations) {

        // Update coordinate
        setPosition(getPosition().nextPosition(radians.get(Math.toRadians(angle))));
        Station nearestSta = checkNearby(getPosition(), stations);

        if((nearestSta != null) && (!nearestSta.getExplored())) {
            if (nearestSta.getPositive() == false){
                System.out.println("Negative!!!!");
            }


            this.singleStepCount = -1;

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
            if (this.lastSta) {
                this.lastAngle = angle;
            }
            this.nextStation = null;
        }

        setPower(getPower() - 1.25);
        this.singleStepCount += 1;
        if (this.lastAngle < 0) {
            this.stepCount += 1;
        }

    }


    // Check if the game goes to the end
    public boolean checkEnd() {

        return stepCount >= 250 || getPower() <= 0.0;
    }

    private ArrayList<Double> createTryList(double angle) {
        ArrayList<Double> output = new ArrayList<Double>();

        output.add(angle);
        double step = 0;
        Position centre = new Position(55.944425, -3.188396);

        Position plusPos = getPosition().nextPosition(radians.get(Math.toRadians((angle + 22.5) % 360)));
        Position minusPos = getPosition().nextPosition(radians.get(Math.toRadians((angle + 360 - 22.5) % 360)));

        int sign = 1;
        if (distance(plusPos, centre) + 3*distance(plusPos, this.nextStation.getPosition()) < distance(minusPos, centre) + 3*distance(minusPos, this.nextStation.getPosition())) {
            sign = 1;
        }
        else sign = -1;

        while (output.size() < 17) {

            output.add((angle + 360 + sign*(step + 22.5)) % 360);
            output.add((angle + 360 + sign*(step + 45)) % 360);
            output.add((angle + 360 + sign*(step + 67.5)) % 360);
            output.add((angle + 360 + sign*(step + 90)) % 360);
            output.add((angle + 360 - sign*(step + 22.5)) % 360);
            output.add((angle + 360 - sign*(step + 45)) % 360);
            output.add((angle + 360 - sign*(step + 67.5)) % 360);
            output.add((angle + 360 - sign*(step + 90)) % 360);
            step += 90;

        }
        output.remove(16);
        return output;
    }

//    public void meetNegative(){
//
//    }

}
