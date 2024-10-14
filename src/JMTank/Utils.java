package JMTank;

public class Utils {
    /**
     * 정규화된 각도를 -180과 +180 사이의 값으로 반환합니다.
     * @param angleDegrees 입력 각도(도 단위).
     * @return 정규화된 각도.
     */
    public static double normalRelativeAngleDegrees(double angleDegrees) {
        while (angleDegrees > 180) angleDegrees -= 360;
        while (angleDegrees <= -180) angleDegrees += 360;
        return angleDegrees;
    }
}