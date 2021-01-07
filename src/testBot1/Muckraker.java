package testBot1;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {
    static boolean goingEast;
    static boolean goingNorth;

    static void run() throws GameActionException {
        MapLocation muckLeft = rc.adjacentLocation(Direction.WEST);
        MapLocation muckSouth = rc.adjacentLocation(Direction.SOUTH);
        RobotInfo leftRob = rc.senseRobotAtLocation(muckLeft);
        RobotInfo southRob = rc.senseRobotAtLocation(muckSouth);

        if (!(goingEast || goingNorth)) {
            if (leftRob != null && leftRob.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                goingEast = true;
            } else if (southRob != null && southRob.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                goingNorth = true;
            }
        }


        if (goingEast && tryMove(Direction.EAST)) {
            System.out.println("I moved east");
        } else if (goingNorth && tryMove(Direction.NORTH)){
            System.out.println("I moved north");
        }
    }
}
