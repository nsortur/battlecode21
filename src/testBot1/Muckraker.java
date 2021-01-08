package testBot1;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {
    static boolean goingEast;
    static boolean goingNorth;

    static void run() throws GameActionException {
        if (enemyEC.size() == 0) {
            killSlanderer();
            isCloseToEC();
            Util.moveNaive(new MapLocation(10026, 23926));
        }
    }

    /**
     *
     * @throws GameActionException
     */
    static void findEnemyEC() throws GameActionException {

        // SEARCH SURROUNDINGS FOR ENEMY EC
        // CHECKS TEAM EC TO SEE IF WE HAVE FOUND THEM ALREADY
        // MOVE IN DIRECTION


        // search surroundings, if found EC then report it
        // if does not find EC, get from flags
        // so --> if team ec says we have ecs in messages then get it from the flag (location to)
        // and if the team ec does not we need to keep searching (so move forward)
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
     * @throws GameActionException
     */

    static void killSlanderer() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(40); // sense all robots in action radius
        for (RobotInfo robot: robots) {
            if (robot.type == RobotType.SLANDERER && robot.team != rc.getTeam()) {
                if (rc.canExpose(robot.location)) rc.expose(robot.location);
            }
        }
    }

    /**
     * Concatenates 2 integers to be 1 integer
     *
     * @param a integer
     * @param b integer
     * @return ab, concatenated integer
     */
    static int concat(int a, int b)
    {

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
