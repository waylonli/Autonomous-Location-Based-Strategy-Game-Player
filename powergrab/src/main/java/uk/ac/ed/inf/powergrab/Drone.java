package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

import java.util.ArrayList;

public interface Drone {

    // Function to update position
    public void updatePosition(Position newPosition);

    // Function to get current position
    public Position getPosition();

    // Function to update number of coins
    public void updateCoins(double newCoins);

    // Function to get the current number of coins
    public double getCoins();

    // Function to update amount of power
    public void updatePower(double newPower);

    // Function to get the current amount of power
    public double getPower();

    // Play next step
    public void nextStep(ArrayList<Feature> stations);

    // Check if there is a charging stations nearby
    public Feature checkNearby(Position position, ArrayList<Feature> stations);

}
