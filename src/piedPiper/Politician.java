package piedPiper;

import battlecode.common.*;

import java.util.HashSet;

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

    static boolean capturePolitician = false;
    static int otherID;

    static boolean defendPolitician = false;
    static boolean defendSlandererPolitician = false;
    static boolean otherPolitician = false;

    static void run() throws GameActionException {
        if (turnCount == 1 && rc.getFlag(rc.getID()) == 0) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
            checkRole();
        } else if (rc.getFlag(rc.getID()) == 10) {
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
                    }
                }
            }
            attackEC(Team.NEUTRAL);
        }

        if (defendPolitician) {
            Util.greedyPath(targetLoc);
            defendTheEC();
        }

        if (defendSlandererPolitician){
            defendSlanderer();
        }
        if (otherPolitician){
            // MAKE IT SAME AS MUCKRAKER ALGORITHM!!!
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if (attackable.length > 2) {
                if (rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                }
            }

        }

    }

    private static void defendSlanderer() {
        System.out.println("Enemy ec loc at " + ecLoc);
        System.out.println();
        System.out.println();
       // rc.resign();
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
            targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
            defendSlandererPolitician = true;
        } else if (ecFlagInfo[2] == 8) {
            capturePolitician = true;
            targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
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
    static void attackEC(Team team) throws GameActionException {

        RobotInfo[] attackable = rc.senseNearbyRobots(2, team);
        RobotInfo[] ourRobots = rc.senseNearbyRobots(2, rc.getTeam());
        HashSet<RobotInfo> ourRobotsNew = new HashSet<>();
        for (RobotInfo robot : ourRobots) {
            if (robot.ID != otherID) {
                ourRobotsNew.add(robot);
            }
        }
        System.out.println(ourRobotsNew);
        for (RobotInfo robot : attackable) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(2) && ourRobotsNew.size() < 1) {
                rc.empower(2);
            }
        }

        if (rc.canDetectLocation(targetLoc)) {
            RobotInfo maybeNeutralEC = rc.senseRobotAtLocation(targetLoc);
            int distToEC = rc.getLocation().distanceSquaredTo(targetLoc);
            if (maybeNeutralEC.team == rc.getTeam() && distToEC < 4) {
                rc.empower(4);
            }
        }
        Util.greedyPath(targetLoc);

    }

    static void defendTheEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        if (robots.length > 6) {
            rc.empower(rc.getType().actionRadiusSquared);
        }
    }
}
