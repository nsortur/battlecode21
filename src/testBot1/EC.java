package testBot1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class EC extends RobotPlayer {

    static boolean[] scoutsSpawned = new boolean[8];

    static void run() throws GameActionException {
        if (numEnlightenmentCenters == 0) {
            Util.getNumEC();
        }
        if (numEnlightenmentCenters == enemyEC.size()) {
            // once we have found all EC's
        } else {
            spawnScout();
        }

    }

    static void spawnScout() throws GameActionException {
        for (int i = 0; i < 8; i++) {
            if (!scoutsSpawned[i]) {
                if (Util.spawnBot(RobotType.MUCKRAKER, directions[i], 1)) scoutsSpawned[i] = true;
                // flag shit
                break;
            }
        }
    }
}
