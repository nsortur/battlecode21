package testBot1;
import battlecode.common.*;

import java.util.ArrayList;


public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;
    static int numMuckrakers = 0;
    static int numSlanderers = 0;
    static int numPoliticians = 0;
    static int numEnlightenmentCenters; // figure out how to calculate this value (kind of did)

    // do we need the location of our own EC's?

    static ArrayList<MapLocation> enemyEC = new ArrayList<>(); // total number of potential enemy ec's
    static int[] flagEC; // the integer value of the EC's flag


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        // System.out.println("I'm a " + rc.getType() + " and I just got created! The turn count is " + turnCount);
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: EC.run(); break;
                    case POLITICIAN:           Politician.run();          break;
                    case SLANDERER:            Slanderer.run();           break;
                    case MUCKRAKER:            Muckraker.run();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }


}
