package bot;

import java.util.Map;
import java.util.HashMap;
import robocode.*;

public class State {

    Robot owner;

    Map<String, OtherRobot> otherRobots;
    OtherRobot latestRobot;

    public State(Robot robot) {
        this.owner = robot;
        this.otherRobots = new HashMap<String, OtherRobot>();
        this.latestRobot = null;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        OtherRobot robot = this.otherRobots.get(e.getName());
        if (robot == null) {
            robot = new OtherRobot(e.getName());
            this.otherRobots.put(e.getName(), robot);
        }

        OtherRobot.Tick tick = new OtherRobot.Tick(this.owner.getTime());
        tick.bearing = e.getBearing();
        tick.distance = e.getDistance();
        tick.energy = e.getEnergy();
        tick.position = this.calculatePosition(tick.bearing, tick.distance);
        robot.pushHistory(tick);
        tick.velocity = State.calculateVelocity(robot.getHistory(-2).position, tick.position);
        this.latestRobot = robot;

        robot.predictBulletShot(this.owner.getTime());
    }

    private Vector calculatePosition(double bearing, double distance) {
        double angleR = Math.toRadians(bearing + this.owner.getHeading());
        double x = this.owner.getX() + Math.sin(angleR) * distance;
        double y = this.owner.getY() + Math.cos(angleR) * distance;
        return new Vector(x, y);
    }

    private static Vector calculateVelocity(Vector newPosition, Vector oldPosition) {
        return new Vector(newPosition.getX() - oldPosition.getX(),
                          newPosition.getY() - oldPosition.getY());
    }

}
