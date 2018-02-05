package scoring;

import walmart.labs.seathold.common.SeatHoldUtils;
import walmart.labs.seathold.models.Venue;

/**
 * Venue seat scoring class that prioritizes seating from left to right and front to back.
 */
public class StandardScorer implements Scorer {
    @Override
    public float calculateScore(int seatIndex, int rowIndex, Venue venue) {
        float seatScore = (float)venue.getSeatsPerRow() / (float)(venue.getSeatsPerRow() + seatIndex);
        float rowScore = (float)venue.getRows() / (float)(venue.getRows() + rowIndex);
        return SeatHoldUtils.round((seatScore + rowScore) / 2);
    }
}
