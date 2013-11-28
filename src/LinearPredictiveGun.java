package bot;

import robocode.*;
import robocode.util.*;

public class LinearPredictiveGun extends Gun {

    private double coefficient;

    public LinearPredictiveGun(State state, double coefficient) {
        super(state);
        this.coefficient = coefficient;
    }

    public double getDist(double ax, double ay, double bx, double by)
    {
        double x = ax - bx;
        double y = ay - by;
        return Math.sqrt(x * x + y * y);
    }

    public double getAngle(double ax, double ay, double bx, double by)
    {
        double x = ax - bx;
        double y = ay - by;
        return -90 - Math.toDegrees(Math.atan2(y, x));
    }

    @Override
    public void execute() {
        // does not use coefficient
        if(this.state.latestRobot != null) {
            OtherRobot.Tick tick = this.state.latestRobot.getHistory(-1);

            // need a way to approximate velocity using history
            // if no history, wait a bit so we have some?
            double velX = tick.velocity.getX(); // target velocity
            double velY = tick.velocity.getY();

            // out velocity
            double mvX = Math.sin(this.state.owner.getHeadingRadians()) * this.state.owner.getVelocity();
            double mvY = Math.cos(this.state.owner.getHeadingRadians()) * this.state.owner.getVelocity();
            double locX = this.state.owner.getX() + mvX;
            double locY = this.state.owner.getY() + mvY;
            double targX = tick.position.getX() + velX;
            double targY = tick.position.getY() + velY;

            double dist = tick.distance;
            double projectileSpeed = 19.7; // need to be able to turn this into power

            double cutoff = 200.0;
            if (dist < cutoff)
            {
                projectileSpeed = (dist / cutoff) * 19.7;
            }
            this.bulletPower = Util.speedToFirepower(projectileSpeed);

            double timeSteps = dist / projectileSpeed;

            double afterDist = getDist(locX, locY, targX + velX * timeSteps, targY + velY * timeSteps);
            double afterTimeSteps = afterDist / projectileSpeed;

            double adqTimeSteps = (afterTimeSteps + timeSteps) * 0.5;

            double litDir = getAngle(locX, locY, targX + velX * adqTimeSteps, targY + velY * adqTimeSteps);

            double rotation = litDir - this.state.owner.getGunHeading(); // relative rotation to gun
            this.rotation = this.coefficient * Utils.normalRelativeAngleDegrees(rotation); // normalise
        } else {
            // leave the rotation as it is allready
        }
    }

}
