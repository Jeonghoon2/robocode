package JMTank;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class JMTank extends Bot {

    boolean movingForward = true;  // 봇이 앞으로 이동 중인지 여부
    double enemyX, enemyY;  // 타겟의 위치를 저장할 변수

    // 메인 메서드에서 봇을 시작
    public static void main(String[] args) {
        new JMTank().start();
    }

    // 생성자, 봇의 설정 파일을 로드
    JMTank() {
        super(BotInfo.fromFile("JMTank.json"));
    }

    // 새 라운드가 시작될 때 호출됨 -> 초기화 및 이동 시작
    @Override
    public void run() {
        // 색상 설정
        Color pink = Color.fromString("#FF69B4");
        setBodyColor(pink);
        setTurretColor(pink);
        setRadarColor(pink);
        setScanColor(pink);
        setBulletColor(pink);

        // 메인 루프 - 게임이 실행되는 동안 반복
        while (isRunning()) {
            // 적이 없으면 계속해서 레이더를 회전하며 적을 찾음
            if (enemyX == 0 && enemyY == 0) {
                turnRadarRight(360);  // 레이더를 계속 회전
            } else {
                // 적이 있으면 적을 쫓아가며 계속 스캔
                double angleToEnemy = calculateAngleToEnemy();
                turnRight(angleToEnemy);  // 적 방향으로 회전
                setForward(100);  // 적을 향해 전진
                waitFor(new TurnCompleteCondition(this));
            }
        }
    }

    // 적과의 각도 계산
    private double calculateAngleToEnemy() {
        double dx = enemyX - getX();
        double dy = enemyY - getY();
        return Math.toDegrees(Math.atan2(dy, dx)) - getDirection();
    }

    // 스캔된 봇이 있을 때 호출 -> 타겟을 추적하고 공격
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // 스캔된 적의 위치를 기록
        enemyX = e.getX();
        enemyY = e.getY();

        // 적과의 상대적 각도를 계산
        double bearingFromGun = gunBearingTo(enemyX, enemyY);
        turnGunLeft(bearingFromGun);  // 총구를 적 방향으로 회전

        // 총열이 적을 향하고 총이 준비되었을 때 공격
        if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - 0.1));
        }

        // 적을 놓치지 않기 위해 레이더를 다시 돌림
        rescan();
    }

    // 벽에 부딪혔을 때 호출 -> 이동 방향 전환
    @Override
    public void onHitWall(HitWallEvent e) {
        movingForward = !movingForward;  // 이동 방향을 반대로 전환
        if (movingForward) {
            setForward(100);  // 앞으로 이동
        } else {
            setBack(100);  // 뒤로 이동
        }
    }

    // 라운드에서 승리했을 때 -> 승리 춤
    @Override
    public void onWonRound(WonRoundEvent e) {
        for (int i = 0; i < 5; i++) {
            turnRight(360);
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
            return bot.getTurnRemaining() == 0;  // 남은 회전량이 0이면 회전 완료
        }
    }
}
