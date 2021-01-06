package testBot1;
import battlecode.common.*;

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

    // Initial variables
    //Muckrakers
    static boolean goingNorth;
    static boolean goingEast;
    static boolean createdNorthMuck;
    static boolean createdEastMuck;


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        if (!(createdEastMuck && createdNorthMuck)) {
            if (!createdNorthMuck) {
                if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.NORTH, 1)) {
                    rc.buildRobot(RobotType.MUCKRAKER, Direction.NORTH, 1);
                    createdNorthMuck = true;
                }
            } else {
                if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.EAST, 1)) {
                    rc.buildRobot(RobotType.MUCKRAKER, Direction.EAST, 1);
                    createdEastMuck = true;
                }
            }
        }

    }


    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        MapLocation muckLeft = rc.adjacentLocation(Direction.WEST);
        MapLocation muckSouth = rc.adjacentLocation(Direction.SOUTH);
        RobotInfo leftRob = rc.senseRobotAtLocation(muckLeft);
        RobotInfo southRob = rc.senseRobotAtLocation(muckSouth);

        if (!(goingEast || goingNorth)) {
            if (leftRob != null && leftRob.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                goingEast = true;
            } else if (southRob != null && southRob.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                goingNorth = true;
            }
        }

        if (goingEast && tryMove(Direction.EAST)) {
            System.out.println("I moved east");
        } else if (goingNorth && tryMove(Direction.NORTH)){
            System.out.println("I moved north");
        }
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
}
