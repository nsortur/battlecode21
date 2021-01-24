package piedPiper;

import battlecode.common.*;

import java.util.*;


public class EC extends RobotPlayer {

    // The location of the enemy EC
    static LinkedHashSet<MapLocation> enemyECLocs = new LinkedHashSet<>();
    static LinkedHashSet<MapLocation> neutralECLocs = new LinkedHashSet<>();
    static LinkedHashSet<Integer> neutralECConvics = new LinkedHashSet<>();
    static ArrayList<MapLocation> capturedNeutralECs = new ArrayList<>();

    static int numEnlightenmentCenters = 0;
    static double bidding = 0.15;
    // ID's of the scout's
    static LinkedHashSet<Integer> scoutID = new LinkedHashSet<>();

    // ID's of the politicians - [0] -> North [1] -> East [2] -> South [3] -> West
    static int[] polID = new int[4];

    // bidding variables
    static int previousVoteNum = 0;
    static int cap = 2;

    // TODO: Fix neutral EC bugs (Neel)
    // TODO: Replenish defendPoliticians

    static void run() throws GameActionException {
        boolean isFlagSet = false;
        // IMPORTANT - We cannot spawn anything on the first turn
        // The order of these functions matter, it's the priority of spawning bots

        // Get the number of enlightenment centers
        if (numEnlightenmentCenters == 0) {
            getNumEC();
        }

        // if found neutral EC run code to convert it
        if (neutralECLocs.size() != 0 && rc.getInfluence() > neutralECConvics.iterator().next() + 30) {
            spawnCapturePols();
        }

        // spawn defensive politicians if one is lost? keep track of ID's and make sure all of them are here
        if (turnCount > 20) {
            if (checkIfStillDefensePoliticians()) {
                isFlagSet = true;
            }
        }
        System.out.println("Reached defense politician on turn " + rc.getRoundNum());

        if (enemyECLocs.size() != numEnlightenmentCenters) { // TODO: isFlagSet?
            processMuckrakers();
         }

        System.out.println("Reached process muckrakers on turn " + rc.getRoundNum());

        // spawn scouting muckrakers and process them for info
        if (turnCount % 4 == 0 && turnCount < 1000) {
            spawnMuckrakers();
            System.out.println("Reached spawn muckraker on turn " + rc.getRoundNum());

        } else if (turnCount % 7 == 0 && turnCount > 50 && turnCount < 500 && enemyECLocs.size() != 0) {
            spawnSlanderers(); // adjust flag for slanderers? direction?
            isFlagSet = true;
            System.out.println("Reached spawn slanderer on turn " + rc.getRoundNum());

        } else if (turnCount % 11 == 0) {
            // spawnPoliticians(); // politicians can chase slanderers if it sees them to defend
            System.out.println("Reached spawn politician on turn " + rc.getRoundNum());

        }


        if (!isFlagSet) {
            // todo: enemy ec flag muckraker surround flag
            Util.trySetFlag(-2);
        }


        if (rc.getRoundNum() < 550){
            rc.bid(1);
        }
        if (rc.getRoundNum() > 550){
            rc.bid((int) (rc.getInfluence() * bidding));
            bidding += .00015;
        }

        System.out.println("Reached end on turn " + rc.getRoundNum());
        //bidInfluence();

    }

    static void bidInfluence() throws GameActionException {
        if (rc.getTeamVotes() > 751){
            return;
        }

        if (rc.getRoundNum() < 450) {
            if (rc.canBid(cap)){
                rc.bid(cap);
            }
            System.out.println("early round bid");
        } else if (rc.getRoundNum() < 1250) {
            if (previousVoteNum == rc.getTeamVotes()) {
                cap += 2;
            }
            int influenceBid = (int) (0.1 * rc.getInfluence());
            if (influenceBid > cap) {
                if (rc.canBid(influenceBid)){
                    rc.bid(influenceBid);
                }
                System.out.println("over the cap");
            } else {
                if (rc.canBid(influenceBid)){
                    rc.bid(influenceBid);
                }
                System.out.println("under the cap");

            }
        } else {
            int influenceBid = (int) (0.1 * rc.getInfluence());
            System.out.println("bid: " + influenceBid);
            if (rc.canBid(influenceBid)){
                rc.bid(influenceBid);
            }

            System.out.println("end game");

        }

        previousVoteNum = rc.getTeamVotes();


    }

    static boolean checkIfStillDefensePoliticians() throws GameActionException {
        MapLocation[] locations = new MapLocation[4];
        for (int i = 0; i < 4; i++) {
            locations[i] = rc.getLocation().add(cardDirections[i]).add(cardDirections[i]).add(cardDirections[i]);
        }

        boolean stillThere = true;
        int index = 0;

        for (int id : polID) {
            if (!rc.canSenseRobot(id) && rc.onTheMap(locations[index])) {
                stillThere = false;
                break;
            } else {
                index += 1;
            }
        }
        if (!stillThere) {
            polID[index] = spawnBotToLocation(locations[index], 6, RobotType.POLITICIAN, 20);
            return true;
        }
        return false;
    }


    static void spawnPoliticians() throws GameActionException {
        spawnBot(RobotType.POLITICIAN, getOpenDirection(), 15);
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
            if (spawnBot(RobotType.MUCKRAKER, dir, 1)) {
                scoutID.add(rc.senseRobotAtLocation(rc.adjacentLocation(dir)).ID);
            }
        }

    }

    /**
     * Spawns a politician to capture a neutral EC
     *
     * @throws GameActionException
     */
    static void spawnCapturePols() throws GameActionException {
        MapLocation neutralLoc = neutralECLocs.iterator().next();
        int neutralConvic = neutralECConvics.iterator().next();
        spawnBotToLocation(neutralLoc, 8, RobotType.POLITICIAN, 23);
        // also add 10
        spawnBotToLocation(neutralLoc, 8, RobotType.POLITICIAN, neutralConvic + 10);
        neutralECLocs.remove(neutralLoc);
        neutralECConvics.remove(neutralConvic);
        capturedNeutralECs.add(neutralLoc);
    }

    /**
     * Processes muckrakers using the id's saved to see if there are any updates
     *
     * @throws GameActionException
     */
    static void processMuckrakers () throws GameActionException {
        for (int id : scoutID) {
            int curFlag = Util.tryGetFlag(id);

            if (curFlag != -1 && curFlag != -2) {
                if (Util.subInt(curFlag, 0, 1) > 2) {
                    // it's a neutral EC special flag
                    int[] neutralFlagInfo = Util.decryptOffsetsNeutral(curFlag);
                    MapLocation foundLoc = Util.getLocFromDecrypt(neutralFlagInfo, rc.getLocation());
                    if (!capturedNeutralECs.contains(foundLoc)) {
                        neutralECLocs.add(foundLoc);

                        switch (neutralFlagInfo[2]) {
                            case 3:
                                neutralECConvics.add(72);
                            case 4:
                                neutralECConvics.add(144);
                            case 5:
                                neutralECConvics.add(215);
                            case 6:
                                neutralECConvics.add(287);
                            case 7:
                                neutralECConvics.add(358);
                            case 8:
                                neutralECConvics.add(431);
                            case 9:
                                neutralECConvics.add(500);
                        }
                    }
                } else {
                    int[] flagInfo = Util.decryptOffsets(curFlag);
                    switch (flagInfo[2]) {
                        case 0:
                            break; // function for edge
                        case 1: // attack ec using flaginfo
                            enemyECLocs.add(Util.getLocFromDecrypt(flagInfo, rc.getLocation()));
                            break;
                        default:
                            break;
                    }
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
            if (rc.onTheMap(rc.adjacentLocation(direction)) && !rc.isLocationOccupied(rc.adjacentLocation(direction))) {
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
        while (dir == null) {
            Clock.yield();
            dir = getOpenDirection();
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
