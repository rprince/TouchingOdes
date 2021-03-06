package U_1F47B;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import robocode.*;

public class U_1F47B extends RateControlRobot {

    private class StrategyComponents {
        public Radar radar;
        public Gun gun;
        public Base base;

        public StrategyComponents(Radar radar, Gun gun, Base base) {
            this.radar = radar;
            this.gun = gun;
            this.base = base;
        }
    }

    public static enum Strategy {
        MELEE, ONEVSONE
    }

    private Map<Strategy, U_1F47B.StrategyComponents> strategyMap;

    private State state;
    // TODO: set these at some point
    private Radar radar;
    private Gun gun;
    private Base base;

    public U_1F47B() {
        this.state = new State(this);
        this.initStrategies(this.state);
        this.updateStrategy(Strategy.MELEE);
    }

    private void initStrategies(final State state) {
        this.strategyMap = new HashMap<Strategy, U_1F47B.StrategyComponents>() {{

            put(Strategy.MELEE, new U_1F47B.StrategyComponents(
                    new PriorityRadar(state),
                    new PredictiveGun(state, 1.0),
                    new PredictiveBase(state)
            ));

            put(Strategy.ONEVSONE, new U_1F47B.StrategyComponents(
                    new TrackingRadar(state, 2.0),
                    new PredictiveGun(state, 1.0),
                    null
            ));

        }};
    }

    public void updateStrategy(Strategy strategy) {
        U_1F47B.StrategyComponents components = this.strategyMap.get(strategy);

        if (components.radar != null)
            this.radar = components.radar;
        if (components.gun != null)
            this.gun = components.gun;
        if (components.base != null)
            this.base = components.base;
    }

    @Override
    public void run() {
        setColors(new Color(0, 40, 43),
                  new Color(0, 96, 102),
                  new Color(0, 120, 128));
        setBulletColor(new Color(70, 77, 106));

        // we want to control the radar manually
        this.setAdjustRadarForRobotTurn(true);
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);

        this.state.battleWidth  = this.getBattleFieldWidth();
        this.state.battleHeight = this.getBattleFieldHeight();

        while (true) {
            // switch to 1vs1 Components when only one other is left
            if (this.getOthers() == 1) {
                this.updateStrategy(Strategy.ONEVSONE);
            }

            this.state.advance();

            this.radar.execute();
            this.gun.execute();
            this.base.execute();

            this.setRadarRotationRate(this.radar.getRotation());

            this.setGunRotationRate(this.gun.getRotation());

            this.setTurnRate(this.base.getRotation());
            this.setVelocityRate(this.base.getSpeed());

            if (this.gun.getShouldFire() && this.getGunHeat() == 0) {
                Bullet bullet = this.setFireBullet(this.gun.getBulletPower());
                TrackedBullet tb = new TrackedBullet(bullet);
                this.gun.firedBullet(tb);
                this.state.ourBullets.add(tb);
                System.out.println("addbull");
            }
            this.execute();
        }
    }

    @Override
    public void onBattleEnded(BattleEndedEvent e) {
        System.out.println("The battle is over: " + e);
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        System.out.println("Out bullet hit a robot: " + e);
        this.state.bulletHitEvents.add(e);
        
        //for (TrackedBullet tb: this.state.ourBullets)
        for (int i = this.state.ourBullets.size() - 1; i >= 0; i--)
        {
            TrackedBullet tb = this.state.ourBullets.get(i);
            if (tb.getBullet().hashCode() == e.getBullet().hashCode())
            {
                this.gun.bulletHit(tb);
                this.state.ourBullets.remove(i);
                return;
            }
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        System.out.println("Our bullet hit another: " + e);
        
        //for (TrackedBullet tb: this.state.ourBullets)
        for (int i = this.state.ourBullets.size() - 1; i >= 0; i--)
        {
            TrackedBullet tb = this.state.ourBullets.get(i);
            if (tb.getBullet().hashCode() == e.getBullet().hashCode())
            {
                this.state.ourBullets.remove(i);
                return;
            }
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        System.out.println("Our bullet missed: " + e);
        
        for (int i = this.state.ourBullets.size() - 1; i >= 0; i--)
        {
            TrackedBullet tb = this.state.ourBullets.get(i);
            if (tb.getBullet().hashCode() == e.getBullet().hashCode())
            {
                this.gun.bulletMissed(tb);
                this.state.ourBullets.remove(i);
                return;
            }
        }
    }

    @Override
    public void onDeath(DeathEvent e) {
        System.out.println("We've died: " + e);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        System.out.println("We've been hit: " + e);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        this.radar.onHitRobot(e);
        this.gun.onHitRobot(e);
        this.base.onHitRobot(e);

        this.state.hitRobotEvents.add(e);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        System.out.println("We've crashed into a wall: " + e);
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);

        this.radar.onPaint(g);
        this.gun.onPaint(g);
        this.base.onPaint(g);

        for(OtherRobot r : this.state.otherRobots.values()) {
            r.onPaint(g, this.getTime());
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        this.state.onRobotDeath(e);
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("The round is over: " + e);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        this.state.onScannedRobot(e);
    }

    @Override
    public void onWin(WinEvent e) {
        System.out.println("We won! " + e);
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("Oh no, we skipped a turn! " + e);
    }

}
