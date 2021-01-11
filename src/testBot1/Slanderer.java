package testBot1;

import battlecode.common.Direction;
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
        } else {
            // once we have the home EC's ID
            if (targetLoc == null) {
                int[] ecFlagInfo = Util.decryptOffsets(Util.tryGetFlag(ecID));

                if (ecFlagInfo[2] == 6) {
                    // hardcoded south for now since attack politician spawns in the north
                    ecLoc = rc.adjacentLocation(Direction.SOUTH);
                    targetLoc = Util.getLocFromDecrypt(ecFlagInfo, ecLoc);
                }
            }
        }
        Util.greedyPath(targetLoc);

    }
}
