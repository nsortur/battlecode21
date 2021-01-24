package notABotJustTest;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class Muckraker extends RobotPlayer {
    static int ecID;
    static MapLocation ecLoc;
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static void run() throws GameActionException {
        if (turnCount == 1) {
            ecID = Util.getECID();
            ecLoc = Util.locationOfFriendlyEC();
        }
        System.out.println(Clock.getBytecodesLeft() + " before");

        System.out.println(Clock.getBytecodesLeft() + " after");

        tryKillSlanderer();
    }

    /**
     * Kills a slanderer if in action radius
     *
     * @return returns true if killed a slanderer
     * @throws GameActionException
     */
    static boolean tryKillSlanderer() throws GameActionException {
        boolean killedSlanderer = false;

        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius); // sense all robots in action radius
        for (RobotInfo robot: robots) {
            if (robot.type == RobotType.SLANDERER && robot.team != rc.getTeam()) {
                if (rc.canExpose(robot.location)) rc.expose(robot.location);
                // TODO: ADD FLAG THAT TELLS EC KILLED SLANDERER **
                killedSlanderer = true;
            }
        }
        return killedSlanderer;
    }

    /**
     * Checks to see if a robot is close to an EC, and if it is sets the robot's flag
     *
     * @return true if the robot is close to an ec
     * @throws GameActionException
     */
    static boolean isCloseToEnemyEC() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam().opponent()) {
                MapLocation enemyECLoc = robot.location;

                int x_offset = enemyECLoc.x - ecLoc.x;
                int y_offset = enemyECLoc.y - ecLoc.y;

                if (Util.trySetFlag(Util.encryptOffsets(x_offset, y_offset, 1))) // System.out.println("Flag set!");

                    return true;
            }
        }
        return false;
    }

}
