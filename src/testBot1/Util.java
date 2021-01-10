package testBot1;

import battlecode.common.*;

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

    static void getNumEC() {
        numEnlightenmentCenters = 3;
        // check flag - if it says number of ec's return that
        // if it does not then equal it to robot count (since this needs to happen immediately)
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

    // preceding "1" bit means positive, "2" means negative
    // valid coordinate codes can range from from 264264 (-64, -64) 100100 (0, 0) to 164164 (64, 64)
    // add support for bit at end ranging from 0-9 to explain different flags
    /**
     * Gets map coordinates from a flag value
     *
     * @return coordinates of edge
     */
    static int[] decryptOffsets(int flagVal) {
        int[] decrypted = new int[2];
        int x_sign = subInt(flagVal, 0, 1);
        int y_sign = subInt(flagVal, 3, 4);
        int flag_x = subInt(flagVal, 1, 3) - 10;
        int flag_y = subInt(flagVal, 4, 6) - 10;

        // make them negative if necessary
        if (x_sign == 2) flag_x = flag_x - (2 * flag_x);
        if (y_sign == 2) flag_y = flag_y - (2 * flag_y);

        decrypted[0] = flag_x;
        decrypted[1] = flag_y;

        return decrypted;
    }

    /**
     * Encrypts x and y offsets into a flag value
     *
     * @param xOffset the x offset
     * @param yOffset the y offset
     * @return the encrypted flag value
     */
    static int encryptOffsets(int xOffset, int yOffset) {
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

        return concatInts(flagX, flagY);
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

}
