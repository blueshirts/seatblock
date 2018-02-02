package walmart.labs.seathold.service;

import walmart.labs.seathold.errors.NoSuchSeatHoldException;
import walmart.labs.seathold.models.SeatHold;
import walmart.labs.seathold.common.SeatHoldUtils;
import scoring.Scorer;
import walmart.labs.seathold.models.Seat;
import walmart.labs.seathold.models.Venue;
import walmart.labs.seathold.models.SeatBlock;

import java.util.*;
import java.util.logging.Logger;

public class TicketServiceImpl implements TicketService {
    /**
     * Logging instance.
     */
    private static final Logger LOG = Logger.getLogger(TicketServiceImpl.class.getName());

    /**
     * The current venue.
     */
    private Venue venue;

    /**
     * The current seat scorer instance.
     */
    private Scorer scorer;

    /**
     * A collection of available seat blocks.
     */
    private PriorityQueue<SeatBlock> seatBlocks = new PriorityQueue<>();

    /**
     * The dictionary of seat hold id's to the corresponding seat hold instance.
     */
    private Map<Integer, SeatBlock> holdBlocks = new HashMap<>();


    /**
     * Construct a ticket service implementation.
     *
     * @param venue  - the venue for this service.
     * @param scorer - the scorer implementation.
     */
    public TicketServiceImpl(Venue venue, Scorer scorer) {
        this.venue = venue;
        this.scorer = scorer;

        final int rowSize = venue.getSeatsPerRow();
        final int rows = venue.getRows();

        for (int row = 0; row < rows; row++) {
            List<Seat> seats = new ArrayList<>(rowSize);
            float sum = 0;

            for (int seat = 0; seat < rowSize; seat++) {
                float score = this.scorer.calculateScore(seat, row, this.venue);
                score = SeatHoldUtils.round(score);
                seats.add(new Seat(seat, row, score));
                sum += score;
            }

            this.seatBlocks.add(new SeatBlock(seats));
        }
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    @Override
    public int numSeatsAvailable() {
        int size = 0;
        for (SeatBlock sb : seatBlocks) {
            size += sb.size();
        }
        return size;
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
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        if (this.seatBlocks.size() == 0) {
            // There are no seats left.
            LOG.info("There are no seats left");
            return null;
        } else {
            // TODO: This logic needs to be synchronized to prevent concurrency errors.

            // Find a block with enough seats.
            SeatBlock bestAvailableBlock = null;
            for (SeatBlock block : this.seatBlocks) {
                if (block.size() >= numSeats) {
                    // This block has enough seats.
                    bestAvailableBlock = block;
                    break; // **EXIT**
                }
            }

            if (bestAvailableBlock != null) {
                // Found a block large enough.

                // Remove the block
                this.seatBlocks.remove(bestAvailableBlock);

                if (bestAvailableBlock.size() == numSeats) {
                    // The block is the exact size required.
                    bestAvailableBlock.setEmail(customerEmail);

                    this.holdBlocks.put(bestAvailableBlock.getId(), bestAvailableBlock);

                    return bestAvailableBlock;
                } else {
                    // The block must be split.
                    List<SeatBlock> splits = bestAvailableBlock.split(numSeats);

                    // Add the remaining seats back.
                    for (int i = 1; i < splits.size(); i++) {
                        this.seatBlocks.add(splits.get(i));
                    }

                    // Return the best portion of the split.
                    SeatBlock result = splits.get(0);
                    result.setEmail(customerEmail);

                    this.holdBlocks.put(result.getId(), result);

                    return result;
                }
            } else {
                // TODO: Need to deal with the case where the numSeats is greater than any possible seat block.
                // No seat block is large enough for this request.
                LOG.info("No seat block was large enough for size of: " + numSeats);
                return null;
            }
        }
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
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

        // TODO: This block must be synched.

        // Remove the seat block from the holds.
        SeatBlock hold = this.holdBlocks.remove(seatHoldId);

        if (!hold.getEmail().equals(customerEmail)) {
            // Error, this hold is not for the supplied email.
            throw new SecurityException(String.format("Seat hold with id: %d is not related to customer email %s",
                    seatHoldId, customerEmail));
        }

        String result = String.valueOf(hold.getId());

        // Audit the reservation.
        LOG.info(String.format("RESERVED: %d seats reserved for customer: %s with confirmation code: %s",
                hold.size(), customerEmail, result));

        // Return a confirmation code.
        return result;
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
