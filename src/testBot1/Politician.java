package testBot1;

import battlecode.common.*;

import java.util.ArrayList;

public class Politician extends RobotPlayer {

    static MapLocation ECLocation = rc.adjacentLocation(Direction.WEST);
    static int count = 0;
    static void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        int convic = rc.getConviction();


        if (count < 30){
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (attackable.length > 0 && robot.team == enemy) {
                    if (rc.canEmpower(actionRadius)){
                        rc.empower(actionRadius);
                    }
                }
            }

            Direction direction = directions[(int) (Math.random() * directions.length)];
            if(rc.getLocation().add(direction).isWithinDistanceSquared(ECLocation, 15)){
                if(rc.canMove(direction)){
                    count++;
                    rc.move(direction);
                }
            }

        }
        else{

            Util.greedyPath(new MapLocation(25064, 12919));
        }


    }
}