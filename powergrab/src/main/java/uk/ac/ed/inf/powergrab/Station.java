package uk.ac.ed.inf.powergrab;

public class Station {
    private Position staPos;
    private double staCoins;
    private double staPower;
    private boolean staPositive;
    private boolean staExplored;

    public Station(Position staPos, double staCoins, double staPower) {
        setPosition(staPos);
        setCoins(staCoins);
        setPower(staPower);
        setExplored(false);
        if (staCoins > 0 || staPower > 0) setPositive(true);
        else setPositive(false);
    }

    public void setPosition(Position position) { this.staPos = position; }
    public Position getPosition() { return staPos; }

    public void setCoins(double coins) { this.staCoins = coins; }
    public double getCoins() { return this.staCoins; }

    public void setPower(double power) { this.staPower = power; }
    public double getPower() { return this.staPower; }

    public void setPositive(boolean positive) { this.staPositive = positive; }
    public boolean getPositive() { return this.staPositive; }

    public void setExplored(boolean explored) { this.staExplored = explored; }
    public boolean getExplored() { return this.staExplored; }
}
