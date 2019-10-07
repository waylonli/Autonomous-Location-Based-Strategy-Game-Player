package uk.ac.ed.inf.powergrab;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        String mapString = "http://homepages.inf.ed.ac.uk/stg/powergrab/2019/01/01/powergrabmap.geojson";
        GeoMap map = new GeoMap(mapString);
        String mapSource = map.buildMapSource();
    }
}
