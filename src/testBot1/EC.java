package testBot1;

import battlecode.common.*;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class EC extends RobotPlayer {

    static boolean[] scoutsSpawned = new boolean[8];
    static boolean attackerSpawned;
    static boolean attackingEC;

    // check to see if early game scouts have spawned
    static boolean earlyGameSlandererSpawned;

    // key: scout IDs, value: their location
    // in order of clockwise direction starting at north, use iterator if you need direction
    static LinkedHashMap<Integer, int[]> scoutLocations = new LinkedHashMap<>();

    static void run() throws GameActionException {
        System.out.println(numEnlightenmentCenters);
        if (numEnlightenmentCenters == 0) {
            Util.getNumEC();
            Clock.yield();
        }

        if (enemyECLocs.size() > 1) {
            // TODO spawn slanderers
            spawnEarlyGameSlanderer();
        } else {
            int scoutID = spawnScout();

            // add scout to linked hashmap if it's spawned
            if (scoutID != -1) scoutLocations.put(scoutID, null);
            updateScoutLocs();

        }
        if (attackingEC) {
            spawnAttackPols();
        }
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
     * Spawns the early game slanderers
     *
     * @throws GameActionException
     */
    static void spawnEarlyGameSlanderer() throws GameActionException {
        // Spawn the slanderers
        // set a flag to a location
        // calculate location using average of this value
        // angle of 2 points, the value of a enemy EC and value of this ec
        // tell robot to go in opposite direction
        //
        double propToGive = 0.5;
        int inflToGive = (int) Math.round(propToGive * rc.getInfluence());

        if (!earlyGameSlandererSpawned) {
            int[] offsets = Util.getOffsetFromEncrypt(rc.getLocation(), getNewLocation());
            int flagToShow = Util.encryptOffsets(offsets[0], offsets[1], 6);
            if (Util.trySetFlag(flagToShow)) {
                // make spawn bot return ID in the future
                Util.spawnBot(RobotType.SLANDERER, Direction.NORTH, inflToGive);

                earlyGameSlandererSpawned = true;
            }
        }
    }

    static MapLocation getNewLocation() throws GameActionException {
        int[] offsets = new int[2];
        int angleIndexSum = 0;
        int distanceSum = 0;
        for (MapLocation enemyEC : enemyECLocs) {
            Direction dir = rc.getLocation().directionTo(enemyEC);
            angleIndexSum += directionsList.indexOf(dir);
            distanceSum += rc.getLocation().distanceSquaredTo(enemyEC);
        }
        int averageIndex = angleIndexSum / enemyECLocs.size();
        Direction direction = directions[Math.abs(averageIndex-4)];
        int averageDistance = distanceSum / enemyECLocs.size();
        int i = 0;
        MapLocation newLocation = rc.getLocation();
        while (i < Math.sqrt(averageDistance / 3)) {
            newLocation = newLocation.add(direction);
            i+=1;
        }
        return newLocation;
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

    static int[] attackInfo;
    static int polID = -1;
    /**
     * Spawns attacking politician
     *
     * @throws GameActionException
     */
    static void spawnAttackPols() throws GameActionException{
        double propToGive = 0.9;
        int inflToGive = (int) Math.round(propToGive * rc.getInfluence());

        if (!attackerSpawned) {
            int flagToShow = Util.encryptOffsets(attackInfo[0], attackInfo[1], 5);
            if (Util.trySetFlag(flagToShow)) {
                // make spawn bot return ID in the future
                Util.spawnBot(RobotType.POLITICIAN, Direction.NORTH, inflToGive);

                // get politician's ID
                MapLocation polLoc = rc.adjacentLocation(Direction.NORTH);
                polID = rc.senseRobotAtLocation(polLoc).ID;
                attackerSpawned = true;
            }
        }

        if (Util.tryGetFlag(polID) == 25) {
            attackingEC = false;
            System.out.println("No longer attacking");
        }
    }

}
