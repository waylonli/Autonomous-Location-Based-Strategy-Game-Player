package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;


/**
 * Interface class to define some basic functions of drones
 * @author s1891340
 */
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


    // Play next step
    void nextStep(ArrayList<Station> stations);

    // Check if the game goes to end
    boolean checkEnd();

}
