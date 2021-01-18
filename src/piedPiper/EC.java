package piedPiper;

import battlecode.common.*;

import java.util.*;


public class EC extends RobotPlayer {
    // The location of the enemy EC
    static HashSet<MapLocation> enemyECLocs = new HashSet<>();
    static HashSet<MapLocation> neutralECLocs = new HashSet<>();

    static int numEnlightenmentCenters = 0;

    // ID's of the scout's
    static LinkedHashSet<Integer> scoutID = new LinkedHashSet<>();

    // ID's of the politicians - [0] -> North [1] -> East [2] -> South [3] -> West
    static int[] polID = new int[4];

    // TODO: Fix neutral EC bugs (Neel)

    static void run() throws GameActionException {
        boolean isFlagUnimportant = true;
        // IMPORTANT - We cannot spawn anything on the first turn
        // The order of these functions matter, it's the priority of spawning bots

        // Get the number of enlightenment centers
        if (numEnlightenmentCenters == 0) {
            getNumEC();
        }

        // if found neutral EC run code to convert it

        // spawn defensive politicians if one is lost? keep track of ID's and make sure all of them are here
        if (turnCount > 20) {
            checkIfStillDefensePoliticians();
        }

         if (enemyECLocs.size() != numEnlightenmentCenters && isFlagUnimportant) { // TODO: isFlagUnimportant?
            processMuckrakers();
            isFlagUnimportant = false;
         }


        // spawn scouting muckrakers and process them for info
        if (turnCount % 5 == 0 && turnCount < 700) {
            spawnMuckrakers();
        } else if (turnCount % 8 == 0 && turnCount > 50 && turnCount < 500 && enemyECLocs.size() != 0) {
            spawnSlanderers(); // adjust flag for slanderers? direction?
            isFlagUnimportant = false;
        } else if (turnCount % 10 == 0) {
            spawnPoliticians(); // politicians can chase slanderers if it sees them to defend
        }


        if (isFlagUnimportant) {
            // put up our flag for politicians and (maybe? muckrakers) to use with a special code
        }
    }

    static void checkIfStillDefensePoliticians() throws GameActionException {
        MapLocation[] locations = new MapLocation[4];
        for (int i = 0; i < 4; i++) {
            locations[i] = rc.getLocation().add(cardDirections[i]).add(cardDirections[i]).add(cardDirections[i]);

        }
        boolean stillThere = true;
        int index = 0;

        for (int id : polID) {
            if (!rc.canSenseRobot(id)) {
                stillThere = false;
                break;
            } else {
                index += 1;
            }
        }
        if (!stillThere) {
            polID[index] = spawnBotToLocation(locations[index], 6, RobotType.POLITICIAN, 20);
        }
    }


    static void spawnPoliticians() throws GameActionException {

    }

    /**
     * Spawns slanderers
     *
     * @throws GameActionException
     */

    static void spawnSlanderers() throws GameActionException {
        // we can spawn it based on influence - 71 influence go one direction, 72 influence go another
        spawnBotToLocation(enemyECLocs.iterator().next(), 4, RobotType.SLANDERER, 40); // TODO: influence

        // communicate direction

    }

    /**
     * Spawns muckrakers in a random open direction
     *
     * @throws GameActionException
     */
    static void spawnMuckrakers() throws GameActionException {
        Direction dir = getOpenDirection();
        if (dir != null) {
            spawnBot(RobotType.MUCKRAKER, dir, 1);
            scoutID.add(rc.senseRobotAtLocation(rc.adjacentLocation(dir)).ID);
        }

    }

    /**
     * Processes muckrakers using the id's saved to see if there are any updates
     *
     * @throws GameActionException
     */
    static void processMuckrakers () throws GameActionException {
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
                    case 2:
                        neutralECLocs.add(Util.getLocFromDecrypt(flagInfo, rc.getLocation()));
                        // spawnNeutralPolitician();
                        break; // capture neutral ec using flaginfo
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

    // TODO: Talk about spawn bot
    static boolean spawnBot(RobotType type, Direction dir, int influence) throws GameActionException{
        if (rc.canBuildRobot(type, dir, influence) && rc.isReady()) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else {
            return false;
        }

    }

    /**
     * Gets an open direction to spawn to
     *
     * @return a direction that is empty next to EC
     * @throws GameActionException
     */

    static Direction getOpenDirection() throws GameActionException {
        List<Direction> directionsList = Arrays.asList(directions);
        Collections.shuffle(directionsList);

        for (Direction direction : directionsList) {
            if (!rc.isLocationOccupied(rc.adjacentLocation(direction))) {
                return direction;
            }
        }
        return null;
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

    /**
     * Spawns a bot that heads toward a given location by putting up a flag
     *
     * @param xOffset the x offset the bot should go to
     * @param yOffset the y offset the bot should go to
     * @param decryptCode the decryption code to use in the dictionary
     * @param robotType the type of robot to spawn
     * @param influence the influence you want to give this robot
     *
     * @return -1 if the EC is surrounded with enemy bots, otherwise the ID of unit spawned
     */
    static int spawnBotToLocation(int xOffset, int yOffset, int decryptCode, RobotType robotType, int influence) throws GameActionException {
        int flagToShow = Util.encryptOffsets(xOffset, yOffset, decryptCode);
        Direction dir = getOpenDirection();
        if (dir == null) {
            Clock.yield();
        }
        while (!spawnBot(robotType, dir, influence)) {
            Clock.yield();
        }

        if (Util.trySetFlag(flagToShow)) {
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
     * Spawns a bot that heads toward a given location by putting up a flag
     *
     * @param destLoc the location you want to go
     * @param decryptCode the decryption code to use in the dictionary
     * @param robotType the type of robot to spawn
     * @param influence the influence you want to give this robot
     *
     * @return -1 if the EC is surrounded with enemy bots, otherwise the ID of unit spawned
     */
    static int spawnBotToLocation(MapLocation destLoc, int decryptCode, RobotType robotType, int influence) throws GameActionException {
        int[] offsets = Util.getOffsetsFromLoc(rc.getLocation(), destLoc);
        int flagToShow = Util.encryptOffsets(offsets[0], offsets[1], decryptCode);

        Direction dir = getOpenDirection();
        if (dir == null) {
            Clock.yield();
        }
        while (!spawnBot(robotType, dir, influence)) {
            Clock.yield();
        }

        if (Util.trySetFlag(flagToShow)) {
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

}
