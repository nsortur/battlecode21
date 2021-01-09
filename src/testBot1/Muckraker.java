package testBot1;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {
    // our enlightenment center's ID (one muckraker spawned from)
    static int ecID;

    // flags for scouts
    static final int[] directionCode = {
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
    };
    // direction for scout
    static Direction scoutDir;

    static void run() throws GameActionException {
        runScout(); // later on add if condition in case not scout
    }

    /**
     * Runs all scout activity, including getting the direction, killing slanderers,
     * checking to see if close to an EC or an edge
     * @throws GameActionException
     */
    static void runScout() throws GameActionException {
        if (scoutDir == null) {
            scoutDir = getScoutDirection();
        }
        killSlanderer();
        if (isCloseToEC() || Util.isNextToEdge()) {
            // do code once at edge/found EC
            System.out.println("Done with scout behavior");
        } else {
            Util.tryMove(scoutDir);
            // Util.moveNaive(new MapLocation(10026, 23926));
        }
    }

    /**
     * Calculates direction scout needs to move in
     * @return a direction
     * @throws GameActionException
     */
    static Direction getScoutDirection() throws GameActionException {
        ecID = getECID();
        int ecFlag = -1;
        if (rc.canGetFlag(ecID)) {
            ecFlag = rc.getFlag(ecID);
        } else {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Cannot get flag");
        }
        switch (ecFlag) {
            case 11:
                return Direction.NORTH;
            case 12:
                return Direction.NORTHEAST;
            case 13:
                return Direction.EAST;
            case 14:
                return Direction.SOUTHEAST;
            case 15:
                return Direction.SOUTH;
            case 16:
                return Direction.SOUTHWEST;
            case 17:
                return Direction.WEST;
            case 18:
                return Direction.NORTHWEST;
            default:
                throw new IllegalStateException("Unexpected value: " + ecFlag);
        }
    }


    /**
     * Checks to see if a robot is close to an EC, and if it is sets the robot's flag
     *
     * @return true if the robot is close to an ec
     * @throws GameActionException
     */
    static boolean isCloseToEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team != rc.getTeam()) {
                // divide by 100 is temporary secret code, make more complex later
                rc.setFlag(concat(robot.getLocation().x, robot.getLocation().y) / 100);
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

    static boolean killSlanderer() throws GameActionException {
        boolean killedSlanderer = false;

        RobotInfo[] robots = rc.senseNearbyRobots(40); // sense all robots in action radius
        for (RobotInfo robot: robots) {
            if (robot.type == RobotType.SLANDERER && robot.team != rc.getTeam()) {
                if (rc.canExpose(robot.location)) rc.expose(robot.location);
                killedSlanderer = true;
            }
        }
        return killedSlanderer;
    }

    /**
     * Gets the id of an EC
     *
     * @return gets the EC ID that the robot spawned from
     * @throws GameActionException
     */

    static int getECID() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.ID;
            }
        }
        throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "No enlightenmnet center");
    }

    /**
     * Concatenates 2 integers to be 1 integer
     *
     * @param a integer
     * @param b integer
     * @return ab, concatenated integer
     */
    static int concat(int a, int b) {

        // Convert both the integers to string
        String s1 = Integer.toString(a);
        String s2 = Integer.toString(b);

        // Concatenate both strings
        String s = s1 + s2;

        // Convert the concatenated string
        // to integer
        int c = Integer.parseInt(s);

        // return the formed integer
        return c;
    }
}
