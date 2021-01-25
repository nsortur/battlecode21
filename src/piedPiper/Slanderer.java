package piedPiper;

import battlecode.common.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Slanderer extends RobotPlayer {
    static int ecID;
    static MapLocation ecLoc;

    static MapLocation enemyECLoc;

    static Direction dir;

    static void run() throws GameActionException {
        if (turnCount == 1) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            checkFlag();
            Util.trySetFlag(Util.getECID());
            dir = calculateOptimalDirection();
        }

        Util.greedyPath(new MapLocation(26538, 23910));
        // Slanderer has one purpose, go in general direction away from enemy EC (flag code of 9)
        // create by giving enemy EC location
        // general goal is to go in opposite direction - +1 or -1 but stay away


        // politicians make sure that
    }

    /**
     * Moves away from enemy ec's and team buildings
     *
     * @throws GameActionException
     */

    static void moveAway() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] closeRobots = rc.senseNearbyRobots(2, rc.getTeam());
        MapLocation ecLocTeam = rc.adjacentLocation(Direction.NORTH); // problem?
        boolean closeToEC = false;
        for (RobotInfo robot : closeRobots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                closeToEC = true;
                ecLocTeam = robot.location;
            }
        }

        if (enemyRobots.length != 0) {
            Util.tryMove(moveAwayFromEnemy());
        }
        if (closeToEC) {
            // TODO: change to move anywhere i can away
            Util.tryMove(rc.getLocation().directionTo(ecLocTeam).opposite());
        }
        if (rc.isReady() && enemyECLoc != null) {
            Direction optDir = Direction.NORTH;
            int maxDist = rc.adjacentLocation(optDir).distanceSquaredTo(enemyECLoc);;
            for (Direction direction : directions) {
                int dist = rc.adjacentLocation(direction).distanceSquaredTo(enemyECLoc);
                if (dist > maxDist) {
                    MapLocation locToMove = rc.adjacentLocation(direction);
                    if (rc.onTheMap(locToMove) && !rc.isLocationOccupied(locToMove)) {
                        maxDist = dist;
                        optDir = direction;
                    }
                }
            }
            int dist = rc.getLocation().distanceSquaredTo(enemyECLoc);
            if (maxDist > dist) {
                Util.tryMove(optDir);
            }
        }
    }

    /**
     * Move's away from enemy troops
     *
     * @return a direction to move
     * @throws GameActionException
     */

    static Direction moveAwayFromEnemy() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
        int[] numOfRobotsInDir = new int[8];

        for (RobotInfo robot : robots) {
            Direction oppInDir = rc.getLocation().directionTo(robot.location);
            int distance = rc.getLocation().distanceSquaredTo(robot.location);
            int index = directionsList.indexOf(oppInDir);
            if (distance >= 25) {
                numOfRobotsInDir[index] += 1;
            } else if (distance >= 16) {
                numOfRobotsInDir[index] += 2;
            } else if (distance >= 9) {
                numOfRobotsInDir[index] += 3;
            } else if (distance >= 4) {
                numOfRobotsInDir[index] += 4;
            } else if (distance >= 1) {
                numOfRobotsInDir[index] += 5;
            } else {
                numOfRobotsInDir[index] += 1;
            }
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
        return directions[((highestIndex)+8) % 8].opposite();
    }

    static void checkFlag() throws GameActionException {
        int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

        if (ecFlagInfo[2] == 4) {
            enemyECLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
        }
    }


    static void appliedMoveAwayV3() throws GameActionException {
        if (rc.onTheMap(rc.getLocation().add(dir)) && !rc.isLocationOccupied(rc.getLocation().add(dir))) {
            Util.tryMove(dir);
        } else {
            dir = moveAwayV3();
            Util.tryMove(dir);
        }
    }

    static Direction moveAwayV3() throws GameActionException {
        int[] ranking = new int[8];
        int index = 0;

        List<Direction> newDirectionList = directionsList;
        Collections.shuffle(newDirectionList);

        MapLocation rcLoc = rc.getLocation();
        for (Direction dir : newDirectionList) {
            MapLocation loc1 = rcLoc.add(dir);
            MapLocation loc2 = loc1.add(dir);
            MapLocation loc3 = loc2.add(dir);

            MapLocation[] locations = {loc1, loc2, loc3};

            for (MapLocation location : locations) {
                ranking[index] += calculateValue(location);
            }
            index += 1;
        }

        int min = findMinIdx(ranking);
        return newDirectionList.get(min);

    }

    /**
     * Find min value in array of numbers
     * @param numbers
     * @return
     */
    static int findMinIdx(int[] numbers) {
        if (numbers == null || numbers.length == 0) return -1; // Saves time for empty array
        // As pointed out by ZouZou, you can save an iteration by assuming the first index is the smallest
        int minVal = numbers[0]; // Keeps a running count of the smallest value so far
        int minIdx = 0; // Will store the index of minVal
        for(int idx=1; idx<numbers.length; idx++) {
            if(numbers[idx] < minVal) {
                minVal = numbers[idx];
                minIdx = idx;
            }
        }
        return minIdx;
    }

    static int calculateValue(MapLocation location) throws GameActionException {

        if (!rc.onTheMap(location)) {
            return -1000;
        }
        RobotInfo robot = rc.senseRobotAtLocation(location);
        if (robot == null) {
            return 0;
        } else if (robot.type == RobotType.ENLIGHTENMENT_CENTER && (robot.team == Team.NEUTRAL || robot.team == rc.getTeam())) {
            return 10000;
        } else if (robot.team == rc.getTeam() & robot.type == RobotType.SLANDERER) {
            return -50;
        } else if (robot.team == rc.getTeam().opponent() && robot.type == RobotType.POLITICIAN || robot.type == RobotType.MUCKRAKER) {
            return 10000;
        } else if (robot.team == rc.getTeam() && robot.type == RobotType.POLITICIAN) {
            return 100;
        } else {
            return 0;
        }
    }


    /**
     * Gets the first open direction
     *
     * @return
     * @throws GameActionException
     */
    static Direction firstOpenDir() throws GameActionException {
        for (Direction dir : directionsList) {
            if (rc.canMove(dir)) {
                return dir;
            }
        }
        return Direction.NORTH;
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

}
