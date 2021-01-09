package testBot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static testBot1.Util.tryMove;

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
        for (int i = 0; i < bots.size(); i++) {
            RobotInfo type = (RobotInfo) bots.get(i);
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            types.add(type.getType());
            System.out.println(types.get(i));
        }
        if(types.contains(RobotType.ENLIGHTENMENT_CENTER)){
            System.out.println("Contains");
            // figure out empower
            if (rc.canEmpower(4)){
                System.out.println("can empower");
                rc.empower(4);
            }

        }
        tryMove(Direction.EAST);
    }
}
