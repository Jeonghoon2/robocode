package JMTank;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class JMTank extends Bot {

    int moveDirection = 1; // 이동 방향
    private double gunBearing = 0; // 로봇의 총구 상대 방향


    public JMTank() {
        super(BotInfo.fromFile("JMTank.json"));
    }

    // 총구 방향을 가져오는 메서드
    public double getGunHeading() {
        return (this.getDirection() + gunBearing) % 360;
    }

    // 총구 방향을 설정하는 메서드
    public void setGunHeading(double angle) {
        gunBearing = angle - this.getDirection();
        gunBearing = gunBearing % 360;
        // 총구를 실제로 회전시키는 로직 필요 (API 제공 메서드 사용)
    }

    // 전진을 설정하는 메서드
    public void setAhead(double distance) {
        double radians = Math.toRadians(this.getDirection());
        double newX = this.getX() + distance * Math.cos(radians);
        double newY = this.getY() + distance * Math.sin(radians);
        // 실제로 로봇을 이동시키는 로직 필요 (API 제공 메서드 사용)
    }


    @Override
    public void run() {
        // 색상 설정
        Color bodyColor = Color.fromString("#808032"); // 128, 128, 50에 해당하는 16진수 색상 코드
        Color gunColor = Color.fromString("#323214"); // 50, 50, 20에 해당하는 16진수 색상 코드
        Color radarColor = Color.fromString("#C8C846"); // 200, 200, 70에 해당하는 16진수 색상 코드
        Color scanColor = Color.WHITE;
        Color bulletColor = Color.BLUE;

        setBodyColor(bodyColor);
        setTurretColor(gunColor);
        setRadarColor(radarColor);
        setScanColor(scanColor);
        setBulletColor(bulletColor);

        setAdjustRadarForGunTurn(true); // 총 회전시 레이더 조정
        setAdjustGunForBodyTurn(true); // 몸 회전시 총 조정
        turnRadarRight(Double.POSITIVE_INFINITY); // 레이더를 계속 오른쪽으로 회전
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Calculate the angle to the scanned bot
        double angleToEnemy = Math.toDegrees(Math.atan2(e.getY() - this.getY(), e.getX() - this.getX()));
        double bearingFromGun = Utils.normalRelativeAngleDegrees(angleToEnemy - getGunHeading());

        // Calculate the distance to the scanned bot
        double distance = Math.sqrt(Math.pow(e.getX() - this.getX(), 2) + Math.pow(e.getY() - this.getY(), 2));

        // Turn radar towards the scanned bot
        turnRadarLeft(getRadarTurnRemaining());

        // Random movement amount
        double randomMovement = (Math.random() > 0.5) ? 100 : -100;

        // Decide the gun turn amount and move accordingly
        double gunTurnAmt;
        if (distance > 150) {
            gunTurnAmt = Utils.normalRelativeAngleDegrees(bearingFromGun + e.getSpeed() / 22);
            turnGunRight(gunTurnAmt);
            turnRight(Utils.normalRelativeAngleDegrees(bearingFromGun));
            setAhead((distance - 140 + randomMovement) * moveDirection);
            fire(3);
        } else {
            gunTurnAmt = Utils.normalRelativeAngleDegrees(bearingFromGun + e.getSpeed() / 15);
            turnGunRight(gunTurnAmt);
            turnLeft(-90 - e.getDirection());
            setAhead((distance - 140 + randomMovement) * moveDirection);
            fire(3);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection; // 벽에 부딪혔을 때 방향 전환
    }

    public static void main(String[] args) {
        new JMTank().start();
    }
}
