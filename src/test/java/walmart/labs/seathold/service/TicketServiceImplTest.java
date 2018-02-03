package walmart.labs.seathold.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walmart.labs.seathold.errors.NoSuchSeatHoldException;
import walmart.labs.seathold.models.Seat;
import walmart.labs.seathold.models.SeatHold;
import scoring.MiddleOutScorer;
import walmart.labs.seathold.models.Venue;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {
    private static final Logger LOG = Logger.getLogger(TicketServiceImplTest.class.getName());

    private static final String EMAIL1 = "email1@email.com";

    private MiddleOutScorer scorer;

    @BeforeEach
    void setUp() {
        this.scorer = new MiddleOutScorer();
    }

    @Test
    void createService() {
        Venue venue = new Venue(8, 4);
        TicketService service = new TicketServiceImpl(venue, scorer);
        //LOG.info(service.toString());
    }

    @Test
    void hold_noSeatsAvailable() {
        final int seats = 0;
        final int rows = 0;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);
        SeatHold hold = service.findAndHoldSeats(5, EMAIL1);
        assertNull(hold);
    }

    @Test
    void holdSeats_notEnoughAvailable() {
        final int seats = 2;
        final int rows = 2;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);
        SeatHold hold = service.findAndHoldSeats(5, EMAIL1);
        assertNull(hold);
    }

    @Test void holdSeats_largeOrder() {
        final int seats = 2;
        final int rows = 10;
        final int orderSize = 5;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);

        SeatHold hold = service.findAndHoldSeats(orderSize, EMAIL1);
        assertSeatHold(hold, EMAIL1);
        assertEquals(orderSize, hold.size());
    }

    @Test
    void holdSeats_8x4() {
        final int totalSeats = 32;
        Venue venue = new Venue(8, 4);
        TicketService service = new TicketServiceImpl(venue, scorer);

        // LOG.info(service.toString());

        // Hold seats.
        SeatHold hold = service.findAndHoldSeats(2, EMAIL1);

        // Debug.
        // LOG.info(service.toString());
        // LOG.info(hold.toString());

        assertSeatHold(hold, EMAIL1, 3, 4);
        assertEquals(totalSeats - 2, service.numSeatsAvailable());
        hold = service.findAndHoldSeats(2, EMAIL1);

        // Debug.
        // LOG.info(service.toString());
        // LOG.info(hold.toString());

        assertSeatHold(hold, EMAIL1, 1, 2);
        assertEquals(totalSeats - 4, service.numSeatsAvailable());
    }

    @Test
    void holdSeats_50x100() {
        final int seats = 50;
        final int rows = 100;
        final int totalSeats = seats * rows;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);

        // LOG.info(service.toString());

        final int blockSize = 2;
        for (int seat = 2; seat < totalSeats; seat += blockSize) {
            SeatHold hold = service.findAndHoldSeats(blockSize, EMAIL1);
            assertSeatHold(hold, EMAIL1);

            // LOG.info(service.toString());
            // LOG.info(hold.toString());

            // LOG.info("Current number of seats available: " + service.numSeatsAvailable());
            assertEquals(totalSeats - seat, service.numSeatsAvailable());
        }
    }

    @Test
    void reserveSeats_2() {
        final int seats = 10;
        final int rows = 2;
        final int totalSeats = seats * rows;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);

        // Hold the seats.
        SeatHold hold = service.findAndHoldSeats(2, EMAIL1);
        assertSeatHold(hold, EMAIL1);
        // The seats should have been removed.
        assertEquals(totalSeats - 2, service.numSeatsAvailable());

        // Reserve the seats.
        String confirmation = service.reserveSeats(hold.getId(), EMAIL1);
        assertNotNull(confirmation);

        // There should still be the same amount of seats available.
        assertEquals(totalSeats - 2, service.numSeatsAvailable());
    }

    @Test
    void reserveSeats_all() {
        final int seats = 50;
        final int rows = 100;
        final int totalSeats = seats * rows;
        Venue venue = new Venue(seats, rows);
        TicketService service = new TicketServiceImpl(venue, scorer);

        final int blockSize = 2;
        for (int seat = 2; seat < totalSeats; seat += blockSize) {
            SeatHold hold = service.findAndHoldSeats(blockSize, EMAIL1);
            assertSeatHold(hold, EMAIL1);
            assertEquals(totalSeats - seat, service.numSeatsAvailable());
            String confirmation = service.reserveSeats(hold.getId(), EMAIL1);
            assertNotNull(confirmation);
            assertEquals(totalSeats - seat, service.numSeatsAvailable());
        }
    }

    @Test
    void reserveSeats_invalid() {
        try {
            final int seats = 10;
            final int rows = 2;
            Venue venue = new Venue(seats, rows);
            TicketService service = new TicketServiceImpl(venue, scorer);

            service.reserveSeats(12345, EMAIL1);
            Assertions.fail("NoSuchSeatHoldException should have been thrown.");
        } catch (NoSuchSeatHoldException e) {
            assertNotNull(e);
        }
    }

    private void assertSeatHold(SeatHold hold, String email) {
        this.assertSeatHold(hold, email, null, null);
    }

    private void assertSeatHold(SeatHold hold, String email, Integer startingSeat, Integer endingSeat) {
        assertNotNull(hold);
        assertEquals(hold.getEmail(), email);
        final List<Seat> seats = hold.getSeats();
        if (startingSeat != null) {
            assertEquals(startingSeat.intValue(), seats.get(0).getSeat());
        }
        if (endingSeat != null) {
            assertEquals(endingSeat.intValue(), seats.get(seats.size() - 1).getSeat());
        }
    }
}
