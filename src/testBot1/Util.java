package testBot1;

import battlecode.common.*;

import java.util.*;


public class Util extends RobotPlayer {




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
        if (!rc.getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
            System.out.println("not EC, trying to spawn from " + rc.getType());
        }
        if (rc.canBuildRobot(type, dir, influence)) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else return false;
    }


    /**
     * Moves to destination disregarding tile passibility
     *
     * @param dest: Destination to move towards
     *
     * @return true if destination is reached
     * @throws GameActionException
     */
    static boolean moveNaive(MapLocation dest) throws GameActionException{
        MapLocation curLoc = rc.getLocation();

        if (!curLoc.equals(dest)){
            Direction toDest = curLoc.directionTo(dest);
            tryMove(toDest);
            return false;

        } else return true;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }



    static void getNumEC() throws GameActionException {
        numEnlightenmentCenters = 3;
        // check flag - if it says number of ec's return that
        // if it does not then equal it to robot count (since this needs to happen immediately)
    }

    /**
     * Checks if a bot is next to an edge
     *
     * @return true if it's next to an edge
     * @throws GameActionException
     */
    static boolean isNextToEdge() throws GameActionException{
        boolean hitEdge = false;

        for(Direction dir : cardDirections) {
            MapLocation adjLoc = rc.adjacentLocation(dir);
            hitEdge = !rc.onTheMap(adjLoc);
            if (hitEdge) {
                rc.setFlag(2); //some flag that tells we've hit an edge
                break;
            }
        }

        return hitEdge;
    }

    /**
     * Sets a flag
     * @param value of the flag
     * @throws GameActionException
     */
    static boolean trySetFlag(int value) throws GameActionException {
        if (rc.canSetFlag(value)) {
            rc.setFlag(value);
            return true;
        } else return false;
    }

    /**
     * Gets a flag
     *
     * @param id of the robot
     * @return the flag value
     * @throws GameActionException
     */
    static int tryGetFlag(int id) throws GameActionException {
        if (rc.canGetFlag(id)) {
            return rc.getFlag(id);
        } else throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Cannot get flag");
    }

    /**
     * Gets the id of an EC
     *
     * @return gets the EC ID that the robot spawned from
     * @throws GameActionException
     */
    static int getECID() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.ID;
            }
        }
        throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "No enlightenment center");
    }


    /**
     * Moves the robot from its current location to the target location
     *
     * @param the location of the target
     * @throws GameActionException
     */

    static double passabilityThreshold = 0.7;
    static Direction bugDirection = null;
    static ArrayList<Direction> visited = new ArrayList<Direction>();
    // if you visit location twice then decrease threshold

    static void goTo(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)){
            visited.clear();
            return; // we have arrived at location
        }

        else if (rc.isReady()){
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold){
                rc.move(d);
                bugDirection = null;
            }
            else{
                if (bugDirection == null){
                    bugDirection = d.rotateRight();
                }
                for (int i = 0; i < 8; ++i){
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold){
                        rc.move(bugDirection);
                        break;
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
                bugDirection = bugDirection.rotateRight();
            }
        }
    }

    /**
     * Moves the robot from its current location to the target location
     *
     * @param  target's location
     * @throws GameActionException
     */

    static void go2(MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)){
            return; // we have arrived at location
        }
        final Direction line = rc.getLocation().directionTo(target);

        if (rc.isReady()){
            if (rc.canMove(line) && rc.sensePassability(rc.getLocation().add(line)) >= passabilityThreshold){
                rc.move(line);
            }
            else{
                for (int i = 0; i < 8; i++) {
                    if (rc.canMove(RobotPlayer.directions[i]) && rc.sensePassability(rc.getLocation().add(RobotPlayer.directions[i])) >= passabilityThreshold){
                        rc.move(RobotPlayer.directions[i]);
                        break;
                    }
                    else{
                        passabilityThreshold -= .1;
                    }

                }
            }
        }
    }

    static void greedyPath(MapLocation target) throws GameActionException{
        if (rc.getLocation().equals(target)){
            return; // we have arrived at location
        }
        Direction direction = rc.getLocation().directionTo(target);
        try{
            if (rc.isReady()){
                Hashtable<Integer, Direction> areas = new Hashtable<>();
                for (int i = 0; i < 3; i++) {
                    areas.put(i, directionsList.get((directionsList.indexOf(direction) - 1 + i) % directionsList.size()));
                }
                HashMap<Double, MapLocation> possibleDirections = new HashMap<>();
                for (int i = 0; i < 3; i++) {
                    possibleDirections.put(rc.sensePassability(rc.getLocation().add(areas.get(i))), rc.getLocation().add(areas.get(i)));
                }
                List keys = new ArrayList(possibleDirections.keySet());
                Collections.sort(keys);
                System.out.println("Areas:" + areas);
                System.out.println("Directions: " + possibleDirections);
                System.out.println("Keys" + keys);
                System.out.println("highest should be: " + keys.get(2));
                System.out.println("Moving to:" + rc.getLocation().directionTo(possibleDirections.get((keys.get(2)))));

                if(rc.canMove(rc.getLocation().directionTo(possibleDirections.get((keys.get(2)))))){
                    rc.move(rc.getLocation().directionTo(possibleDirections.get((keys.get(2)))));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
