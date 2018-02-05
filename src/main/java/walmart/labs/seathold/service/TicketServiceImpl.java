package walmart.labs.seathold.service;

import scoring.Scorer;
import walmart.labs.seathold.common.SeatHoldUtils;
import walmart.labs.seathold.errors.NoSuchSeatHoldException;
import walmart.labs.seathold.models.Seat;
import walmart.labs.seathold.models.SeatBlock;
import walmart.labs.seathold.models.SeatHold;
import walmart.labs.seathold.models.Venue;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TicketServiceImpl implements TicketService {
    /**
     * Logging instance.
     */
    private static final Logger LOG = Logger.getLogger(TicketServiceImpl.class.getName());

    /**
     * The timeout in milliseconds before a ticket hold will be removed.
     */
    private static final long HOLD_TIMEOUT = 120 * 1000;

    /**
     * The current venue.
     */
    private Venue venue;

    /**
     * The current seat scorer instance.
     */
    private Scorer scorer;

    /**
     * The hold timeout.
     */
    private long holdTimeout;

    /**
     * A collection of available seat blocks in priority of best available seating.
     */
    private PriorityQueue<SeatBlock> seatBlocks = new PriorityQueue<>();

    /**
     * The dictionary of seat hold id's to the corresponding seat hold instance.
     */
    private Map<Integer, SeatBlock> holdBlocks = new HashMap<>();

    /**
     * A dictionary keyed by hold timeout values to the correpnding hold id.
     */
    private SortedMap<Long, Integer> timeoutToHolds = Collections.synchronizedSortedMap(new TreeMap<Long, Integer>());

    /**
     *
     */
    private Thread sweepThread;


    /**
     * Construct a ticket service implementation with the default hold timeout.
     *
     * @param venue  - the venue for this service.
     * @param scorer - the scorer implementation.
     */
    public TicketServiceImpl(Venue venue, Scorer scorer) {
        this(venue, scorer, HOLD_TIMEOUT);
    }

    /**
     * Construct a ticket service implementation.
     *
     * @param venue       - the venue for this service.
     * @param scorer      - the scorer implementation.
     * @param holdTimeout - the hold timeout value.
     */
    public TicketServiceImpl(Venue venue, Scorer scorer, long holdTimeout) {
        this.venue = venue;
        this.scorer = scorer;
        this.holdTimeout = holdTimeout;

        assert(this.holdTimeout > 0);

        final int rowSize = venue.getSeatsPerRow();
        final int rows = venue.getRows();

        for (int row = 0; row < rows; row++) {
            List<Seat> seats = new ArrayList<>(rowSize);

            for (int seat = 0; seat < rowSize; seat++) {
                float score = this.scorer.calculateScore(seat, row, this.venue);
                score = SeatHoldUtils.round(score);
                seats.add(new Seat(seat, row, score));
            }

            this.seatBlocks.add(new SeatBlock(seats));
        }

        this.sweepThread = new Thread(() -> {
            while (true) {
                Set<Integer> expiredHolds = new HashSet<>();
                for (Map.Entry<Long, Integer> entry : this.timeoutToHolds.entrySet()) {
                    long expireTime = entry.getKey() + this.holdTimeout;
                    if (System.currentTimeMillis() >= expireTime) {
                        // This entry has expired.
                        expiredHolds.add(entry.getValue());
                    } else {
                        // No more holds have expired at the current time.
                        break; // **EXIT**
                    }
                }

                // Remove the expired holds.
                if (expiredHolds.size() > 0) {
                    this.removeHolds(expiredHolds);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break; // **EXIT**
                }
            }
        });
        this.sweepThread.start();
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    @Override
    public synchronized int numSeatsAvailable() {
        int size = 0;
        for (SeatBlock sb : seatBlocks) {
            size += sb.size();
        }
        return size;
    }

    /**
     * Find the best available block from the currently available seats.
     *
     * @param numSeats        - the number of seats requested.
     * @param useAnyBlockSize - if true then the seats do not need to be contiguous or in the same row.
     * @return the best available SeatBlock containing enough seats to fulfill the order or null if
     * it cannot be fulfilled.
     */
    private SeatBlock findBestAvailableBlocks(int numSeats, boolean useAnyBlockSize) {
        int seatsRequired = numSeats;
        List<Seat> heldSeats = new ArrayList<>();
        List<SeatBlock> usedBlocks = new ArrayList<>();

        for (SeatBlock block : this.seatBlocks) {
            if (useAnyBlockSize || block.size() >= numSeats) {
                //
                usedBlocks.add(block);

                if (block.size() == seatsRequired) {
                    // The block is an exact match.

                    // Add the entire block to the result.
                    heldSeats.addAll(block.getSeats());
                    seatsRequired -= block.size();

                    // There should be no seats required.
                    assert (seatsRequired == 0);

                    break; // **EXIT**
                } else if (seatsRequired < block.size()) {
                    // This block has more seats than is required.

                    // Add the best available portion to the result.
                    List<SeatBlock> splits = block.split(seatsRequired);
                    SeatBlock bestAvailableBlock = splits.get(0);
                    heldSeats.addAll(bestAvailableBlock.getSeats());
                    seatsRequired -= bestAvailableBlock.size();

                    // There hold should be fulfilled.
                    assert (seatsRequired == 0);

                    // Add the remaining seats back.
                    for (int i = 1; i < splits.size(); i++) {
                        this.seatBlocks.add(splits.get(i));
                    }

                    break; // **EXIT**
                } else {
                    // This block is smaller than the required size

                    heldSeats.addAll(block.getSeats());
                    seatsRequired -= block.size();

                    // The hold should not be fulfilled.
                    assert (seatsRequired > 0);
                }
            } // else this block is not a match, keep searching.
        }

        if (usedBlocks.size() > 0) {
            for (SeatBlock toDelete : usedBlocks) {
                this.seatBlocks.remove(toDelete);
            }
        }

        if (heldSeats.size() == 0) {
            // There are no blocks that are large enough to fulfill this request and we are not using
            // individual seats.  This hold order cannot be fulfilled.
            assert (seatsRequired == numSeats);
            return null;
        } else {
            // Create a new seat hold containing all of the seats combined.
            return new SeatBlock(heldSeats);
        }
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    @Override
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        int numSeatsAvailable;
        if (this.seatBlocks.size() == 0) {
            // There are no seats left.
            LOG.fine("There are currently not seats available");
            return null;
        } else if (numSeats > (numSeatsAvailable = numSeatsAvailable())) {
            // There are not enough seats available to fulfill this request.
            String msg = String.format("The requested number of seats: %d is greater than the number of " +
                    "seats that are currently available: %d", numSeats, numSeatsAvailable);
            LOG.fine(msg);
            return null;
        } else {
            SeatBlock result = findBestAvailableBlocks(numSeats, false);

            // Note: At the current time I am assuming if there is not a contiguous seat block large enough to
            // fulfill the customers request then we do not create the hold.  The request must be retried using
            // a smaller block.  In the future we can retry and fulfill the order with seats that are not
            // contiguous.
            //if (!useAnyBlockSize && result == null) {
            //    result = findBestAvailableBlocks(numSeats, true);
            //}

            if (result != null) {
                // Associated the customer email with this hold.
                result.hold(customerEmail);
                // Add the hold to the dictionary by its id.
                this.holdBlocks.put(result.getId(), result);
                // Store the hold id by creation date.
                this.timeoutToHolds.put(result.getHoldTime(), result.getId());
            }

            return result;
        }
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     * @throws NoSuchSeatHoldException if a corresponding hold cannot be found.
     */
    @Override
    public synchronized String reserveSeats(int seatHoldId, String customerEmail) {
        if (seatHoldId <= 0) {
            // Error, invalid seat id.
            throw new IllegalArgumentException("Seat hold id is not valid: " + seatHoldId);
        } else if (customerEmail == null || customerEmail.equals("")) {
            // Error, the customer email is not valid.
            throw new IllegalArgumentException("Customer email is not valid: " + customerEmail);
        } else if (!this.holdBlocks.containsKey(seatHoldId)) {
            // Error, the hold does not exist.
            String msg = String.format("The seat hold for customer: %s having id: %d does not exist.",
                    customerEmail, seatHoldId);
            throw new NoSuchSeatHoldException(msg);
        }

        // Remove the seat block from the holds.
        SeatBlock hold = this.holdBlocks.remove(seatHoldId);

        if (!hold.getEmail().equals(customerEmail)) {
            // Error, this hold is not for the supplied email.
            throw new SecurityException(String.format("Seat hold with id: %d is not related to customer email %s",
                    seatHoldId, customerEmail));
        }

        String result = String.valueOf(hold.getId());

        // Audit the reservation.
        LOG.fine(String.format("RESERVED: %d seats reserved for customer: %s with confirmation code: %s",
                hold.size(), customerEmail, result));

        // Return a confirmation code.
        return result;
    }

    /**
     * Remove the seat holds by id if they exist.
     *
     * @param holdIds - a list of seat hold ids.
     */
    private synchronized void removeHolds(Set<Integer> holdIds) {
        for (int holdId : holdIds) {
            // Remove the hold if it exists.
            SeatBlock hold = this.holdBlocks.remove(holdId);
            if (hold != null) {
                this.seatBlocks.add(hold);
            }
        }
    }

    public void shutdown() {
        try {
            this.sweepThread.interrupt();
            this.sweepThread.join(1000);
        } catch (InterruptedException e) {
            LOG.warning("Exception while shutting down: " + e.toString());
        }
    }

    /**
     * Return the service as a string value.
     * <p>
     * NOTE: This function is currently only for debugging purposes. This function should not be used in a production
     * environment.
     *
     * @return the service instance as a string.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TicketServiceImpl (");
        sb.append("\n");

        int row = 0;

        SeatBlock[] a = this.seatBlocks.toArray(new SeatBlock[this.seatBlocks.size()]);
        Arrays.sort(a);

        for (SeatBlock block : a) {
            sb.append("\t");
            sb.append(row);
            sb.append(" - ");
            sb.append(block);
            sb.append("\n");

            row++;
        }

        sb.append(")");

        return sb.toString();
    }
}
