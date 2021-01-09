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
            // changes
        }

    }

    /**
     * Spawns 8 scouts in different directions
     *
     * @throws GameActionException
     */

    static void spawnScout() throws GameActionException {
        for (int i = 0; i < 8; i++) {
            if (!scoutsSpawned[i]) {
                if (Util.spawnBot(RobotType.MUCKRAKER, directions[i], 1)) {
                    scoutsSpawned[i] = true;
                    setFlagScout(directions[i]);
                }
                break;
            }
        }
    }

    /**
     * Sets the EC's flag when creating a scout
     * @throws GameActionException
     */
    static void setFlagScout(Direction dir) throws GameActionException {
        switch (dir) {
            case NORTH: Util.tryFlag(11); break;
            case NORTHEAST: Util.tryFlag(12); break;
            case EAST: Util.tryFlag(13); break;
            case SOUTHEAST: Util.tryFlag(14); break;
            case SOUTH: Util.tryFlag(15); break;
            case SOUTHWEST: Util.tryFlag(16); break;
            case WEST: Util.tryFlag(17); break;
            case NORTHWEST: Util.tryFlag(18); break;
            default:
                throw new IllegalStateException("Unexpected direction: " + dir);
        }
    }
}
