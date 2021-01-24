package piedPiper;

import battlecode.common.*;

import java.util.*;

public class Muckraker extends RobotPlayer {
    static int ecID;
    static MapLocation ecLoc;
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static Direction dir = Direction.NORTH;

    static void run() throws GameActionException {
        if (turnCount == 1) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            dir = calculateOptimalDirection();
        }


        tryKillSlanderer();
        isCloseToNeutralEC();

        if (isCloseToEnemyEC()) {
            // doing things
        } else {
            appliedMoveAwayV3();
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
        System.out.println(newDirectionList.get(min));
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
            return 1000;
        }
        RobotInfo robot = rc.senseRobotAtLocation(location);
        if (robot == null) {
            return 0;
        } else if (robot.type == RobotType.ENLIGHTENMENT_CENTER && (robot.team == Team.NEUTRAL || robot.team == rc.getTeam())) {
            return 10000;
        } else if (robot.team == rc.getTeam()) {
            return 100;
        } else if (robot.team == rc.getTeam().opponent() && robot.type == RobotType.POLITICIAN) {
            return 2;
        } else if (robot.team == rc.getTeam().opponent() && robot.type == RobotType.SLANDERER) {
            return -15;
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
                Util.greedyPath(enemyECLoc);
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
                int convic = robot.conviction;
                // spawn big boi just in case we don't detect correctly
                int dictVal = 9;

                if (convic < 72) {
                    dictVal = 3;
                } else if (convic < 144) {
                    dictVal = 4;
                } else if (convic < 215) {
                    dictVal = 5;
                } else if (convic < 287) {
                    dictVal = 6;
                } else if (convic < 358) {
                    dictVal = 7;
                } else if (convic < 431) {
                    dictVal = 8;
                } else {
                    System.out.println("impossiblee");
                }

                if (Util.trySetFlag(Util.encryptOffsetsNeutral(x_offset, y_offset, dictVal))) {
                    return true;
                }
            }
        }
        return false;
    }

}
