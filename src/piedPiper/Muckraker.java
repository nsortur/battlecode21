package piedPiper;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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

        getDirectionAwayFromThings();

        // if a certain type of muckraker?
        // TODO: moving function needs work
        if (!Util.tryMove(dir)) {
            dir = Util.randomDirection();
        }
    }

    /**
     * Move away from nearby robots if there are any
     *
     * @return a direction away from a robot
     * @throws GameActionException
     */


    static void getDirectionAwayFromThings() throws GameActionException {
        // nearby robots
        RobotInfo[] robots = rc.senseNearbyRobots();
        ArrayList<Direction> dirOppEdge = directionsOppositeEdge();

        // if there are none go random place
        if (robots.length == 0) {
            dir = Util.randomDirection();
        } else if (dirOppEdge.size() != 0) {
            dir = dirOppEdge.get(new Random().nextInt(dirOppEdge.size()));
        } else {
            int closestDistance = robots[0].location.distanceSquaredTo(rc.getLocation());
            RobotInfo closestRobot = robots[0];
            for (RobotInfo robot : robots) {
                int dist = robot.location.distanceSquaredTo(rc.getLocation());
                if (closestDistance > dist) {
                    closestDistance = dist;
                    closestRobot = robot;
                }
            }
            dir = rc.getLocation().directionTo(closestRobot.location).opposite();
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

        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius); // sense all robots in action radius
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

}
