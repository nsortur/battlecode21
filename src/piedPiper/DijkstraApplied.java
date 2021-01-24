package piedPiper;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import java.util.ArrayList;

public class DijkstraApplied extends RobotPlayer{

    static void useDijkstra(MapLocation endLoc) throws GameActionException {

        Graph g = new Graph(makeGraph());
        g.dijkstra(convertLocToString(rc.getLocation()));
        g.printPath(convertLocToString(endLoc));

    }

    static ArrayList<Graph.Edge> makeGraph() throws GameActionException {
        ArrayList<MapLocation> locations = new ArrayList<>();
        locations.add(rc.getLocation());
        for (Direction dir : directionsList) {
            locations.add(rc.getLocation().add(dir));
            locations.add(rc.getLocation().add(dir).add(dir));
        }

        ArrayList<Graph.Edge> edges = new ArrayList<>();
        for (MapLocation loc : locations) {
            edges = addToGraph(loc, edges);
        }
        return edges;
    }
    /**
     * Adds all surrounding locations to a list of edges
     *
     * @param loc the surrounding locations
     * @param edges list of edges
     * @throws GameActionException
     */
    static ArrayList<Graph.Edge> addToGraph(MapLocation loc, ArrayList<Graph.Edge> edges) throws GameActionException  {

        for (Direction dir : directionsList) {
            MapLocation newLoc = loc.add(dir);
            int val = getValueForPassability(rc.sensePassability(newLoc));
            edges.add(new Graph.Edge(convertLocToString(loc), convertLocToString(newLoc), val));

        }

        return edges;

    }

    static String convertLocToString(MapLocation loc) {
        return String.valueOf(loc.x) + String.valueOf(loc.y);
    }

    static int getValueForPassability(double passability) {
        if (passability >= 1) {
            return 1;
        } else if (passability >= 0.9) {
            return 2;
        } else if (passability >= 0.8) {
            return 3;
        } else if (passability >= 0.7) {
            return 4;
        } else if (passability >= 0.6) {
            return 5;
        } else if (passability >= 0.5) {
            return 6;
        } else if (passability >= 0.4) {
            return 7;
        } else if (passability >= 0.3) {
            return 8;
        } else if (passability >= 0.2) {
            return 9;
        } else if (passability >= 0.1) {
            return 10;
        } else if (passability >= 0) {
            return 11;
        }
        return 1;
    }

}
