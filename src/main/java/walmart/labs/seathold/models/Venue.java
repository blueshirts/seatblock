package walmart.labs.seathold.models;

public class Venue {
    private String name;
    private int seatsPerRow;
    private int rows;

    public Venue(int seatsPerRow, int rows) {
        this.seatsPerRow = seatsPerRow;
        this.rows = rows;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public int getRows() {
        return rows;
    }

    public String toString() {
        return String.format("Venue(seatsPerRow: %d, rows: %d)", this.seatsPerRow, this.rows);
    }
}
