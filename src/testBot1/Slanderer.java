package testBot1;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Team;

public class Slanderer extends RobotPlayer {
    // our enlightenment center's info (one politician spawned from)
    static int ecID;
    static MapLocation ecLoc;

    static final Team enemy = rc.getTeam().opponent();
    static MapLocation targetLoc;

    static void run() throws GameActionException {
        if (ecID == 0) {
            ecID = Util.getECID();
            checkForTargetLoc();
        }
        Util.greedyPath(targetLoc);

    }

    static void checkForTargetLoc() throws GameActionException{
        if (targetLoc == null) {
            int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

            if (ecFlagInfo[2] == 7) {

                ecLoc = Util.locationOfFriendlyEC();
                targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
            }
        }
    }



}
