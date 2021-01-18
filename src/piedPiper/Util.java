package piedPiper;

import battlecode.common.*;

import java.util.*;

public class Util extends RobotPlayer {

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

    /**
     * Gets location of friendly EC
     *
     * @return location of friendly ec, provided there is one
     * @throws GameActionException if out of range
     */
    static MapLocation locationOfFriendlyEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.location;
            }
        }
        throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "out of range of our ec");
    }

    static int getECID() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.ID;
            }
        }
        throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "can't get ECID");
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
     * @return the flag value, -2 if there is no set flag, -1 if flag value can't be read
     * @throws GameActionException
     */
    static int tryGetFlag(int id) throws GameActionException {
        if (rc.canGetFlag(id)) {
            id = rc.getFlag(id);
            // make sure there is a flag set
            if (id != 0) {
                return id;
            } else return -2;
        } else return -1;
    }


    // preceding "1" bit means positive, "2" means negative
    // valid coordinate codes can range from from 264264 (-64, -64) 100100 (0, 0) to 164164 (64, 64)
    // add support for bit at end ranging from 0-9 to explain different flags
    /**
     * Gets map coordinates from a flag value
     *
     * @return coordinates of edge and type of flag corresponding to dictionary in doc
     */
    static int[] decryptOffsets(int flagVal) {
        int[] decrypted = new int[3];
        int x_sign = subInt(flagVal, 0, 1);
        int y_sign = subInt(flagVal, 3, 4);
        int flag_x = subInt(flagVal, 1, 3) - 10;
        int flag_y = subInt(flagVal, 4, 6) - 10;
        int dictVal = subInt(flagVal, 6, 7);

        // make them negative if necessary
        if (x_sign == 2) flag_x = flag_x - (2 * flag_x);
        if (y_sign == 2) flag_y = flag_y - (2 * flag_y);

        decrypted[0] = flag_x;
        decrypted[1] = flag_y;
        decrypted[2] = dictVal;

        return decrypted;
    }


    /**
     * Encrypts x and y offsets into a flag value
     *
     * @param xOffset the x offset
     * @param yOffset the y offset
     * @param dictVal the meaning of the flag corresponding to dictionary in doc
     * @return the encrypted flag value
     */
    static int encryptOffsets(int xOffset, int yOffset, int dictVal) {
        int flagX;
        int flagY;
        int xOffsetNew = Math.abs(xOffset) + 10;
        int yOffsetNew = Math.abs(yOffset) + 10;

        // set signs
        if (xOffset < 0) {
            flagX = concatInts(2, xOffsetNew);
        } else {
            flagX = concatInts(1, xOffsetNew);
        }

        if (yOffset < 0) {
            flagY = concatInts(2, yOffsetNew);
        } else {
            flagY = concatInts(1, yOffsetNew);
        }

        return concatInts(concatInts(flagX, flagY), dictVal);
    }

    /**
     * Similar to "substring" but for an integer
     *
     * @param toSub integer to be broken up
     * @param start starting indices
     * @param end ending indices
     * @return part of the integer
     */
    static int subInt(int toSub, int start, int end){
        String strInt = Integer.toString(toSub);
        String str = strInt.substring(start, end);
        return Integer.parseInt(str);
    }

    /**
     * Concatenates 2 integers to be 1 integer
     *
     * @param a integer
     * @param b integer
     * @return ab, concatenated integer
     */
    static int concatInts(int a, int b) {

        // Convert both the integers to string
        String s1 = Integer.toString(a);
        String s2 = Integer.toString(b);

        // Concatenate both strings
        String s = s1 + s2;

        // return the formed integer
        return Integer.parseInt(s);
    }

    /**
     * Gets absolute location from a decrypted flag
     * Only works for EC
     *
     * @param decrypted the decrypted flag value
     * @return the absolute map location
     */
    static MapLocation getLocFromDecrypt(int[] decrypted, MapLocation curLoc) {
        return new MapLocation(curLoc.x + decrypted[0], curLoc.y + decrypted[1]);
    }


    /**
     * Get the best direction away from the given objects
     *
     * @param things the objects you are trying to move away from
     * @return a direction away from those objects
     * @throws GameActionException
     */
    static Direction getDirectionAway(HashSet<MapLocation> things) throws GameActionException {
        int indexSum = 0;
        for (MapLocation thing : things) {
            Direction dir = rc.getLocation().directionTo(thing);
            indexSum += directionsList.indexOf(dir);
        }
        int averageIndex = indexSum / things.size();
        return directions[(averageIndex + 4) % 8];
    }


    /**
     * Uses the greedy algorithm to move to a location
     *
     * @param target location to go to
     * @throws GameActionException
     */
    static void greedyPath(MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)) return;


        Direction direction = rc.getLocation().directionTo(target);

        if (rc.isReady()){
            List<Double> passabilities = new ArrayList<>();
            List<Direction> possibleDirections = new ArrayList<>();

            // add 3 directions to check
            for (int i = 0; i < 3; i++) {
                Direction directionToAdd = directionsList.get(Math.abs((directionsList.indexOf(direction) - 1 + i) % directionsList.size()));
                if (rc.onTheMap(rc.getLocation().add(directionToAdd))){
                    possibleDirections.add(directionToAdd);
                }
            }

            // add passabilties
            for (int i = 0; i < possibleDirections.size(); i++) {
                passabilities.add(rc.sensePassability(rc.getLocation().add(possibleDirections.get(i))));
            }

            int minIndex = passabilities.indexOf(Collections.min(passabilities));
            //System.out.println(passabilities.toString());
            //System.out.println(possibleDirections.toString());

            Direction go = possibleDirections.get(minIndex);
            if (rc.canMove(go)){
                rc.move(possibleDirections.get(minIndex));
            }
            else{
                for (Direction value : directionsList) {
                    if (rc.canMove(value)) rc.move(value);
                }
            }
        }
    }


    /**
     * Calculates a new location in the direction you want based on how many tiles
     *
     * @param dir the direction you want to go
     * @param numMove number of tiles you want to go in that direction
     * @param loc location you want to go to
     * @return
     */
    static MapLocation calculateNewLocationWithDirection(Direction dir, int numMove, MapLocation loc) {
        MapLocation newLoc = loc;
        for (int i = 0; i < numMove; i++) {
            newLoc = newLoc.add(dir);
        }
        return newLoc;
    }

    /**
     * Gets the offset from the current location and new loc
     * Only works for EC
     *
     * @param curLoc current location of ec
     * @param destLoc targetLocation
     * @return the offsets
     */
    static int[] getOffsetsFromLoc(MapLocation curLoc, MapLocation destLoc) {
        return new int[]{destLoc.x - curLoc.x, destLoc.y - curLoc.y};
    }

}
