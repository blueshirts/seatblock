package walmart.labs.seathold.models;

import java.util.List;

/**
 * The SeatHold interface represents a set of seats that are currently held for a customer.
 */
public interface SeatHold {
    int getId();
    String getEmail();
    List<Seat> getSeats();
    int size();
}
