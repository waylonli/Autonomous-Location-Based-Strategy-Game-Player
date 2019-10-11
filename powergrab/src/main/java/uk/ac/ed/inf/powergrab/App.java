package uk.ac.ed.inf.powergrab;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
//            for (int k = 0; k <2; k++){

            // Properties
            JsonObject properties = new JsonObject();
            JsonArray coordinates = new JsonArray();
            JsonArray inicoordinate = new JsonArray();


            try{
                properties.addProperty("prop0", "value0");
                properties.addProperty("prop1", "\"this\":\"that\"");
                inicoordinate.add(initialLongitude);
                inicoordinate.add(initialLatitude);
                coordinates.add(inicoordinate);
            }
            catch (Exception e){
                System.out.println("Properties JSon writing error!");
            }

            while (!stateless.checkEnd()){
                try{
                    // Coordinates
                    JsonArray newcoordinate = new JsonArray();
                    stateless.nextStep(stations);
                    
                    newcoordinate.add(stateless.getPosition().longitude);
                    newcoordinate.add(stateless.getPosition().latitude);
                    coordinates.add(newcoordinate);

                }
                catch (Exception e){
                    System.out.println("Position JSon writing error!");
                }

            }
            try{
                // Geometry
                JsonObject geometry = new JsonObject();
                geometry.addProperty("type", "LineString");
                geometry.add("coordinates", coordinates);

                // JsonList
                JsonObject list = new JsonObject();
                list.addProperty("type", "Feature");
                list.add("geometry", geometry);
                list.add("properties", properties);

                String path = new String( StringEscapeUtils.unescapeJava(list.toString()).replaceFirst("\"\"","{\""));
                jsonList = jsonList.substring(0, jsonList.length()-2) + "," +
                        StringEscapeUtils.unescapeJava(path).replaceFirst("\"\"","\"}");
            }
            catch (Exception e){
                System.out.println("Other JSon error!");
            }
            writeJson(jsonList + "]}", year, month, day);
        }

    }

    public static void writeJson(String json, String year, String month, String day){
        String filename = "/Users/waylon/Desktop/ILP Output/stateless-" + day + "-" + month + "-" + year + ".geojson";
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false)));
            out.write(json);
            out.flush();
            out.close();
        }
        catch (Exception e){
            System.out.println("Output GeoJson error!");
        }
    }

}
