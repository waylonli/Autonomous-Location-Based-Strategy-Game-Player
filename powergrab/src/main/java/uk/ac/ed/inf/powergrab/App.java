package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

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

        // Format the URL link to download the map
        String mapString =
            "http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day
                + "/powergrabmap.geojson";
        GeoMap map = new GeoMap(mapString);
        String mapSource = map.buildMapSource();

        ArrayList<Station> stations = new ArrayList<Station>();
        ArrayList<Point> passPoints = new ArrayList<Point>();
        FeatureCollection fc = map.readFeature(mapSource);
        String jsonList = fc.toJson();

        // Load all the stations to the station list
        for (int i = 0; i < 50; i++) {
            Feature newfea = fc.features().get(i);
            Point newPoint = (Point) newfea.geometry();
            Position staPos = new Position(newPoint.latitude(), newPoint.longitude());
            Station newSta = new Station(staPos, newfea.getProperty("coins").getAsDouble(), newfea.getProperty("power").getAsDouble());
            stations.add(newSta);
        }

        // See whether the user choose to play with stateless drone or stateful drone
        if (sc.hasNextInt()) {
            int randomSeed = sc.nextInt();
            StatelessDrone stateless = new StatelessDrone(initialLatitude, initialLongitude, randomSeed, year, month, day);
            JsonWriter statelessWriter = new JsonWriter();
            statelessWriter.writeStateless(stateless, stations, initialLatitude, initialLongitude, jsonList, year, month, day);
        }
        else if (sc.next().equals("stateful")){
            StatefulDrone stateful = new StatefulDrone(initialLatitude, initialLongitude, year, month, day);
            JsonWriter statefulWriter = new JsonWriter();
            statefulWriter.writeStateless(stateful, stations, initialLatitude, initialLongitude, jsonList, year, month,day);
        }
        
    }


}
