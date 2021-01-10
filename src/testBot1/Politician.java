package testBot1;

import battlecode.common.*;

import java.util.ArrayList;

public class Politician extends RobotPlayer {

    static void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        int convic = rc.getConviction();
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);

        /*
        if (attackable.length != 0 && rc.canEmpower(actionRadius) && convic > 10) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        */
        ArrayList<RobotType> types = new ArrayList<>();
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == enemy) {
               types.add(robot.type);
            }
        }
        System.out.println(types.toString());
        if(types.contains(RobotType.ENLIGHTENMENT_CENTER)){
            System.out.println("Contains");
            if (rc.canEmpower(4)){
                rc.empower(4);
            }

        }

        Util.greedyPath(new MapLocation(10026, 23926));

    }
}
