package testBot1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Util extends RobotPlayer {
    /**
     * Spawns a bot for an enlightenment center
     *
     * @param type: type to spawn
     * @param dir: direction to spawn in
     * @param influence: influence to transfer to bot
     *
     * @return true if spawned
     * @throws GameActionException
     */
    static boolean spawnBot(RobotType type, Direction dir, int influence) throws GameActionException{
        if (!rc.getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
            System.out.println("not EC, trying to spawn from " + rc.getType());
        }
        if (rc.canBuildRobot(type, dir, influence)) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else return false;
    }


    /**
     * Moves to destination disregarding tile passibility
     *
     * @param dest: Destination to move towards
     *
     * @return true if destination is reached
     * @throws GameActionException
     */
    static boolean moveNaive(MapLocation dest) throws GameActionException{
        MapLocation curLoc = rc.getLocation();

        if (!curLoc.equals(dest)){
            Direction toDest = curLoc.directionTo(dest);
            tryMove(toDest);
            return false;

        } else return true;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     *
     * @throws GameActionException
     */
    static void findEnemyEC() throws GameActionException {
        // search surroundings, if found EC then report it
        // if does not find EC, get from flags
        // so --> if team ec says we have ecs in messages then get it from the flag (location to)
        // and if the team ec does not we need to keep searching (so move forward)
    }

    static void getNumEC() throws GameActionException {
        // check flag - if it says number of ec's return that
        // if it does not then equal it to robot count (since this needs to happen immediately)
    }

}
