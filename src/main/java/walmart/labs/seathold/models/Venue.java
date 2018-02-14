package walmart.labs.seathold.models;

/**
 * The Venue class is an abstraction of venue or hall that has seating.  The Venue describes the details
 * of the number of seats and the general layout.
 */
public class Venue {
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

    public int getMaxSeats() {
        return this.seatsPerRow * rows;
    }

    public String toString() {
        return String.format("Venue(seatsPerRow: %d, rows: %d)", this.seatsPerRow, this.rows);
    }
}
