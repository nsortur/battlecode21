package testBot1;

import battlecode.common.*;

import java.util.ArrayList;

public class Politician extends RobotPlayer {
    static boolean attackingEC;

    // our enlightenment center's ID (one muckraker spawned from)
    static int ecID;

    static final Team enemy = rc.getTeam().opponent();
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static void run() throws GameActionException {
        if (ecID == 0) {
            ecID = Util.getECID();
        } else {
            // once we have the home EC's ID
            if (Util.tryGetFlag(ecID) == 24) {
                attackingEC = true;
            }
        }
        if (attackingEC) {
            attackEnemyEC();
        }

        int convic = rc.getConviction();
        // Util.greedyPath(new MapLocation(25064, 12919));

    }

    static void attackEnemyEC() throws GameActionException{

        /**
         * get location from ec arraylist
         * calculate conviction to give (40% of EC conviction each for two politicians, 1% for a muckraker)
         * destroyedEC stop attacking if it's destroyed
         * if !destroyedEC, greedyMove to EC location while searching to empower if enlightenment center is seen
         *      if rounds have been more than 50, spawn another army and send
         * if destroyedEC, break and set attacking to false
         */
        // for loop later

        MapLocation toAttack = enemyECLocs.get(0);
        Util.greedyPath(toAttack);

        // attack and raised killed flag if about to kill
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        for (RobotInfo robot : attackable) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(actionRadius)) {
                Util.trySetFlag(25);

                // wait for EC to read flag that politician is gonna die
                Clock.yield();
                attackingEC = false;
                rc.empower(actionRadius);
            }
        }

    }

}
