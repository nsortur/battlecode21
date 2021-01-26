package piedPiper;

import battlecode.common.*;

import java.util.*;

// 4 Types of Politicians
// 1. Convert Politicians - given a location - go there and convert (flag code of 5)
// 2. Defend Politicians - given a location, and must defend (flag code of 6)
// 3. Defend Slanderer Politicians  - given enemy EC - must go in opposite direction until a few slanderers in sight
// then space out away from other politicians by moving away from them (flag code of 7)
// 4. Other Politicians - randomly created and slanderer spawned - must go around and move using muckraker code
// and only kill slanderers if it can, same with enlightenment centers, and maybe politicians as well? (flag code of 8)

public class Politician extends RobotPlayer {
    static final Team enemy = rc.getTeam().opponent();
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static int ecID; // only initialized if spawned from EC
    static MapLocation ecLoc; // only initialized if spawned from EC

    static MapLocation targetLoc;

    // If we want to attack enemy EC
    static boolean convertPolitician = false;

    // for neutral EC
    static boolean capturePolitician = false;
    static boolean henchCapturePolitician = false;
    static boolean jenCapturePolitician = false; // because it's a small one

    static int otherID;

    static boolean defendPolitician = false;
    static boolean otherPolitician = false;

    static Direction dir = Direction.NORTH;
    static Direction dirV4 = Direction.NORTH;


    static void run() throws GameActionException {
        if (turnCount == 1 && rc.getFlag(rc.getID()) == 0) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            checkRole();
        } else if (rc.getFlag(rc.getID()) != 0) {
            otherPolitician = true;
        }

        if (convertPolitician) {
            attackEC(enemy);
        }
        if (capturePolitician) {
            RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (otherID == 0) {
                    if (robot.influence == 23) {
                        otherID = robot.ID;
                        henchCapturePolitician = true;
                    } else if (robot.influence == 82 || robot.influence == 154 || robot.influence == 225 || robot.influence == 297 ||
                            robot.influence == 368 || robot.influence == 441 || robot.influence == 510){
                        otherID = robot.ID;
                        jenCapturePolitician = true;
                    }
                }
            }
            attackEC(Team.NEUTRAL);
        }

        if (defendPolitician) {
            defendTheEC();
            if (turnCount < 30) {
                appliedMoveAwayV4();
            } else {
                appliedMoveAwayV3();
            }

        }


        if (otherPolitician) {
            dir = calculateOptimalDirection();
            convertedAttack();
        }
    }



    /**
     * Checks the role of a politician based on the flag of the EC
     * @throws GameActionException
     */

    static void checkRole() throws GameActionException {
        int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

        if (ecFlagInfo[2] == 5) {
            convertPolitician = true;
            targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
        } else if (rc.getInfluence() == 25) {
            defendPolitician = true;
        } else if (ecFlagInfo[2] == 8) {
            capturePolitician = true;
            targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
        } else {
            otherPolitician = true;
        }
    }

    static void figureOutWhereToDefend() throws GameActionException {


    }


    /**
     * Go towards enemy EC and kaboom
     *
     * @throws GameActionException
     */
    static void attackEC(Team team) throws GameActionException {

        if (henchCapturePolitician) {
            RobotInfo[] robots = rc.senseNearbyRobots(2);
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(2) && robots.length == 1 && robot.team == team) {
                    rc.empower(2);
                } else if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam() && rc.getLocation().distanceSquaredTo(targetLoc) < 10) {
                    defendPolitician = true;
                    capturePolitician = false;
                }
            }
        }

        if (jenCapturePolitician) {
            RobotInfo[] robots = rc.senseNearbyRobots(2);
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(2) && robot.team.equals(Team.NEUTRAL)) {
                    rc.empower(2);
                } else if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam() && rc.getLocation().distanceSquaredTo(targetLoc) < 10) {
                    defendPolitician = true;
                    capturePolitician = false;
                }
            }
        }

        if (convertPolitician) {
            RobotInfo[] robots = rc.senseNearbyRobots(2);
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(2) && robot.team == rc.getTeam().opponent()) {
                    rc.empower(2);
                } else if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam() && rc.getLocation().distanceSquaredTo(targetLoc) < 10) {
                    defendPolitician = true;
                    convertPolitician = false;
                }
            }
        }
        // ystem.out.println("Going to: " + targetLoc);
        Util.greedyPath(targetLoc);

    }

    static void defendTheEC() throws GameActionException {

        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy);


        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius, rc.getTeam().opponent());
        if (robots.length > 0) {
            if (rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
            }
        }

        for (RobotInfo enemy : enemies) {
            if (enemy.getType() == RobotType.MUCKRAKER && rc.getLocation().distanceSquaredTo(enemy.location) < rc.getType().actionRadiusSquared) {
                if (rc.canEmpower(4)) {
                    rc.empower(4);
                }
            } else {
                Util.greedyPath(enemy.location);
            }
        }

    }

    static void convertedAttack() throws GameActionException {
        if (targetLoc == null) {
            int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

            RobotInfo[] ourRobots = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared, rc.getTeam());
            for (RobotInfo robot : ourRobots) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecLoc = robot.location;
                    ecID = robot.getID();
                }
            }

            if (ecLoc != null && ecFlagInfo[2] == 7) {
                targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);

            } else {
                // muckraker move algorithm goes here
                RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
                HashSet<RobotInfo> mucks = new HashSet<>();
                for (RobotInfo robot : attackable) {
                    if (robot.type == RobotType.MUCKRAKER || robot.type == RobotType.SLANDERER) {
                        mucks.add(robot);
                    }
                }
                if (mucks.size() > 3 && rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                }

                appliedMoveAwayV3();
            }

        } else {
            // whether to use old attack function
            RobotInfo[] attackable = rc.senseNearbyRobots(4, enemy);
            for (RobotInfo robot : attackable) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(4)) {
                    rc.empower(4);
                }
            }
            // System.out.println("Going to: " + targetLoc);
            Util.greedyPath(targetLoc);
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


    static void appliedMoveAwayV4() throws GameActionException {
        if (rc.onTheMap(rc.getLocation().add(dirV4)) && !rc.isLocationOccupied(rc.getLocation().add(dirV4))) {
            Util.tryMove(dirV4);
        } else {
            dir = moveAwayV4();
            Util.tryMove(dirV4);
        }
    }

    static Direction moveAwayV4() throws GameActionException {
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
                ranking[index] += calculateValueV4(location);
            }
            index += 1;
        }

        int min = findMinIdx(ranking);
        return newDirectionList.get(min);

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
            return 10;
        }

        RobotInfo robot = rc.senseRobotAtLocation(location);
        if (robot == null) {
            return -50;
        }  else if (robot.team != rc.getTeam()) {
            return -5000;
        } else if (robot.team == rc.getTeam()) {
            return 50;
        }  else {
            return 0;
        }
    }


    static int calculateValueV4(MapLocation location) throws GameActionException {

        if (!rc.onTheMap(location)) {
            return -100000;
        }

        RobotInfo robot = rc.senseRobotAtLocation(location);
        if (robot == null) {
            return 10;
        }  else if (robot.team == rc.getTeam() && robot.type == RobotType.POLITICIAN) {
            return 15;
        } else if (robot.team != rc.getTeam()) {
            return -50;
        } else if (robot.team == rc.getTeam()) {
            return 0;
        } else {
            return 20;
        }
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







}
