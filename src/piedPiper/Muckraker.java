package piedPiper;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Muckraker extends RobotPlayer{
    static int ecID;
    static MapLocation ecLoc;
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static Direction dir = Direction.NORTH;

    static void run() throws GameActionException {
        if (turnCount == 1) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
        }

        tryKillSlanderer();
        isCloseToEnemyEC();
        isCloseToNeutralEC();

        moveAway();
    }


    /**
     * Move away from nearby robots if there are any
     *
     * @throws GameActionException
     */

    // TODO: Bugs with algorithm
    // 1 - not exploring well enough, not separating
    // 2 - crowding team EC
    static void moveAway() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        ArrayList<RobotInfo> robotsExceptEC = new ArrayList<>();
        for (RobotInfo robot : robots) {
            if (robot.getType() != RobotType.ENLIGHTENMENT_CENTER) {
                robotsExceptEC.add(robot);
            }
        }
        if (robotsExceptEC.size() != 0) {
            dir = calculateOptimalDirection();
        } else if (!rc.onTheMap(rc.getLocation().add(dir))) {
            dir = calculateOptimalDirection();
        }
        Util.tryMove(dir);
    }

    /**
     * Calculates an optimal direction to move based on the robots around
     *
     * @return a direction to move in
     * @throws GameActionException
     */

    static Direction calculateOptimalDirection() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(30, rc.getTeam());
        ArrayList<Direction> dirOppEdge = directionsOppositeEdge();
        int[] numOfRobotsInDir = new int[8];

        if (robots.length == 0) {
            return Util.randomDirection();
        } else if (dirOppEdge.size() != 0) {
            return dirOppEdge.get(new Random().nextInt(dirOppEdge.size()));
        } else {
            for (RobotInfo robot : robots) {
                Direction oppInDir = rc.getLocation().directionTo(robot.location);
                numOfRobotsInDir[directionsList.indexOf(oppInDir)] += 1;
            }
            int max = numOfRobotsInDir[0];
            int index = 0;
            int highestIndex = 0;
            for (int val : numOfRobotsInDir) {
                if (val > max) {
                    max = val;
                    highestIndex = index;
                }
                index += 1;
            }
            Random ran = new Random();
            int x = ran.nextInt(2) - 1;

            // TODO: problem is they tend to head southwest, because if val isn't GREATER then max, they ignore it
            // when you do the opposite of the middle values, you get southwest
            return directions[((highestIndex)+x+8) % 8].opposite();
        }
    }


    /**
     * Checks if a scout is next to an edge
     *
     * @return a list of directions that are opposite edge
     * @throws GameActionException
     */
    static ArrayList<Direction> directionsOppositeEdge() throws GameActionException{
        ArrayList<Direction> oppDir = new ArrayList<>();

        for(Direction dir : directions) {
            MapLocation adjLoc = rc.adjacentLocation(dir);
            if (!rc.onTheMap(adjLoc)) {
                oppDir.add(dir.opposite());
            }
        }
        return oppDir;
    }


    /**
     * Kills a slanderer if in action radius
     *
     * @return returns true if killed a slanderer
     * @throws GameActionException
     */
    static boolean tryKillSlanderer() throws GameActionException {
        boolean killedSlanderer = false;

        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius, rc.getTeam().opponent()); // sense all robots in action radius
        for (RobotInfo robot: robots) {
            if (robot.type == RobotType.SLANDERER && robot.team != rc.getTeam()) {
                if (rc.canExpose(robot.location)) rc.expose(robot.location);
                // TODO: ADD FLAG THAT TELLS EC KILLED SLANDERER **
                killedSlanderer = true;
            }
        }
        return killedSlanderer;
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
                MapLocation enemyECLoc = robot.location;

                int x_offset = enemyECLoc.x - ecLoc.x;
                int y_offset = enemyECLoc.y - ecLoc.y;

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
                MapLocation neutralEC = robot.location;

                int x_offset = neutralEC.x - ecLoc.x;
                int y_offset = neutralEC.y - ecLoc.y;

                if (Util.trySetFlag(Util.encryptOffsets(x_offset, y_offset, 2))) {
                    return true;
                }
            }
        }
        return false;
    }

}
