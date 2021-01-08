package testBot1;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Slanderer extends RobotPlayer {
    static void run() throws GameActionException {
        //just some random destination for now
        if (Util.moveNaive(new MapLocation(10010, 23942))){
            System.out.println("Reached destination!");
        }

    }
}
