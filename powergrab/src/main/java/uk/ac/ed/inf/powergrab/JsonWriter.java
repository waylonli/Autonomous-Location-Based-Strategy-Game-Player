package uk.ac.ed.inf.powergrab;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


/**
 * A class to write GeoJson file
 * @author s1891340
 */
public class JsonWriter {

    /**
     * A method to run the game and write GeoJson file for stateless drone and stateful drone
     * @param drone can be stateful or stateless
     * @param stations the array list contains all the stations in the map
     * @param initialLatitude latitude of initial position
     * @param initialLongitude longitude of initial position
     * @param jsonList the GeoJson file containing the information of stations
     * @param year year of the map
     * @param month month of the map
     * @param day day of the map
     * @param type can be "stateful" or "stateless" which indicates the type of the drone
     */
    public static void writeJson(StatelessDrone drone, ArrayList<Station> stations,
                                 double initialLatitude, double initialLongitude,
                                 String jsonList, String year, String month, String day, String type) {

        JsonObject properties = new JsonObject();
        JsonArray coordinates = new JsonArray();
        JsonArray inicoordinate = new JsonArray();

        // Properties
        try {
            properties.addProperty("prop0", "value0");
            properties.addProperty("prop1", "\"this\":\"that\"");
            inicoordinate.add(initialLongitude);
            inicoordinate.add(initialLatitude);
            coordinates.add(inicoordinate);
        } catch (Exception e) {
            System.out.println("Properties JSon writing error!");
        }

        while (!drone.checkEnd()) {
            try {
                // Coordinates
                JsonArray newcoordinate = new JsonArray();
                drone.nextStep(stations);

                newcoordinate.add(drone.getPosition().longitude);
                newcoordinate.add(drone.getPosition().latitude);
                coordinates.add(newcoordinate);

            } catch (Exception e) {
                System.out.println("Position JSon writing error!");
            }

        }
        try {
            // Geometry
            JsonObject geometry = new JsonObject();
            geometry.addProperty("type", "LineString");
            geometry.add("coordinates", coordinates);

            // JsonList
            JsonObject list = new JsonObject();
            list.addProperty("type", "Feature");
            list.add("geometry", geometry);
            list.add("properties", properties);

            String path = StringEscapeUtils.unescapeJava(list.toString()).replaceFirst("\"\"", "{\"");
            jsonList = jsonList.substring(0, jsonList.length() - 2) + "," +
                    StringEscapeUtils.unescapeJava(path).replaceFirst("\"\"", "\"}");
        } catch (Exception e) {
            System.out.println("Other JSon error!");
        }

        // Define the ouput path and filename and output the json file
        String filename = "./" + type + "-" + day + "-" + month + "-" + year + ".geojson";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false)));
            out.write(jsonList + "]}");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Output GeoJson error!");
        }
    }

}
