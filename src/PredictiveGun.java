package bot;

import robocode.*;
import robocode.util.*;

public class PredictiveGun extends Gun {

    private double coefficient;

    public PredictiveGun(State state, double coefficient) {
        super(state);
        this.coefficient = coefficient;
    }

    @Override
    public void execute() {
        // does not use coefficient
        if (this.state.trackingRobot != null) {
            OtherRobot.Tick tick = this.state.trackingRobot.getHistory(-1);
            
            // our velocity
            double mvX = Math.sin(this.state.owner.getHeadingRadians()) * this.state.owner.getVelocity();
            double mvY = Math.cos(this.state.owner.getHeadingRadians()) * this.state.owner.getVelocity();
            double locX = this.state.owner.getX() + mvX;
            double locY = this.state.owner.getY() + mvY;

            double dist = tick.distance;
            double projectileSpeed = 19.7; // need to be able to turn this into power

            double cutoff = 200.0;
            if (dist < cutoff)
            {
                projectileSpeed = (dist / cutoff) * 19.7;
            }
            this.bulletPower = Util.speedToFirepower(projectileSpeed);

            double timeSteps = dist / projectileSpeed;

            Vector predVec = this.state.trackingRobot.predictLocation((int)timeSteps + 1, OtherRobot.TurnBehaviours.keepTurn, OtherRobot.SpeedBehaviours.keepSpeed);

            double afterDist = Util.getDistance(locX - predVec.getX(), locY - predVec.getY());
            double afterTimeSteps = afterDist / projectileSpeed;

            double adqTimeSteps = (afterTimeSteps * 0.8 + timeSteps * 0.2); // take weighted average

            predVec = this.state.trackingRobot.predictLocation((int)adqTimeSteps + 1, OtherRobot.TurnBehaviours.keepTurn, OtherRobot.SpeedBehaviours.keepSpeed);

            double litDir = Util.getAngle(locX - predVec.getX(), locY - predVec.getY());

            double rotation = litDir - this.state.owner.getGunHeading(); // relative rotation to gun
            this.rotation = this.coefficient * Utils.normalRelativeAngleDegrees(rotation); // normalise
        } else {
            // leave the rotation as it is allready
        }
    }

}
