package walmart.labs.seathold.scoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walmart.labs.seathold.models.Venue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiddleOutScorerTest {
    private Scorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new MiddleOutScorer();
    }

    @AfterEach
    void tearDown() {
        scorer = null;
    }


    @Test
    void calculateScore_8x4() {
        Venue v = new Venue(8, 4);

        // assert row 1.
        assertEquals(.63f, scorer.calculateScore(0, 0, v));
        assertEquals(.75f, scorer.calculateScore(1, 0, v));
        assertEquals(.88f, scorer.calculateScore(2, 0, v));
        assertEquals(1.0f, scorer.calculateScore(3, 0, v));
        assertEquals(1.0f, scorer.calculateScore(4, 0, v));
        assertEquals(.88f, scorer.calculateScore(5, 0, v));
        assertEquals(.75f, scorer.calculateScore(6, 0, v));
        assertEquals(.63f, scorer.calculateScore(7, 0, v));

        // assert row 2.
        assertEquals(.50f, scorer.calculateScore(0, 1, v));
        assertEquals(.63f, scorer.calculateScore(1, 1, v));
        assertEquals(.75f, scorer.calculateScore(2, 1, v));
        assertEquals(.88f, scorer.calculateScore(3, 1, v));
        assertEquals(.88f, scorer.calculateScore(4, 1, v));
        assertEquals(.75f, scorer.calculateScore(5, 1, v));
        assertEquals(.63f, scorer.calculateScore(6, 1, v));
        assertEquals(.50f, scorer.calculateScore(7, 1, v));

        // assert row 3.
        assertEquals(.38f, scorer.calculateScore(0, 2, v));
        assertEquals(.50f, scorer.calculateScore(1, 2, v));
        assertEquals(.63f, scorer.calculateScore(2, 2, v));
        assertEquals(.75f, scorer.calculateScore(3, 2, v));
        assertEquals(.75f, scorer.calculateScore(4, 2, v));
        assertEquals(.63f, scorer.calculateScore(5, 2, v));
        assertEquals(.50f, scorer.calculateScore(6, 2, v));
        assertEquals(.38f, scorer.calculateScore(7, 2, v));

        // assert row 4.
        assertEquals(.25f, scorer.calculateScore(0, 3, v));
        assertEquals(.38f, scorer.calculateScore(1, 3, v));
        assertEquals(.50f, scorer.calculateScore(2, 3, v));
        assertEquals(.63f, scorer.calculateScore(3, 3, v));
        assertEquals(.63f, scorer.calculateScore(4, 3, v));
        assertEquals(.50f, scorer.calculateScore(5, 3, v));
        assertEquals(.38f, scorer.calculateScore(6, 3, v));
        assertEquals(.25f, scorer.calculateScore(7, 3, v));
    }

    @Test
    void calculateScore_50x4() {
        Venue v = new Venue(50, 4);

        // assert row 1.
        assertEquals(.52f, scorer.calculateScore(0, 0, v));
        assertEquals(.54f, scorer.calculateScore(1, 0, v));
        assertEquals(1.0f, scorer.calculateScore(24, 0, v));
        assertEquals(1.0f, scorer.calculateScore(25, 0, v));
        assertEquals(.54f, scorer.calculateScore(48, 0, v));
        assertEquals(.52f, scorer.calculateScore(49, 0, v));

//        assertEquals(.75f, scorer.calculateScore(1, 0, v));
//        assertEquals(.88f, scorer.calculateScore(2, 0, v));
//        assertEquals(1.0f, scorer.calculateScore(3, 0, v));
//        assertEquals(1.0f, scorer.calculateScore(4, 0, v));
//        assertEquals(.88f, scorer.calculateScore(5, 0, v));
//        assertEquals(.75f, scorer.calculateScore(6, 0, v));
//        assertEquals(.63f, scorer.calculateScore(7, 0, v));
    }

//    char[][] seats = new char[cols][rows];
//    PriorityQueue<SeatBlock> blocks = new PriorityQueue<>();
//
//    // Generate seat blocks.
//    PriorityQueue<SeatBlock> seatBlocks = new PriorityQueue<>();
//        for (int y = 0; y < rows; y++) {
//        List<Seat> rowSeats = new ArrayList<>();
//        float rowScoreSum = 0;
//
//        for (int x = 0; x < cols; x++) {
//            float seatScore = getSeatScore(x, y);
//            rowScoreSum += seatScore;
//            Seat s = new Seat(x, y, seatScore);
//            rowSeats.add(s);
//        }
//
//        SeatBlock sb = new SeatBlock(rowSeats, rowScoreSum / rowSeats.size());
//        seatBlocks.add(sb);
//    }
//
//        for (SeatBlock b : seatBlocks) {
//        System.out.println(b);
//    }
}
