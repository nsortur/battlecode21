package testBot1;

import battlecode.common.*;

public class Politician extends RobotPlayer {

    static MapLocation ECLocation = rc.adjacentLocation(Direction.WEST);
    static int roundsMoving = 0;
    static void run() throws GameActionException {
        if (rc.getRoundNum() < 1000){ // defend for 1000 rounds
            defend();
        }
        else{
            Direction direction = directions[(int) (Math.random() * directions.length)];
                if(rc.canMove(direction)){
                    rc.move(direction);
                }
            }
    }


    public static void defend() throws GameActionException {
        //constantly checks if theres an enemy bot
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] robots = rc.senseNearbyRobots(actionRadius);
        for (RobotInfo robot : robots) {
            if (robot.team == enemy) {
                if (rc.canEmpower(actionRadius)){
                    rc.empower(actionRadius);
                }
            }
        }

        Direction direction = directions[(int) (Math.random() * directions.length)];
        if(rc.getLocation().add(direction).isWithinDistanceSquared(ECLocation, 15)){
            if(rc.canMove(direction)){
                rc.move(direction);
            }
        }
        // moves in random directions


        roundsMoving++;
    }
}