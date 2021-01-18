package testBot1;

import battlecode.common.*;

import java.util.Arrays;
import java.util.List;

public class Muckraker extends RobotPlayer {
    // our enlightenment center's ID (one muckraker spawned from)
    static int ecID;

    // direction for scout
    static Direction scoutDir;

    // location of spawning EC
    static MapLocation homeECLoc;

    static Integer[] scoutFlagValues = new Integer[]{11,12,13,14,15,16,17,18};
    static List<Integer> listOfScoutVals = Arrays.asList(scoutFlagValues);

    static boolean isScout = false;

    static MapLocation targetECLoc;
    static MapLocation ecLoc;

    static void run() throws GameActionException {
        if (ecID == 0) {
            ecID = Util.getECID();
            isScout();
        }

        if (isScout) {
            runScout(); // later on add if condition in case not scout
        } else {
            checkForTargetECLoc();
            //Util.greedyPathAndAvoid(Util.calculateNewLocationWithDirection(ecLoc.directionTo(targetECLoc),
            //        10, targetECLoc), targetECLoc, 100);
        }
    }

    static void isScout() throws GameActionException {
        if (listOfScoutVals.contains(Util.tryGetFlag(ecID))) {
            isScout = true;
        }
    }

    /**
     * Runs all scout activity, including getting the direction, killing slanderers,
     * checking to see if close to an EC or an edge
     * @throws GameActionException
     */
    static void runScout() throws GameActionException {
        if (scoutDir == null) {
            scoutDir = Util.getScoutDirection(ecID);
            // EC is 1 unit in opposite direction scout is heading at start
            homeECLoc = rc.getLocation().subtract(scoutDir);
        }
        tryKillSlanderer();
        if (isCloseToEnemyEC() || isCloseToNeutralEC() || isNextToEdge()) {
            // do code once at edge/found EC
            // System.out.println("Done with scout behavior");
        } else {
            Util.tryMove(scoutDir);
            // Util.moveNaive(new MapLocation(10026, 23926));
        }
    }


    /**
     * Checks to see if a robot is close to an EC, and if it is sets the robot's flag
     *
     * @return true if the robot is close to an ec
     * @throws GameActionException
     */
    static boolean isCloseToEnemyEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam().opponent()) {
                MapLocation ecLoc = robot.location;

                int x_offset = ecLoc.x - homeECLoc.x;
                int y_offset = ecLoc.y - homeECLoc.y;

                if (Util.trySetFlag(Util.encryptOffsets(x_offset, y_offset, 1))) // System.out.println("Flag set!");

                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if a robot is close to a neutral EC, and if it is sets the robot's flag
     *
     * @return true if the robot is close to an ec
     * @throws GameActionException
     */
    static boolean isCloseToNeutralEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == Team.NEUTRAL) {
                MapLocation ecLoc = robot.location;

                int x_offset = ecLoc.x - homeECLoc.x;
                int y_offset = ecLoc.y - homeECLoc.y;

                if (Util.trySetFlag(Util.encryptOffsets(x_offset, y_offset, 2))) // System.out.println("Flag set!");

                return true;
            }
        }
        return false;
    }

    /**
     * Kills a slanderer if in action radius
     *
     * @return returns true if killed a slanderer
     * @throws GameActionException
     */
    static boolean tryKillSlanderer() throws GameActionException {
        boolean killedSlanderer = false;

        RobotInfo[] robots = rc.senseNearbyRobots(40); // sense all robots in action radius
        for (RobotInfo robot: robots) {
            if (robot.type == RobotType.SLANDERER && robot.team != rc.getTeam()) {
                if (rc.canExpose(robot.location)) rc.expose(robot.location);
                // ** ADD FLAG THAT TELLS EC KILLED SLANDERER **
                killedSlanderer = true;
            }
        }
        return killedSlanderer;
    }

    /**
     * Checks if a scout is next to an edge
     *
     * @return true if it's next to an edge
     * @throws GameActionException
     */
    static boolean isNextToEdge() throws GameActionException{
        boolean hitEdge = false;

        for(Direction dir : cardDirections) {
            MapLocation adjLoc = rc.adjacentLocation(dir);
            hitEdge = !rc.onTheMap(adjLoc);

            if (hitEdge) {
                int x_offset = rc.getLocation().x - homeECLoc.x;
                int y_offset = rc.getLocation().y - homeECLoc.y;

                if (Util.trySetFlag(Util.encryptOffsets(x_offset, y_offset, 0))) // System.out.println("Flag set!");
                break;
            }
        }

        return hitEdge;
    }

    /**
     * Checks for target ec loc
     * @throws GameActionException
     */
    static void checkForTargetECLoc() throws GameActionException{
        if (targetECLoc == null) {
            int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

            if (ecFlagInfo[2] == 9) {
                ecLoc = Util.locationOfFriendlyEC();
                targetECLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
            }
        }
    }
}
