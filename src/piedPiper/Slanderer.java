package piedPiper;

import battlecode.common.*;

import java.util.ArrayList;

public class Slanderer extends RobotPlayer {
    static int ecID;
    static MapLocation ecLoc;

    static MapLocation enemyECLoc;

    static void run() throws GameActionException {
        if (turnCount == 1) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            checkFlag();
        }

        moveAway();
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
        if (rc.isReady()) {
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
}
