package walmart.labs.seathold.scoring;

import walmart.labs.seathold.common.SeatHoldUtils;
import walmart.labs.seathold.models.Venue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Venue seat scoring class that prioritizes seating in the middle of the venue before the outer edges.  It is possible
 * when using this scoring model that customers will be seated in the middle of deeper row prior to the end of a closer
 * row.
 */
public class MiddleOutScorer implements Scorer {
    private static final Logger LOG = Logger.getLogger(MiddleOutScorer.class.getName());

    private float calculateRowScore(int index, int size) {
        float result = ((float)size - (float)index) / (float)size;

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.fine(String.format("%d - %d / %d", size, index, size));
            LOG.fine("row score: " + result);
        }

        return result;
    }

    private float calculateSeatScore(int index, int size) {
        float score;
        if (size % 2 == 0) {
            // Even.
            int mid = size / 2;

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("even");
                LOG.finest("index: " + index);
                LOG.finest("max: " + size);
                LOG.finest("mid: " + mid);
            }

            if (index + 1 <= mid) {
                score = (float)(index + 1) / (float)mid;
            } else {
                score = (float)(size - index) / (float)mid;
            }
        } else {
            // Odd
            int mid = index / 2 + 1;
            if (index == mid) {
                score = 1.0F;
            } else if (index < mid) {
                score = mid - (mid - index + 1) / mid;
            } else { // x > midX
                score = mid / (mid - index + 1 - mid);
            }
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("seat score: " + score);
        }

        return score;
    }

    @Override
    public float calculateScore(int seatIndex, int rowIndex, Venue venue) {
        float seatScore = calculateSeatScore(seatIndex, venue.getSeatsPerRow());
        float rowScore = calculateRowScore(rowIndex, venue.getRows());

        float result = ((seatScore + rowScore) / 2.0f);
        result = SeatHoldUtils.round(result);

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("standard score: " + result);
        }

        return result;
    }

}
