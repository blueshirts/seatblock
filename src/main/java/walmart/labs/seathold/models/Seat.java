package walmart.labs.seathold.models;

public class Seat implements Comparable {
    private int seat;
    private int row;
    private float score;
    private String code;

    public Seat(int seat, int row, float score) {
        this.seat = seat;
        this.row = row;
        this.score = score;
        this.code = String.format("%d-%d", this.seat, this.row);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Seat) {
            return Float.compare(this.score, ((Seat)o).score) * -1;
        } else {
            throw new IllegalArgumentException(o.getClass().getName() + " cannot be compared to " +
                    Seat.class.getName());
        }
    }

    public int getSeat() {
        return seat;
    }

    public int getRow() {
        return row;
    }

    public float getScore() {
        return score;
    }

    public String toString() {
        return String.format("Seat(%d, %d, %.2f)", this.seat, this.row, this.score);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Seat) {
            return this.code.equals(((Seat) o).code);
        } else {
            throw new IllegalArgumentException(String.format("Invalid parameter type: %s", o.getClass().getName()));
        }
    }
}
