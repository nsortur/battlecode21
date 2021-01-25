package piedPiper;

import battlecode.common.*;

import java.util.*;


public class EC extends RobotPlayer {

    // The location of the enemy EC
    static LinkedHashSet<MapLocation> enemyECLocs = new LinkedHashSet<>();
    static LinkedHashSet<MapLocation> neutralECLocs = new LinkedHashSet<>();
    static ArrayList<Integer> neutralECConvics = new ArrayList<>();
    static ArrayList<MapLocation> detectedNeutralECConvics = new ArrayList<>();
    static ArrayList<MapLocation> capturedNeutralECs = new ArrayList<>();


    static int numEnlightenmentCenters = 0;
    static int stuck = 0;
    // ID's of the scout's
    static LinkedHashSet<Integer> scoutID = new LinkedHashSet<>();

    // ID's of the politicians - [0] -> North [1] -> East [2] -> South [3] -> West
    static int[] polID = new int[4];

    // bidding variables
    static double percentage = .01;
    static int cap = 2;

    static int wasFlagSet = 0;

    static String[] smsmsp = {"S", "M", "S", "M", "S", "P"};
    static String[] smmp = {"S", "M", "M", "P", "S", "M", "M", "p"};
    static String[] sppm = {"S", "P", "P", "M"};

    static int indexTroop = 0;

    // TODO: Fix neutral EC bugs (Neel)
    // TODO: Replenish defendPoliticians

    static void run() throws GameActionException {
        boolean isFlagSet = false;
        // System.out.println("Was set flag is" + wasFlagSet);
        // IMPORTANT - We cannot spawn anything on the first turn
        // The order of these functions matter, it's the priority of spawning bots

        // Get the number of enlightenment centers
        if (numEnlightenmentCenters == 0) {
            getNumEC();
        }

        // process mucks
        if (enemyECLocs.size() != numEnlightenmentCenters) {
            processMuckrakers();
        }


        // if found neutral EC run code to convert it
        if (neutralECLocs.size() != 0 && rc.getInfluence() > neutralECConvics.iterator().next() + 30) {
            spawnCapturePols();
            isFlagSet = true;
            wasFlagSet = rc.getRoundNum();
        }

        // System.out.println(numEnlightenmentCenters + " and " + enemyECLocs.size());

        if (turnCount > 1) isFlagSet = spawnTroop();


        if (!isFlagSet && enemyECLocs.size() != 0 && rc.getRoundNum() - wasFlagSet > 4) {
            wasFlagSet = rc.getRoundNum();
            MapLocation enemyECToTarget = enemyECLocs.iterator().next();
            int[] offsets = Util.getOffsetsFromLoc(rc.getLocation(), enemyECToTarget);
            Util.trySetFlag(Util.encryptOffsets(offsets[0], offsets[1], 7));
        }

        if (isFlagSet) {
            wasFlagSet = rc.getRoundNum();
        }

        if (isSurrounded()) {
            bidSurrounded();
        } else {
           stuck = 0;
           bidInfluence();
        }
    }

    // TODO:
    // 2. Have all politicians that spawn with 25 influence guard then after 30 turns advance
    // 4. Create targeted muckrakers

    private static boolean spawnTroop() throws GameActionException {
        if (neutralECLocs.size() + capturedNeutralECs.size() == 0 && rc.getRoundNum() < 250) {
            if (rc.getInfluence() > 70) {
                spawnSlanderers(rc.getInfluence()-1);
            } else {
                spawnMuckrakers(1);
            }
        } else {
            String troopToSpawn = "S";
            if (rc.getRoundNum() > 700) {
                troopToSpawn = sppm[indexTroop % 4];
            } else if (rc.getRoundNum() > 250) {
                troopToSpawn = smmp[indexTroop % 8];
            } else {
                troopToSpawn = smsmsp[indexTroop % 6];
            }

            if (rc.isReady()) {
                switch (troopToSpawn) {
                    case "S":
                        spawnSlanderers((int) (0.75 * rc.getInfluence()));
                    case "P":
                        spawnPoliticians(25);
                    case "M":
                        spawnMuckrakers(1);
                    case "p":
                        if (rc.getInfluence() > 800) {
                            if (enemyECLocs.size() != 0) {
                                spawnBotToLocation(randomElement(enemyECLocs), 5, RobotType.POLITICIAN, (int) (0.5 * rc.getInfluence()));
                            } else {
                                spawnPoliticians(25);
                            }
                            indexTroop += 1;
                            wasFlagSet = rc.getRoundNum();
                            return true;
                        } else {
                            spawnPoliticians(25);
                        }
                }

                indexTroop += 1;
            }
        }

        return false;

    }

    private static MapLocation randomElement(HashSet<MapLocation> set) {
        int size = set.size();
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        for(MapLocation obj : set)
        {
            if (i == item)
                return obj;
            i++;
        }
        return set.iterator().next();
    }

    private static void bidSurrounded() throws GameActionException {
        if (stuck < 15){
            stuck++;
            return;
        }
        if (rc.canBid((int) ( rc.getInfluence() * .1))){
            rc.bid((int) (rc.getInfluence() * .1));
        }
    }

        //bidInfluence();

    // TODO: bidding if surrounded


    public static boolean isSurrounded() throws GameActionException {

        for (Direction direction : directionsList) {
            if (!rc.onTheMap(rc.getLocation().add(direction)))
                continue;

            if (!rc.isLocationOccupied(rc.getLocation().add(direction))) {
                return false;
            }
        }
        return true;
    }


    static void bidInfluence() throws GameActionException {
        if (rc.getTeamVotes() > 751){
            return;
        }
        if (rc.getRoundNum() > 500){
            percentage += .001;
        }
        int toBid = (int) (rc.getInfluence() * percentage);

        if (toBid > 200 && rc.getRoundNum() < 850 && rc.canBid(200)){
            rc.bid(200);
            return;
        }
        if (rc.canBid(toBid)) rc.bid(toBid);

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


    static void spawnPoliticians(int infl) throws GameActionException {
        Direction dir = getOpenDirection();
        if (dir != null) {
            spawnBot(RobotType.POLITICIAN, dir, infl); // if infl is very high make big boy politician and convert
        }
    }

    /**
     * Spawns slanderers
     *
     * @throws GameActionException
     */

    static void spawnSlanderers(int infl) throws GameActionException {
        Direction dir = getOpenDirection();
        if (dir != null) {
            spawnBot(RobotType.SLANDERER, getOpenDirection(), infl); // change?
        }
    }

    /**
     * Spawns muckrakers in a random open direction
     *
     * @throws GameActionException
     */
    static void spawnMuckrakers(int infl) throws GameActionException {
        Direction dir = getOpenDirection();
        if (dir != null) {
            if (spawnBot(RobotType.MUCKRAKER, dir, infl)) {
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
        neutralECConvics.remove(neutralECConvics.iterator().next());
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
                    if (!detectedNeutralECConvics.contains(foundLoc)) {
                        neutralECLocs.add(foundLoc);
                        detectedNeutralECConvics.add(foundLoc);

                        switch (neutralFlagInfo[2]) {
                            case 3:
                                neutralECConvics.add(72);
                                break;
                            case 4:
                                neutralECConvics.add(144);
                                break;
                            case 5:
                                neutralECConvics.add(215);
                                break;
                            case 6:
                                neutralECConvics.add(287);
                                break;
                            case 7:
                                neutralECConvics.add(358);
                                break;
                            case 8:
                                neutralECConvics.add(431);
                            case 9:
                                neutralECConvics.add(500);
                                break;
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
