package notABotJustTest;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

import java.util.HashSet;

public class EC extends RobotPlayer {
    // The location of the enemy EC
    static HashSet<MapLocation> enemyECLocs = new HashSet<>();
    static int numEnlightenmentCenters = 0;

    // ID's of the scout's
    static HashSet<Integer> scoutID = new HashSet<>();

    static void run() throws GameActionException {
        spawnMuckrakers();
    }

    static void spawnPoliticians() throws GameActionException {

    }

    static void spawnSlanderers() throws GameActionException {
        // we can spawn it based on influence - 71 influence go one direction, 72 influence go another
        Direction dirToGo = Util.getDirectionAway(enemyECLocs);
        rc.buildRobot(RobotType.SLANDERER, getOpenDirection(), 1);

        // communicate direction

    }

    /**
     * Spawns muckrakers in a random open direction
     *
     * @throws GameActionException
     */
    static void spawnMuckrakers() throws GameActionException {
        Direction dir = getOpenDirection();
        if (spawnBot(RobotType.MUCKRAKER, dir, 1)) {
            scoutID.add(rc.senseRobotAtLocation(rc.adjacentLocation(dir)).ID);
        }
    }

    /**
     * Processes muckrakers using the id's saved to see if there are any updates
     *
     * @throws GameActionException
     */
    static void processMuckrakers() throws GameActionException {
        for (int id : scoutID) {
            int curFlag = Util.tryGetFlag(id);

            // make sure it's in range and a flag exists
            if (curFlag != -1 && curFlag != -2) {
                int[] flagInfo = Util.decryptOffsets(curFlag);
                switch (flagInfo[2]) {
                    case 0: break; // function for edge
                    case 1: // attack ec using flaginfo
                        enemyECLocs.add(Util.getLocFromDecrypt(flagInfo, rc.getLocation()));
                        break;
                    case 2: break; // capture neutral ec using flaginfo
                    default: break;
                }
            }

        }
    }

    /**
     * Spawns a bot for an enlightenment center
     *
     * @param type: type to spawn
     * @param dir: direction to spawn in
     * @param influence: influence to transfer to bot
     *
     * @return true if spawned
     * @throws GameActionException
     */
    static boolean spawnBot(RobotType type, Direction dir, int influence) throws GameActionException{
        if (rc.canBuildRobot(type, dir, influence)) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else return false;
    }

    /**
     * Gets an open direction to spawn to
     *
     * @return a direction that is empty next to EC
     * @throws GameActionException
     */

    static Direction getOpenDirection() throws GameActionException {
        for (Direction direction : directions) {
            if (!rc.isLocationOccupied(rc.adjacentLocation(direction))) {
                return direction;
            }
        }
        return Direction.NORTH;
    }

    /**
     * Sets the number of EC's
     *
     * @throws GameActionException
     */

    static void getNumEC() throws GameActionException {
        if (rc.getRoundNum() < 3) {
            numEnlightenmentCenters = rc.getRobotCount();
        } else {
            numEnlightenmentCenters = -1;
            // the case where our EC has been converted and then converted back // neutral EC
            // ideas
            // TODO ask for help using flags - send a 55 and the someone will respond with correct number of EC's
        }
    }
}
