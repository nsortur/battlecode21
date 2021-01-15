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
        }

        if (convertPolitician) {
            Util.greedyPath(targetLoc);
            convertEC();
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
            // defend role
            defendPolitician = true;
        } else if (ecFlagInfo[2] == 7) {
            // defend slanderer role
            defendSlandererPolitician = true;
        } else {
            otherPolitician = true;
        }
    }

    static void convertEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam().opponent()) {
                tryConvert();
            }
        }
    }

    /**
     * Attempts to move in a given direction.
     *
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryConvert() throws GameActionException {
        if (rc.canEmpower(rc.getType().actionRadiusSquared)) {
            rc.empower(rc.getType().actionRadiusSquared);
            return true;
        } else return false;
    }
}
