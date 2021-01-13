package testBot1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Team;

public class Slanderer extends RobotPlayer {
    // our enlightenment center's info (one politician spawned from)
    static int ecID;
    static MapLocation ecLoc;

    static final Team enemy = rc.getTeam().opponent();
    static MapLocation targetLoc;

    static void run() throws GameActionException {
        if (ecID == 0) {
            ecID = Util.getECID();
        }
        // TODO: Should i give slanderer a location or only a direction?
        Direction direction = Util.getScoutDirection(ecID);
        MapLocation newLocation = rc.getLocation();
        for (int i = 0; i < 3; i++) {
            newLocation = newLocation.add(direction);
        }
        if (rc.onTheMap(newLocation)) {
            // TODO: Make it so the slanderer is far enough away to avoid muckraker on edge
            // TODO: Fix the bug where if the edge is 8 squares away it will see that the first 4 are fine, and the next 4 are fine
            // and then hit edge
            Util.greedyPath(newLocation);
        } else {
            System.out.println("Done");
        }

    }



}
