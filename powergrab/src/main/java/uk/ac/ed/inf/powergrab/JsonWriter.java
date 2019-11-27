package uk.ac.ed.inf.powergrab;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class JsonWriter {

    public static void writeStateless(StatelessDrone drone, ArrayList<Station> stations,
                                 double initialLatitude, double initialLongitude,
                                 String jsonList, String year, String month, String day){

        JsonObject properties = new JsonObject();
        JsonArray coordinates = new JsonArray();
        JsonArray inicoordinate = new JsonArray();

        // Properties
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

        while (!drone.checkEnd()){
            try{
                // Coordinates
                JsonArray newcoordinate = new JsonArray();
                drone.nextStep(stations);
                
                System.out.println(drone.getPosition());
                newcoordinate.add(drone.getPosition().longitude);
                newcoordinate.add(drone.getPosition().latitude);
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

        // Define the ouput path and filename
        String filename = "/Users/waylon/Desktop/ILP Output/stateless-" + day + "-" + month + "-" + year + ".geojson";
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false)));
            out.write(jsonList + "]}");
            out.flush();
            out.close();
        }
        catch (Exception e){
            System.out.println("Output GeoJson error!");
        }
    }

}
