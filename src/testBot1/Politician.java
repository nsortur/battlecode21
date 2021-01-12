package testBot1;

import battlecode.common.*;

public class Politician extends RobotPlayer {

    static boolean attackingEC;

    // our enlightenment center's info (one politician spawned from)
    static int ecID;
    static MapLocation ecLoc;

    static final Team enemy = rc.getTeam().opponent();
    static final int actionRadius = rc.getType().actionRadiusSquared;
    static MapLocation targetECLoc;

    static void run() throws GameActionException {
        if (ecID == 0) {
            ecID = Util.getECID();
        } else {
            // once we have the home EC's ID
            checkForTargetECLoc();
        }
    }

    /**
     * Defends by staying close to home EC
     *
     * @throws GameActionException
     */
    public static void defend() throws GameActionException {
        //constantly checks if theres an enemy bot
        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius, enemy);
        for (RobotInfo robot : robots) {
            if (rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
            }
        }
        // moves in random directions
        Direction direction = directions[(int) (Math.random() * directions.length)];
        if(rc.getLocation().add(direction).isWithinDistanceSquared(new MapLocation(0, 0), 15)){ // use friendly EC location instead
            if(rc.canMove(direction)){
                rc.move(direction);
            }
        }
    }

    /**
     * Go towards enemy EC and kaboom
     *
     * @throws GameActionException
     */
    static void attackEnemyEC() throws GameActionException{
        /**
         * get location from ec arraylist
         * calculate conviction to give (40% of EC conviction each for two politicians, 1% for a muckraker)
         * destroyedEC stop attacking if it's destroyed
         * if !destroyedEC, greedyMove to EC location while searching to empower if enlightenment center is seen
         *      if rounds have been more than 50, spawn another army and send
         * if destroyedEC, break and set attacking to false
         */

        Util.greedyPath(targetECLoc);
        // attack and raised killed flag if about to kill
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        for (RobotInfo robot : attackable) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(actionRadius)) {
                Util.trySetFlag(25);

                // wait for EC to read flag that politician is gonna die
                Clock.yield();
                attackingEC = false;
                rc.empower(actionRadius);
            }
        }

    }

    /**
     * Asks the home EC for the target location, and sets targetECLoc if it's available
     * Note: it asks for flag last bit 5
     *
     * @throws GameActionException
     */
    static void checkForTargetECLoc() throws GameActionException{
        if (targetECLoc == null) {
            int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

            if (ecFlagInfo[2] == 5) {
                attackingEC = true;
                // hardcoded south for now since attack politician spawns in the north
                ecLoc = rc.adjacentLocation(Direction.SOUTH);
                targetECLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
            }
        }
    }
}
