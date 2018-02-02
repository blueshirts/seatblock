package walmart.labs.seathold.common;

public class SeatHoldUtils {

    /**
     * Round the float to two decimal places.
     * @param f - the number.
     * @return a float rounded to two decimal places.
     */
    public static float round(float f) {
        return (float)Math.round(f * 100.0f) / 100.0f;
    }
}
