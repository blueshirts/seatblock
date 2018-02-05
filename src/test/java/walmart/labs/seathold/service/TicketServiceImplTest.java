package walmart.labs.seathold.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scoring.MiddleOutScorer;
import scoring.StandardScorer;
import walmart.labs.seathold.errors.NoSuchSeatHoldException;
import walmart.labs.seathold.models.Seat;
import walmart.labs.seathold.models.SeatHold;
import walmart.labs.seathold.models.Venue;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {
    private static final Logger LOG = Logger.getLogger(TicketServiceImplTest.class.getName());

    private static final String EMAIL1 = "email1@email.com";

    private TicketService service;

    private MiddleOutScorer scorer;

    @BeforeEach
    void beforeEach() {
        this.scorer = new MiddleOutScorer();
    }

    @AfterEach
    void afterEach() {
        if (this.service != null) {
            ((TicketServiceImpl)this.service).shutdown();
        }
    }

    @Test
    void numSeatsAvailable() {
        Venue venue = new Venue(8, 4);
        this.service = new TicketServiceImpl(venue, scorer);
        assertNotNull(service);
        assertEquals(venue.getMaxSeats(), service.numSeatsAvailable());
    }

    @Test
    void hold_noSeatsAvailable() {
        final int seats = 0;
        final int rows = 0;
        Venue venue = new Venue(seats, rows);
        this.service = new TicketServiceImpl(venue, scorer);
        SeatHold hold = service.findAndHoldSeats(5, EMAIL1);
        assertNull(hold);
    }

    @Test
    void holdSeats_notEnoughAvailable() {
        final int seats = 2;
        final int rows = 2;
        Venue venue = new Venue(seats, rows);
        this.service = new TicketServiceImpl(venue, scorer);
        SeatHold hold = service.findAndHoldSeats(5, EMAIL1);
        assertNull(hold);
    }

    @Test
    void holdSeats_8x4() {
        final int totalSeats = 32;
        Venue venue = new Venue(8, 4);
        this.service = new TicketServiceImpl(venue, scorer);

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
        Venue venue = new Venue(50, 100);
        this.service = new TicketServiceImpl(venue, scorer);

        // LOG.info(service.toString());

        final int blockSize = 2;
        for (int seat = 2; seat < venue.getMaxSeats(); seat += blockSize) {
            SeatHold hold = service.findAndHoldSeats(blockSize, EMAIL1);
            assertSeatHold(hold, EMAIL1);

            // LOG.info(service.toString());
            // LOG.info(hold.toString());

            // LOG.info("Current number of seats available: " + service.numSeatsAvailable());
            assertEquals(venue.getMaxSeats() - seat, service.numSeatsAvailable());
        }
    }

    @Test
    void holdSeats_50x100_standardScorer() {
        Venue venue = new Venue(50, 100);
        this.service = new TicketServiceImpl(venue, new StandardScorer());

        // LOG.info(service.toString());

        final int blockSize = 2;
        for (int seat = 2; seat < venue.getMaxSeats(); seat += blockSize) {
            SeatHold hold = service.findAndHoldSeats(blockSize, EMAIL1);
            assertSeatHold(hold, EMAIL1);

            // LOG.info(service.toString());
            // LOG.info(hold.toString());

            // LOG.info("Current number of seats available: " + service.numSeatsAvailable());
            assertEquals(venue.getMaxSeats() - seat, service.numSeatsAvailable());
        }
    }

    @Test
    void reserveSeats_2() {
        final int seats = 10;
        final int rows = 2;
        final int totalSeats = seats * rows;
        Venue venue = new Venue(seats, rows);
        this.service = new TicketServiceImpl(venue, scorer);

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
        Venue venue = new Venue(50, 100);
        this.service = new TicketServiceImpl(venue, scorer);

        final int blockSize = 2;
        for (int seat = 2; seat < venue.getMaxSeats(); seat += blockSize) {
            SeatHold hold = service.findAndHoldSeats(blockSize, EMAIL1);
            assertSeatHold(hold, EMAIL1);
            assertEquals(venue.getMaxSeats() - seat, service.numSeatsAvailable());
            String confirmation = service.reserveSeats(hold.getId(), EMAIL1);
            assertNotNull(confirmation);
            assertEquals(venue.getMaxSeats() - seat, service.numSeatsAvailable());
        }
    }

    @Test
    void reserveSeats_invalid() {
        try {
            Venue venue = new Venue(10, 2);
            this.service = new TicketServiceImpl(venue, scorer);

            service.reserveSeats(12345, EMAIL1);
            Assertions.fail("NoSuchSeatHoldException should have been thrown.");
        } catch (NoSuchSeatHoldException e) {
            assertNotNull(e);
        }
    }

    @Test
    void holdTimeout() {
        Venue venue = new Venue(10, 10);
        this.service = new TicketServiceImpl(venue, this.scorer, 1000);
        SeatHold hold = this.service.findAndHoldSeats(5, EMAIL1);
        assertSeatHold(hold, EMAIL1);
        assertEquals(95, this.service.numSeatsAvailable());
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
        assertEquals(venue.getMaxSeats(), service.numSeatsAvailable());
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
