package uk.ac.ed.inf.powergrab;

import javax.swing.plaf.synth.SynthSpinnerUI;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class StatefulDrone extends StatelessDrone {

    private Position position = null;
    private double coins;
    private double power;
    private int stepCount;
    private String year;
    private String month;
    private String day;
    private Station nextStation = null;
    private boolean lastSta = false;
    private double lastAngle = -10.0;
    private double preAngle = -10.0;
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
        this.lastSta = false;
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

        if (this.lastSta == false){
            this.nextStation = decideNextStation(stations);
        }
        
        double nextAngle = decideNextDirection(stations);
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
        ArrayList<Station> unexploredStas = new ArrayList<Station>(stations);
        unexploredStas.removeIf(Station -> (!Station.getPositive()));
        unexploredStas.removeIf(Station::getExplored);
        return getMaxStation(unexploredStas);
    }



    private Station getMaxStation(ArrayList<Station> unexploredStas) {
        // Define a score value to measure which station has the most profit and sort the station
        System.out.println(unexploredStas.size());
        if (unexploredStas.size() == 1) {
            this.lastSta = true;
        }
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

    private double decideNextDirection(ArrayList<Station> stations) {

        if (this.lastAngle >= 0) {
            this.lastAngle = (this.lastAngle + 180) % 360;
            return this.lastAngle;
        }

        Station nextSta = this.nextStation;
        // Find the direction to go towards our target station
        // See if this direction has a negative station
        // i is for avoiding infinite loops
        Direction nextDir = null;
        Position nextPos = null;
        Station nextNearestSta = null;
        double nextDirAngle = 0.0;

        // if there is a negative station nearby, we need to change another direction
        int i = 0;
        double step = 0.0;
        double nullAngle = -10.0;
        double shortestDis = 99999.9;

        while(i <= 15) {
            nextDirAngle = nextDirAngle + step;
            if (nextDirAngle == ((this.preAngle + 180) % 360)){
                if (step != 0.0) {
                    i++;
                    continue;
                }
                else {
                    step = 22.5;
                    i++;
                    continue;
                }
            }
            nextDir = radians.get(Math.toRadians(nextDirAngle));
            nextPos = getPosition().nextPosition(nextDir);
            nextNearestSta = checkNearby(nextPos, stations);

            if (nextPos.inPlayArea()) {
                if (nextNearestSta == null) {
                    double disToSta = distance(nextPos, this.nextStation.getPosition());
                    if (disToSta < shortestDis) {
                        shortestDis = disToSta;
                        nullAngle = nextDirAngle;
                    }
                }
                else if (nextNearestSta.getExplored()) {
                    double disToSta = distance(nextPos, this.nextStation.getPosition());
                    if (disToSta < shortestDis) {
                        shortestDis = disToSta;
                        nullAngle = nextDirAngle;
                    }
                }
                else if (nextNearestSta.getPositive()) {
                    this.preAngle = nextDirAngle;
                    return nextDirAngle;
                }
            }

            i++;
            step = 22.5;
        }
        this.preAngle = nullAngle;
        return nullAngle;
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

            if (this.lastSta == true) {
                this.lastAngle = angle;
            }
        }
        setPower(getPower() - 1.25);
        this.stepCount += 1;
    }


    // Check if the game goes to the end
    public boolean checkEnd() {
        return stepCount >= 250 || getPower() <= 0.0;
    }

//    public void meetNegative(){
//
//    }

}
