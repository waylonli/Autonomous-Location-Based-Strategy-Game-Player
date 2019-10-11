package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;

public interface Drone {

    // Function to get current position
    Position getPosition();

    // Function to update position
    void setPosition(Position newPosition);

    // Function to get the current number of coins
    double getCoins();

    // Function to update number of coins
    void setCoins(double newCoins);

    // Function to get the current amount of power
    double getPower();

    // Function to update amount of power
    void setPower(double newPower);

    int getStepCount();

    // Play next step
    void nextStep(ArrayList<Station> stations);

    // Check if there is a charging stations nearby
    Station checkNearby(Position position, ArrayList<Station> stations);

    boolean checkEnd();

}
