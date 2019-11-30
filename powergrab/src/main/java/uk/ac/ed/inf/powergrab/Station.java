package uk.ac.ed.inf.powergrab;

/**
 * Station class is used to store the information of a station
 *
 * @author s1891340
 */
public class Station {

    /**
     * @param staPos the position of the station
     * @param staCoins the number of coins of the station
     * @param staPower the amount of power of the station
     * @param staPositive true - the station contains positive coins and power, false - contains negative coins and power
     * @param staExplored true - all the coins and power are taken away by a drone, false - the station still has coins and power
     */
    private Position staPos;
    private double staCoins;
    private double staPower;
    private boolean staPositive;
    private boolean staExplored;

    /**
     * Constructor of the class
     *
     * @param staPos   the position of the station
     * @param staCoins the number of coins of the station
     * @param staPower the amount of power of the station
     */
    public Station(Position staPos, double staCoins, double staPower) {
        setPosition(staPos);
        setCoins(staCoins);
        setPower(staPower);
        setExplored(false);
        if (staCoins > 0 || staPower > 0) setPositive(true);
        else setPositive(false);
    }


    /**
     * @param position position to set
     */
    public void setPosition(Position position) {
        this.staPos = position;
    }

    /**
     * @return get the position of the station
     */
    public Position getPosition() {
        return staPos;
    }


    /**
     * @param coins coins to set
     */
    public void setCoins(double coins) {
        this.staCoins = coins;
    }

    /**
     * @return get the number of coins of the station
     */
    public double getCoins() {
        return this.staCoins;
    }


    /**
     * @param power the amount of power to set
     */
    public void setPower(double power) {
        this.staPower = power;
    }

    /**
     * @return get the amount of power
     */
    public double getPower() {
        return this.staPower;
    }


    /**
     * @param positive the value to set for positive or not
     */
    public void setPositive(boolean positive) {
        this.staPositive = positive;
    }

    /**
     * @return whether this is a positive station
     */
    public boolean getPositive() {
        return this.staPositive;
    }


    /**
     * @param explored the value to set for already being explored or not
     */
    public void setExplored(boolean explored) {
        this.staExplored = explored;
    }

    /**
     * @return whether this is an explored station
     */
    public boolean getExplored() {
        return this.staExplored;
    }
}
