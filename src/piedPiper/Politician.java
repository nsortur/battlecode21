package piedPiper;

import battlecode.common.*;

// 4 Types of Politcians
// 1. Convert Politicians - given a location - go there and convert (flag code of 5)
// 2. Defend Politicians - given a location, and must defend (flag code of 6)
// 3. Defend Slanderer Politicians  - given enemy EC - must go in opposite direction until a few slanderers in sight
// then space out away from other politicians by moving away from them (flag code of 7)
// 4. Other Politicans - randomly created and slanderer spawned - must go around and move using muckraker code
// and only kill slanderers if it can, same with enlightenment centers, and maybe politicians as well? (flag code of 8)

public class Politician extends RobotPlayer {
    static final Team enemy = rc.getTeam().opponent();
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static int ecID; // only initialized if spawned from EC
    static MapLocation ecLoc; // only initialized if spawned from EC

    static MapLocation targetLoc;

    static boolean convertPolitician = false;
    static boolean defendPolitician = false;
    static boolean defendSlandererPolitician = false;
    static boolean otherPolitician = false;

    static void run() throws GameActionException {
        if (turnCount == 1 && rc.getFlag(rc.getID()) == 0) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            checkRole();
            System.out.println("the flag i put out was to: " + targetLoc + " on turn " + turnCount);
        }

        if (convertPolitician) {
            attackEnemyEC();
        }

        if (defendPolitician) {
            System.out.println("My target LOC is " + targetLoc);
            Util.greedyPath(targetLoc);
            defendTheEC();
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
        } else if (ecFlagInfo[2] == 6) {
            targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
            defendPolitician = true;
        } else if (ecFlagInfo[2] == 7) {
            // defend slanderer role
            defendSlandererPolitician = true;
        } else {
            otherPolitician = true;
        }
    }


    // TODO: Greedy Path DOES NOT WORK

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
        System.out.println("Going to: " + targetLoc);
        Util.greedyPath(targetLoc);
        // attack and raised killed flag if about to kill
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, null);
        for (RobotInfo robot : attackable) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(actionRadius)) {
                rc.empower(actionRadius);
            }
        }

    }

    static void defendTheEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        if (robots.length > 6) {
            rc.empower(rc.getType().actionRadiusSquared);
        }
    }
}
