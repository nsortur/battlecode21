package testBot1;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {
    static boolean goingEast;
    static boolean goingNorth;

    static void run() throws GameActionException {
        if (enemyEC.length == 0) {
            findEnemyEC();
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
                rc.setFlag(concat(robot.getLocation().x, robot.getLocation().y));
                return true;
            }
        }
        return false;
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
