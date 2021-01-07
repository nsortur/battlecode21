package testBot1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class EC extends RobotPlayer {
    static int totalBeginNorthMuck = 0;
    static int totalBeginEastMuck = 0;

    static void run() throws GameActionException {
        if (totalBeginNorthMuck == 0 && spawnBot(RobotType.MUCKRAKER, Direction.NORTH, 1)) {
            totalBeginNorthMuck += 1;
            numMuckrakers++;
        }
        if (totalBeginEastMuck == 0 && spawnBot(RobotType.MUCKRAKER, Direction.EAST, 1)){
            totalBeginEastMuck += 1;
            numMuckrakers++;
        }
    }
}
