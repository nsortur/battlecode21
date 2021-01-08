package testBot1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class EC extends RobotPlayer {
    static int totalBeginNorthMuck = 0;
    static int totalBeginEastMuck = 0;

    static void run() throws GameActionException {
        if (numEnlightenmentCenters == 0) {
            Util.getNumEC();
        }
        if (numEnlightenmentCenters == enemyEC.size()) {
            // once we have found all EC's
        } else {
            if (totalBeginNorthMuck == 0 && Util.spawnBot(RobotType.MUCKRAKER, Direction.NORTH, 1)) {
                totalBeginNorthMuck += 1;
                numMuckrakers++;
            }
            if (totalBeginEastMuck == 0 && Util.spawnBot(RobotType.MUCKRAKER, Direction.EAST, 1)){
                totalBeginEastMuck += 1;
                numMuckrakers++;
            }
        }

    }
}
