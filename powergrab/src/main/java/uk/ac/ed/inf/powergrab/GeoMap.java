package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.FeatureCollection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeoMap {

    private URL mapURL = null;
    private HttpURLConnection conn = null;


    public GeoMap(String mapString) {

        // Check if the URL format is correct
        try {
            mapURL = new URL(mapString);
            conn = (HttpURLConnection) mapURL.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
        } catch (Exception e) {
            System.out.println("Illegal URL format, please double check!");
        }
    }


    public String buildMapSource() {
        StringBuilder sb = new StringBuilder();
        String line = null;

        // Using StringBuilder to turn InputStream to String
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            System.out.println("Cannot get InputStream!");
        }

        return sb.toString();
    }


    // Read the features from mapSource
    public FeatureCollection readFeature(String mapSource) {
        FeatureCollection fc = null;
        fc = FeatureCollection.fromJson(mapSource);
        return fc;
    }
}
