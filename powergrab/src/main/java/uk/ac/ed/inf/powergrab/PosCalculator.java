package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

public class PosCalculator {
    public static HashMap<Direction, double[]>  getPosChanges() {
        HashMap<Direction, double[]> map = new HashMap<Direction, double[]>();

        // For N, S, E, W
        map.put(Direction.N, new double[]{0.0003, 0});
        map.put(Direction.S, new double[]{-0.0003, 0});
        map.put(Direction.E, new double[]{0, 0.0003});
        map.put(Direction.W, new double[]{0, -0.0003});

        // For NE, SE, NW, SW
        map.put(Direction.NE, new double[]{(0.0003 * Math.sin(Math.toRadians(45))), (0.0003 * Math.cos(Math.toRadians(45)))});
        map.put(Direction.SE, new double[]{-(0.0003 * Math.sin(Math.toRadians(45))), (0.0003 * Math.cos(Math.toRadians(45)))});
        map.put(Direction.NW, new double[]{(0.0003 * Math.sin(Math.toRadians(45))), -(0.0003 * Math.cos(Math.toRadians(45)))});
        map.put(Direction.SW, new double[]{-(0.0003 * Math.sin(Math.toRadians(45))), -(0.0003 * Math.cos(Math.toRadians(45)))});

        // For NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW
        map.put(Direction.NNE, new double[]{(0.0003 * Math.cos(Math.toRadians(22.5))), (0.0003 * Math.sin(Math.toRadians(22.5)))});
        map.put(Direction.ENE, new double[]{(0.0003 * Math.sin(Math.toRadians(22.5))), (0.0003 * Math.cos(Math.toRadians(22.5)))});
        map.put(Direction.ESE, new double[]{-(0.0003 * Math.sin(Math.toRadians(22.5))), (0.0003 * Math.cos(Math.toRadians(22.5)))});
        map.put(Direction.SSE, new double[]{-(0.0003 * Math.cos(Math.toRadians(22.5))), (0.0003 * Math.sin(Math.toRadians(22.5)))});
        map.put(Direction.SSW, new double[]{-(0.0003 * Math.cos(Math.toRadians(22.5))), -(0.0003 * Math.sin(Math.toRadians(22.5)))});
        map.put(Direction.WSW, new double[]{-(0.0003 * Math.sin(Math.toRadians(22.5))), -(0.0003 * Math.cos(Math.toRadians(22.5)))});
        map.put(Direction.WNW, new double[]{(0.0003 * Math.sin(Math.toRadians(22.5))), -(0.0003 * Math.cos(Math.toRadians(22.5)))});
        map.put(Direction.NNW, new double[]{(0.0003 * Math.cos(Math.toRadians(22.5))), -(0.0003 * Math.sin(Math.toRadians(22.5)))});

        return map;
    }

    public static HashMap<Double, Direction> getGradians() {
        HashMap<Double, Direction> radians = new HashMap<>();
        Direction[] directions = new Direction[]{Direction.N, Direction.NNE, Direction.NE, Direction.ENE, Direction.E,
                Direction.ESE, Direction.SE, Direction.SSE, Direction.S, Direction.SSW, Direction.SW, Direction.WSW,
                Direction.W, Direction.WNW, Direction.NW, Direction.NNW};

        int i = 0;
        for (double r = 0.0; r <= Math.toRadians(360.0); r += Math.toRadians(22.5)){
            radians.put(r, directions[i]);
            i++;
        }

        return radians;
    }

}
