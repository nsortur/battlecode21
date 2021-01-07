package testBot1;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {
    static boolean goingEast;
    static boolean goingNorth;

    static void run() throws GameActionException {
        if (enemyEC.length == 0) {
            Util.findEnemyEC();
        }
    }
}
