package testBot1;

import battlecode.common.*;

import java.util.HashSet;
import java.util.LinkedHashMap;

public class EC extends RobotPlayer {

    static boolean[] scoutsSpawned = new boolean[8];
    static boolean attackerSpawned;
    static boolean attackingEC;
    static int[] attackInfo;
    static int polID = -1;


    static int numEnlightenmentCenters = 0; // figure out how to calculate this value (kind of did)
    static HashSet<MapLocation> enemyECLocs = new HashSet<>();

    // key: scout IDs, value: their location
    // in order of clockwise direction starting at north, use iterator if you need direction
    static LinkedHashMap<Integer, int[]> scoutLocations = new LinkedHashMap<>();

    static void run() throws GameActionException {

        if (numEnlightenmentCenters == 0) {
            getNumEC();
        }
        if (numEnlightenmentCenters == enemyECLocs.size()) {
            // once we have found all EC's
        } else {
            int scoutID = spawnScout();

            // add scout to linked hashmap if it's spawned
            if (scoutID != -1) scoutLocations.put(scoutID, null);
            updateScoutLocs();
        }
        // For rush strat:
        // Util.spawnBot(RobotType.POLITICIAN, Direction.EAST, 150);
        // if (attackingEC) {
        //     spawnAttackPol();
        // }
        if (enemyECLocs.size() > 0) {
            spawnSlanderers();
        }

    }

    /**
     * Spawn slanderers that hide
     *
     * @throws GameActionException
     */

    static void spawnSlanderers() throws GameActionException {
        int roundNum = rc.getRoundNum();
        if (roundNum > 3 && roundNum < 200 && roundNum % 5 == 0) {
            // set flag

            setFlagBasedOnDirection(getDirectionForSlanderer());

            // spawn bot
            int inflToGive = (int) Math.round(slandProp * rc.getInfluence());
            Direction dir = getOpenDirection();
            Util.spawnBot(RobotType.SLANDERER, dir, inflToGive);

        }
    }

    static Direction getDirectionForSlanderer() throws GameActionException {
        int indexSum = 0;
        for (MapLocation enemyLOC : enemyECLocs) {
            Direction dir = rc.getLocation().directionTo(enemyLOC);
            indexSum += directionsList.indexOf(dir);
        }
        int averageIndex = indexSum / enemyECLocs.size();
        return directions[(averageIndex + 4) % 8];
    }

    /**
     * Spawns 8 scouts in different directions
     *
     * @throws GameActionException
     * @return the scout's id
     */
    static int spawnScout() throws GameActionException {
        for (int i = 0; i < 8; i++) {
            if (!scoutsSpawned[i]) {
                if (Util.spawnBot(RobotType.MUCKRAKER, directions[i], 1)) {
                    scoutsSpawned[i] = true;
                    setFlagBasedOnDirection(directions[i]);
                    MapLocation scoutLoc = rc.adjacentLocation(directions[i]);
                    return rc.senseRobotAtLocation(scoutLoc).ID;
                }
                break;
            }
        }
        return -1;
    }

    /**
     * Sets the EC's flag when creating a scout
     *
     * @param dir the direction the scout should move in
     * @throws GameActionException
     */
    static void setFlagBasedOnDirection(Direction dir) throws GameActionException {
        switch (dir) {
            case NORTH: Util.trySetFlag(11); break;
            case NORTHEAST: Util.trySetFlag(12); break;
            case EAST: Util.trySetFlag(13); break;
            case SOUTHEAST: Util.trySetFlag(14); break;
            case SOUTH: Util.trySetFlag(15); break;
            case SOUTHWEST: Util.trySetFlag(16); break;
            case WEST: Util.trySetFlag(17); break;
            case NORTHWEST: Util.trySetFlag(18); break;
            default:
                throw new IllegalStateException("Unexpected direction: " + dir);
        }
    }

    /**
     * Checks the field for all scouts' flags and updates their location in the hashmap
     *
     * @throws GameActionException
     */
    static void updateScoutLocs() throws GameActionException{
        int[] flagInfo = new int[]{0, 0, -1};
        for (int id : scoutLocations.keySet()) {
            int curFlag = Util.tryGetFlag(id);

            // make sure it's in range and a flag exists
            if (curFlag != -1 && curFlag != -2) {
                flagInfo = Util.decryptOffsets(curFlag);
                // uses ~1200 bytecode to put and ~1250 to get
                scoutLocations.put(id, flagInfo);
            }
            switch (flagInfo[2]) {
                case 0: break; // function for edge
                case 1: // attack ec using flaginfo
                    attackingEC = true;
                    attackInfo = flagInfo;
                    enemyECLocs.add(Util.getLocFromDecrypt(flagInfo, rc.getLocation()));
                    System.out.println("Starting to attack");
                    break;
                case 2: break; // capture neutral ec using flaginfo
                default: break;
            }
        }
    }

    /**
     * Spawns attacking politician
     *
     * @throws GameActionException
     */
    static void spawnAttackPol() throws GameActionException{
        if (!attackerSpawned) {
            polID = spawnBotToLocation(attackInfo[0], attackInfo[1], 5, RobotType.POLITICIAN, polProp);
            attackerSpawned = true;
        }
    }

    /**
     * Checks to see if attack pol has killed the enemy EC
     *
     * @throws GameActionException
     */
    static void checkAttackPol() throws GameActionException {
        if (Util.tryGetFlag(polID) == 25) {
            attackingEC = false;
            System.out.println("No longer attacking");
        }
    }

    /**
     * Spawns a bot that heads toward a given location by putting up a flag
     *
     * @param xOffset the x offset the bot should go to
     * @param yOffset the y offset the bot should go to
     * @param decryptCode the decryption code to use in the dictionary
     * @param robotType the type of robot to spawn
     * @param prop the proportion of influence you want to give this robot
     * @return
     */

    static int spawnBotToLocation(int xOffset, int yOffset, int decryptCode, RobotType robotType, double prop) throws GameActionException {
        int inflToGive = (int) Math.round(prop * rc.getInfluence());
        int flagToShow = Util.encryptOffsets(xOffset, yOffset, decryptCode);
        if (Util.trySetFlag(flagToShow)) {
            // spawn bot
            Direction dir = getOpenDirection();
            Util.spawnBot(robotType, dir, inflToGive);

            // get ID
            MapLocation polLoc = rc.adjacentLocation(dir);
            return rc.senseRobotAtLocation(polLoc).ID;
        } else {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Cannot set flag");
        }
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
