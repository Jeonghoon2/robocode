package StormMaster;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class StormMaster extends Bot {

    boolean movingForward;

    public static void main(String[] args) {
        new StormMaster().start();
    }

    StormMaster() {
        super(BotInfo.fromFile("StormMaster.json"));
    }

    @Override
    public void run() {
        // Set combined colors
        setBodyColor(Color.fromString("#00C800"));   // lime
        setTurretColor(Color.fromString("#FF69B4")); // pink
        setRadarColor(Color.fromString("#FF69B4"));  // pink
        setBulletColor(Color.fromString("#FFFF64")); // yellow
        setScanColor(Color.fromString("#FFC8C8"));   // light red

        // Start Crazy movement
        while (isRunning()) {
            // Move ahead 40000 units
            setForward(40000);
            movingForward = true;

            // Turn right 90 degrees
            setTurnRight(90);
            waitFor(new TurnCompleteCondition(this));

            // Turn left 180 degrees
            setTurnLeft(180);
            waitFor(new TurnCompleteCondition(this));

            // Turn right 180 degrees
            setTurnRight(180);
            waitFor(new TurnCompleteCondition(this));
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // TrackFire: Aim and fire at nearest bot
        var bearingFromGun = gunBearingTo(e.getX(), e.getY());
        turnGunLeft(bearingFromGun);

        if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
        }

        // Rescan if the gun is aligned with the bot
        if (bearingFromGun == 0) {
            rescan();
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        reverseDirection();
    }

    @Override
    public void onHitBot(HitBotEvent e) {
        // If we rammed into the bot, reverse direction
        if (e.isRammed()) {
            reverseDirection();
        }
    }

    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setForward(40000);
            movingForward = true;
        }
    }

    // Condition that is triggered when the turning is complete
    public static class TurnCompleteCondition extends Condition {

        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            return bot.getTurnRemaining() == 0;
        }
    }
}
