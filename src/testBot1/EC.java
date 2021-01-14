package testBot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class EC extends RobotPlayer {

    static boolean[] scoutsSpawned = new boolean[8];

    static boolean attackPolSpawned;
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
        if (enemyECLocs.size() > 0) { //why?
            spawnSlanderers();
            spawnDefensivePoliticians();
            spawnOffensiveMuckrakers();
        }
        // bid influence
        if (rc.getRoundNum() > 500 && rc.getTeamVotes() < 1502){
            rc.bid((int) (0.15 * rc.getInfluence()));
        }
        // spawn attack politician
        if (rc.getInfluence() > 11000 && !attackPolSpawned && spawnAttackPol()){
            attackPolSpawned = true;
        }
        checkAttackPol();
    }

    // TODO: Make scouts greedy path!!!

    static void spawnDefensivePoliticians() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {

        }
    }

    static void spawnOffensiveMuckrakers() throws GameActionException {
        int roundNum = rc.getRoundNum();
        if (roundNum > 10 && roundNum % 3 == 0) {
            spawnBotToLocation(enemyECLocs.iterator().next(), 9, RobotType.MUCKRAKER, 1);
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
            Direction direction = getDirectionForSlanderer();
            int index = directionsList.indexOf(direction);

            // gets the scout information based on direction slanderer is going
            List values = new ArrayList(scoutLocations.values());
            int[] loc = (int[]) values.get(index);

            // gets the map location the slanderer should go to
            MapLocation scoutLoc = Util.getLocFromDecrypt(loc, rc.getLocation());
            MapLocation optimalLoc = scoutLoc;
            for (int i = 0; i < 6; i++) {
                optimalLoc = optimalLoc.subtract(direction);
            }

            // spawn bot
            spawnBotToLocation(optimalLoc, 7, RobotType.SLANDERER, slandProp);

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
     * @return whether attack pol is spawned
     * @throws GameActionException
     */
    static boolean spawnAttackPol() throws GameActionException{
        polID = spawnBotToLocation(attackInfo[0], attackInfo[1], 5, RobotType.POLITICIAN, attackPolProp);
        return polID != -1;
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
     *
     * @return -1 if the EC is surrounded with enemy bots, otherwise the ID of unit spawned
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
            RobotInfo rob = rc.senseRobotAtLocation(polLoc);
            if (rob.team == rc.getTeam()) {
                return rob.ID;
            } else return -1;

        } else {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Cannot set flag");
        }
    }

    /**
     *
     * @param destLoc destination to send robot towards
     * @param decryptCode the decryption code to use in the dictionary
     * @param robotType the type of robot to spawn
     * @param prop the proportion of influence you want to give this robot
     *
     * @return -1 if the EC is surrounded with enemy bots, otherwise the ID of unit spawned
     * @throws GameActionException
     */
    static int spawnBotToLocation(MapLocation destLoc, int decryptCode, RobotType robotType, double prop) throws GameActionException {
        int inflToGive = (int) Math.round(prop * rc.getInfluence());
        int[] offsets = Util.getOffsetsFromLoc(rc.getLocation(), destLoc);
        int flagToShow = Util.encryptOffsets(offsets[0], offsets[1], decryptCode);
        if (Util.trySetFlag(flagToShow)) {
            // spawn bot
            Direction dir = getOpenDirection();
            Util.spawnBot(robotType, dir, inflToGive);

            // get ID
            MapLocation polLoc = rc.adjacentLocation(dir);
            RobotInfo rob = rc.senseRobotAtLocation(polLoc);
            if (rob.team == rc.getTeam()) {
                return rob.ID;
            } else return -1;

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
