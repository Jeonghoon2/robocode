package JMTank;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class JMTank extends Bot {

    double closestEnemyX, closestEnemyY;  // 가장 가까운 적의 위치 저장 변수
    double closestEnemyDistance = Double.MAX_VALUE;  // 가장 가까운 적과의 거리
    boolean radarLocked = false;  // 레이더가 적에게 고정됐는지 여부
    boolean movingForward = true;  // 봇이 앞으로 이동 중인지 여부

    // 메인 메서드에서 봇을 시작
    public static void main(String[] args) {
        new JMTank().start();
    }

    // 생성자, 봇의 설정 파일 로드
    JMTank() {
        super(BotInfo.fromFile("JMTank.json"));
    }

    // 게임이 시작될 때 호출됨 -> 초기화
    @Override
    public void run() {
        setColors();

        while (isRunning()) {
            if (!radarLocked) {
                // 적이 스캔되지 않았으면 레이더를 계속 회전
                turnRadarRight(360);
            } else {
                // 적이 잠기면 계속해서 적을 추적
                moveToEnemy();
            }
        }
    }

    // 색상 설정
    private void setColors() {
        setBodyColor(Color.fromString("#0040FF"));
        setTurretColor(Color.fromString("#0080FF"));
        setRadarColor(Color.fromString("#FFFF00"));
        setScanColor(Color.fromString("#FF0000"));
        setBulletColor(Color.fromString("#00FF00"));
    }

    // 적과의 거리를 계산하는 메서드
    private double distanceToEnemy(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // 적의 위치로 이동하는 메서드
    private void moveToEnemy() {
        double distance = distanceToEnemy(closestEnemyX, closestEnemyY);
        double angleToEnemy = calculateAngleToEnemy();

        // 적을 향해 회전
        turnRight(angleToEnemy);

        // 적이 너무 멀면 접근, 너무 가까우면 후퇴
        if (distance > 200) {
            setForward(150);  // 적에게 접근
        } else if (distance < 100) {
            setBack(100);  // 적에게서 물러남
        }

        // 불규칙 회전으로 회피 기동
        if (Math.random() > 0.7) {
            turnRight(30 + Math.random() * 40);  // 불규칙하게 회전
        }
    }

    // 적과의 각도 계산
    private double calculateAngleToEnemy() {
        double dx = closestEnemyX - getX();
        double dy = closestEnemyY - getY();
        return Math.toDegrees(Math.atan2(dy, dx)) - getDirection();
    }

    // 적을 스캔하면 호출 -> 가장 가까운 적을 추적, 공격 및 레이더 고정
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        double scannedEnemyX = e.getX();
        double scannedEnemyY = e.getY();
        double distanceToScannedEnemy = distanceToEnemy(scannedEnemyX, scannedEnemyY);

        // 가장 가까운 적을 갱신
        if (distanceToScannedEnemy < closestEnemyDistance) {
            closestEnemyX = scannedEnemyX;
            closestEnemyY = scannedEnemyY;
            closestEnemyDistance = distanceToScannedEnemy;
        }

        // 총구와 레이더를 가장 가까운 적에게 고정
        double gunBearing = gunBearingTo(closestEnemyX, closestEnemyY);
        double radarBearing = radarBearingTo(closestEnemyX, closestEnemyY);

        // 적을 계속 추적하며 레이더를 적이 움직일 범위에 맞게 회전
        double extraTurn = radarBearing - getRadarTurnRemaining();
        turnRadarRight(extraTurn);  // 적을 놓치지 않게 레이더 고정

        radarLocked = true;  // 레이더가 적에게 고정됨

        turnGunLeft(gunBearing);  // 총구 회전

        // 적이 가까이 있으면 최대 파워로 공격
        if (Math.abs(gunBearing) <= 3 && getGunHeat() == 0) {
            fire(3);
        } else if (getGunHeat() == 0) {
            fire(1);  // 먼 거리일 때는 낮은 파워로 공격
        }

        // 적을 놓치지 않기 위해 레이더를 다시 돌림
        rescan();
    }

    // 레이더 방향 조정
    public double radarBearingTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.toDegrees(Math.atan2(dy, dx)) - getRadarDirection();
    }

    @Override
    public void onBotDeath(BotDeathEvent botDeathEvent) {
        radarLocked = false;
        closestEnemyDistance = Double.MAX_VALUE;  // 적이 사라졌을 때 거리 초기화
        turnRadarRight(360);  // 적을 다시 찾기 위해 레이더 회전
    }


    // 벽에 부딪혔을 때 호출 -> 반대 방향으로 이동
    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(100);  // 벽에 부딪히면 후퇴
        movingForward = !movingForward;  // 이동 방향 반전
    }

    // 라운드에서 승리했을 때 -> 승리 춤
    @Override
    public void onWonRound(WonRoundEvent e) {
        for (int i = 0; i < 3; i++) {
            turnRight(360);  // 승리 후 360도 회전
        }
    }
}
