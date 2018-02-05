package walmart.labs.seathold.scoring;

import walmart.labs.seathold.models.Venue;

public interface Scorer {
    float calculateScore(int seatIndex, int rowIndex, Venue venue);
}
