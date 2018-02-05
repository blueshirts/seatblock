package walmart.labs.seathold.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walmart.labs.seathold.scoring.MiddleOutScorer;
import walmart.labs.seathold.scoring.Scorer;
import walmart.labs.seathold.models.Seat;
import walmart.labs.seathold.models.SeatHold;
import walmart.labs.seathold.models.Venue;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TicketServiceImplStressTest {
    private static final Logger LOG = Logger.getLogger(TicketServiceImplStressTest.class.getName());
    private static final String EMAIL = "request@mail.com";
    private static final int maxBlockSize = 25;
    private static final int threads = 10;
    private static final int seats = 1000;
    private static final int rows = 1000;
    private static final Venue venue = new Venue(seats, rows);
    private static final Scorer scorer = new MiddleOutScorer();

    private CompletionService<SeatHold> holdCompletionService;
    private CompletionService<String> reservationCompletionService;
    private TicketService service;


    @BeforeEach
    void beforeEach() {
        this.service = new TicketServiceImpl(venue, scorer);
        this.holdCompletionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(threads));
        this.reservationCompletionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(threads));
    }

    @AfterEach
    void afterEach() {
        ((TicketServiceImpl)this.service).shutdown();
    }

    @Test
    void holdAndReserveSeats() {
        // There should always be seats to start out.
        assertTrue(service.numSeatsAvailable() > 0);

        // The futures containing the SeatHold result from findAndHoldSeats.
        final Set<Future<SeatHold>> holdFutures = ConcurrentHashMap.newKeySet();
        // The ticket holds that have been created.
        List<SeatHold> holds = Collections.synchronizedList(new ArrayList<SeatHold>());
        //
        Set<Integer> holdIds = ConcurrentHashMap.newKeySet();
        // A set of seats that are currently on hold.
        final Set<Seat> heldSeats = ConcurrentHashMap.newKeySet();
        // The start time.
        long start = System.currentTimeMillis();

        // Attempt to hold all of the seats in the venue.
        while (service.numSeatsAvailable() > 0) {
            // Concurrently hold tickets.
            Future<SeatHold> f = holdCompletionService.submit(() -> {
                final int numTickets = getNumTickets();
//                LOG.info(String.format("Creating hold for %d tickets (%d)", numTickets, Thread.currentThread().getId()));
                return service.findAndHoldSeats(numTickets, EMAIL);
            });
            holdFutures.add(f);
        }

        // Wait for the hold requests to complete.
        while (holdFutures.size() > 0) {
            try {
                Future<SeatHold> f = holdCompletionService.take();
                holdFutures.remove(f);
                SeatHold h = f.get();

                if (h != null) {
                    // Save the seat hold for later use.
                    assertFalse(holdIds.contains(h.getId()));
                    holdIds.add(h.getId());
                    holds.add(h);

                    // Add each of the seats held to a set for validation.
                    for (Seat s : h.getSeats()) {
                        // The seats should never be already reserved.
                        assertFalse(heldSeats.contains(s));

                        // Keep track of the held seats.
                        heldSeats.add(s);
                    }
                }
            } catch (Exception e) {
                Assertions.fail(e);
            }
        }

        final int totalHolds = holds.size();

        // The futures containing the reservation confirmation code.
        final Set<Future<String>> reserveFutures = ConcurrentHashMap.newKeySet();
        // The ticket reservations that have been created.
        final Set<String> reservations = ConcurrentHashMap.newKeySet();

        while (holds.size() > 0) {
            final SeatHold holdToReserve = holds.remove(0);
            Future<String> f = reservationCompletionService.submit(() -> {
                return service.reserveSeats(holdToReserve.getId(), holdToReserve.getEmail());
            });
            reserveFutures.add(f);
        }

        while (reserveFutures.size() > 0) {
            String confirmationCode = null;
            try {
                Future<String> rf = reservationCompletionService.take();
                reserveFutures.remove(rf);
                confirmationCode = rf.get();
                if (confirmationCode != null) {
                    assertFalse(reservations.contains(confirmationCode));
                    reservations.add(confirmationCode);
                }
            } catch (Exception e) {
                LOG.warning("\n ---------- Error Metrics ----------\n" +
                        String.format("\tTotal Holds: %d\n", totalHolds) +
                        String.format("\tTicket Holds: %d\n", holds.size()) +
                        String.format("\tSeats Held: %d\n", heldSeats.size()) +
                        String.format("\tReservations: %d", reservations.size()));
                if (confirmationCode != null) {
                    LOG.warning(String.format("Confirmation already processed: %b", reservations.contains(confirmationCode)));
                }

                Assertions.fail(e);
            }
        }

        long end = System.currentTimeMillis();

        // The held seats count should match the number of seats in the venue.
        assertEquals(venue.getMaxSeats(), heldSeats.size());

        LOG.info("\n\n---------- Results ----------\n" +
                String.format("\tTotal Holds: %d\n", totalHolds) +
                String.format("\tTicket Holds: %d\n", holds.size()) +
                String.format("\tSeats Held: %d\n", heldSeats.size()) +
                String.format("\tReservations: %d", reservations.size()) +
                String.format("\tTime: %d ms\n", (end - start)));
    }

    /**
     * Randomly return a ticket block size.
     *
     * @return a random number between 1 and the max block size.
     */
    private int getNumTickets() {
        return ThreadLocalRandom.current().nextInt(1, maxBlockSize + 1);
    }
}
