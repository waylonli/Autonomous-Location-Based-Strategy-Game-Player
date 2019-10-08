package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String day = sc.next();
        String month = sc.next();
        String year = sc.next();
        double initialLatitude = sc.nextDouble();
        double initialLongitude = sc.nextDouble();

        String mapString =
            "http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day
                + "/powergrabmap.geojson";
        GeoMap map = new GeoMap(mapString);
        String mapSource = map.buildMapSource();
        final ArrayList<Feature> stations = new ArrayList<Feature>();
        FeatureCollection fc = map.readFeature(mapSource);

        // Load all the stations to the station list
        for (int i = 0; i < 50; i++) {
            stations.add(fc.features().get(i));
        }

        if (sc.hasNextInt()) {
            int randomSeed = sc.nextInt();
            StatelessDrone stateless = new StatelessDrone(initialLatitude, initialLongitude, randomSeed);
            stateless.nextStep(stations);
        }

    }
}
