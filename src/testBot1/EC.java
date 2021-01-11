package testBot1;

import battlecode.common.*;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class EC extends RobotPlayer {

    static boolean[] scoutsSpawned = new boolean[8];

    // key: scout IDs, value: their location
    // in order of clockwise direction starting at north, use iterator if you need direction
    static LinkedHashMap<Integer, int[]> scoutLocations = new LinkedHashMap<>();

    static void run() throws GameActionException {
        if (numEnlightenmentCenters == 0) {
            Util.getNumEC();
        }

        if (numEnlightenmentCenters == enemyECLocs.size()) {
            // once we have found all EC's
        } else {
            int scoutID = spawnScout();

            // add scout to linked hashmap if it's spawned
            if (scoutID != -1) scoutLocations.put(scoutID, null);
            updateScoutLocs();

        }
        Util.spawnBot(RobotType.POLITICIAN, Direction.EAST, 150);
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
                    setFlagScout(directions[i]);
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
    static void setFlagScout(Direction dir) throws GameActionException {
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
        // loop through spawned scouts and update hashmap with location if possible
        int[] flagInfo;
        for (int id : scoutLocations.keySet()) {
            int curFlag = Util.tryGetFlag(id);

            // make sure it's in range and a flag exists
            if (curFlag != -1 && curFlag != -2) {
                flagInfo = Util.decryptOffsets(curFlag);
                // uses ~1200 bytecode to put and ~1250 to get
                // scoutLocations.put(id, flagInfo);
            }



        }
    }
}
