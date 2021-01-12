package testBot1;

import battlecode.common.*;

public class Politician extends RobotPlayer {
    
    static final Team enemy = rc.getTeam().opponent();
    static final int actionRadius = rc.getType().actionRadiusSquared;

    static void run() throws GameActionException {
        
    }

    public static void defend() throws GameActionException {
        //constantly checks if theres an enemy bot
        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius, enemy);
        for (RobotInfo robot : robots) {
            if (rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
            }
        }
        // moves in random directions
        Direction direction = directions[(int) (Math.random() * directions.length)];
        if(rc.getLocation().add(direction).isWithinDistanceSquared(new MapLocation(0, 0), 15)){ // use friendly EC location instead
            if(rc.canMove(direction)){
                rc.move(direction);
            }
        }
    }
}
