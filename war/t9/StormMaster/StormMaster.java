package StormMaster;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class StormMaster extends Bot {

    boolean radarLocked = false;
    double enemyX, enemyY;  // 적의 위치 저장 변수
    double enemyHeading;    // 적의 진행 방향
    double enemyDistance;   // 적과의 거리 저장 변수

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

        // Main loop - constantly scanning and moving
        while (isRunning()) {
            if (!radarLocked) {
                // 적이 스캔되지 않았으면 레이더를 계속 회전
                turnRadarRight(360); // 적을 찾기 위해 레이더를 회전시킴
            } else {
                // 적이 스캔되었을 때, 적 뒤를 추적
                moveToBehindEnemy();
            }
        }
    }

    // 적과의 거리를 계산하는 메서드
    private double distanceToEnemy(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // 적의 뒤로 이동하는 메서드
    private void moveToBehindEnemy() {
        enemyDistance = distanceToEnemy(enemyX, enemyY);

        // 적의 뒤쪽에 위치하려면, 적의 진행 방향을 기준으로 180도 회전
        double angleToEnemyRear = normalizeAngle(enemyHeading + 180 - getDirection());

        // 적의 뒤쪽으로 회전
        if (angleToEnemyRear > 0) {
            setTurnRight(angleToEnemyRear);
        } else {
            setTurnLeft(-angleToEnemyRear);
        }

        // 적에게 가까워지면서 일정 거리 유지 (최대 50의 거리)
        if (enemyDistance > 50) {
            setForward(100);  // 적에게 더 가까이 이동
        } else if (enemyDistance < 30) {
            setBack(50);    // 너무 가까우면 살짝 후퇴
        }

        // 레이더를 적에게 고정
        double radarBearing = radarBearingTo(enemyX, enemyY);
        turnRadarRight(radarBearing - getRadarTurnRemaining());  // 레이더 고정

        waitFor(new TurnCompleteCondition(this));  // 회전이 완료될 때까지 대기
    }

    // 각도를 360도 범위로 정규화하는 메서드
    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // 적을 스캔하면 호출 -> 적의 위치 기록 및 레이더 고정
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        enemyX = e.getX();
        enemyY = e.getY();
        enemyHeading = e.getDirection();  // 적의 진행 방향 저장
        radarLocked = true;  // 레이더 잠금

        // 총구를 적에게 조준
        double gunBearing = gunBearingTo(enemyX, enemyY);
        turnGunLeft(gunBearing);

        // 총열이 적을 향하고 있다면 발사
        if (Math.abs(gunBearing) <= 3 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(gunBearing), getEnergy() - .1));
        }

        // 적이 가까워졌을 때 다시 스캔
        if (Math.abs(gunBearing) == 0) {
            rescan();  // 적을 놓치지 않도록 다시 스캔
        }
    }

    // 레이더 방향 조정
    public double radarBearingTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.toDegrees(Math.atan2(dy, dx)) - getRadarDirection();
    }

    // 벽에 부딪혔을 때 호출 -> 이동 방향 반전
    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(100); // 벽에 부딪히면 후퇴
    }

    // 다른 로봇에 부딪혔을 때 호출 -> 이동 방향 반전
    @Override
    public void onHitBot(HitBotEvent e) {
        if (e.isRammed()) {
            setBack(100); // 적과 충돌 시 후퇴
        }
    }

    // 회전이 완료되었는지 확인하는 조건 클래스
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
