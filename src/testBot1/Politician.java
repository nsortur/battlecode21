package testBot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
            */
        List bots = Arrays.asList(rc.senseNearbyRobots(4, rc.getTeam().opponent())); // convert to arraylist for easier use
        ArrayList<RobotType> types = new ArrayList<RobotType>();
        // turn the list into the types of the robots

        if(types.contains(RobotType.ENLIGHTENMENT_CENTER)){
            System.out.println("Contains");
            if (rc.canEmpower(6)){
                rc.empower(6);
            }

        }
        Util.optimalGo(new MapLocation(10026, 23948));

    }
}
