package walmart.labs.seathold.models;

public class Seat implements Comparable {
    private int seat;
    private int row;
    private float score;

    public Seat(int seat, int row, float score) {
        this.seat = seat;
        this.row = row;
        this.score = score;
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

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public float getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String toString() {
        return String.format("Seat(%d, %d, %.2f)", this.seat, this.row, this.score);
    }
}
