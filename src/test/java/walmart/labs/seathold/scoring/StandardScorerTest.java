package walmart.labs.seathold.scoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walmart.labs.seathold.models.Venue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardScorerTest {
    private Scorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new StandardScorer();
    }

    @AfterEach
    void tearDown() {
        scorer = null;
    }
    @Test
    void calculateScore_8x4() {
        Venue v = new Venue(10, 10);

        // assert row 1.
        assertEquals(1.0f, scorer.calculateScore(0, 0, v));
        assertEquals(.95f, scorer.calculateScore(1, 0, v));
        assertEquals(.92f, scorer.calculateScore(2, 0, v));
        assertEquals(.88f, scorer.calculateScore(3, 0, v));
        assertEquals(.86f, scorer.calculateScore(4, 0, v));
        assertEquals(.83f, scorer.calculateScore(5, 0, v));
        assertEquals(.81f, scorer.calculateScore(6, 0, v));
        assertEquals(.79f, scorer.calculateScore(7, 0, v));
        assertEquals(.78f, scorer.calculateScore(8, 0, v));
        assertEquals(.76f, scorer.calculateScore(9, 0, v));
    }
}
