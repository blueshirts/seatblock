package walmart.labs.seathold.models;

import walmart.labs.seathold.common.SeatHoldUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeatBlock implements Comparable, Iterable, SeatHold {
    /**
     * An internal sequence.
     */
    private static int ID = 0;
    /**
     * Logging instance.
     */
    private static final Logger LOG = Logger.getLogger(SeatBlock.class.getName());

    /**
     * A unique id for this seat hold.
     */
    private int id = ++ID;

    /**
     * The time this seat block was held.
     */
    private long holdTime;

    /**
     * The customers email.
     */
    private String email;

    /**
     * The score for this seat block.  Higher scores imply better seats.
     */
    private float score;

    /**
     * The list of seats associated with this block.
     */
    private List<Seat> seats = new ArrayList<>();


    /**
     * Construct a new SeatBlock.
     *
     * @param email - the customers email for this hold block.
     * @param seats - the seats for this hold block.
     */
    public SeatBlock(String email, List<Seat> seats) {
        this.email = email;

        float scoreSum = 0.0f;
        for (Seat s : seats) {
            scoreSum += s.getScore();
            this.seats.add(s);
        }
        this.score = SeatHoldUtils.round(scoreSum / this.seats.size());
    }

    /**
     * Construct a new seat block instance.
     *
     * @param seats - the initial seats in the hold block.
     */
    public SeatBlock(List<Seat> seats) {
        this(null, seats);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof SeatBlock) {
            return Float.compare(this.score, ((SeatBlock) o).score) * -1;
        } else {
            throw new IllegalArgumentException(o.getClass().getName() + " cannot be compared to " +
                    SeatBlock.class.getName());
        }
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator iterator() {
        return this.seats.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified).  Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer action) {
        this.seats.forEach(action);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public Spliterator spliterator() {
        return this.seats.spliterator();
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public void hold(String email) {
        this.email = email;
        this.holdTime = System.currentTimeMillis();
    }

    public long getHoldTime() {
        return this.holdTime;
    }

    public List<Seat> getSeats() {
        return Arrays.asList(this.seats.toArray(new Seat[this.seats.size()]));
    }

    public int size() {
        return this.seats.size();
    }

    /**
     * Split the into a list of blocks.
     * - If the list is equal to size then return the block.
     * - If the block is greater than size then return a list of blocks where the first contains the best available
     * seats of length "size".  The remaining blocks will contain the unused setas.
     *
     * @param size - the size of the block neede.d
     * @return a list of seat blocks where the first is the best available "size" seats.
     */
    public List<SeatBlock> split(int size) {
        if (size > this.seats.size()) {
            throw new ArrayIndexOutOfBoundsException("Split size is greater than the number of seats: " + size);
        }
        List<SeatBlock> results = new ArrayList<>();

        int bestStartingIndex = bestStartingIndex(size);

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Best starting index: " + bestStartingIndex);
        }

        if (bestStartingIndex == 0) {
            // Split into two groups returning the left most.

            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Split seat block into two, keeping the left most.");
            }

            List<Seat> left = this.seats.subList(0, size);
            List<Seat> right = this.seats.subList(size, this.seats.size());

            results.add(new SeatBlock(left));
            results.add(new SeatBlock(right));
        } else if (bestStartingIndex + size == this.seats.size()) {
            // Split into two returning the right most.

            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Split seat block into two, keeping the right most.");
            }

            List<Seat> left = this.seats.subList(0, bestStartingIndex);
            List<Seat> right = this.seats.subList(bestStartingIndex, this.seats.size());

            results.add(new SeatBlock(right));
            results.add(new SeatBlock(left));
        } else if (bestStartingIndex > 0) {
            // Potentially split into three groups, returning the middle.

            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Split seat block into three, keeping the middle.");
            }

            List<Seat> left = this.seats.subList(0, bestStartingIndex);
            List<Seat> middle = this.seats.subList(bestStartingIndex, bestStartingIndex + size);
            List<Seat> right;
            if (bestStartingIndex + size < this.seats.size()) {
                right = this.seats.subList(bestStartingIndex + size, this.seats.size());
            } else {
                right = new ArrayList<>();
            }

            results.add(new SeatBlock(middle));
            results.add(new SeatBlock(left));
            if (right.size() > 0) {
                // There is a block on the right.
                results.add(new SeatBlock(right));
            }
        }

        if (LOG.isLoggable(Level.FINER)) {
            StringBuilder s = new StringBuilder();
            s.append("Split seat blocks: ");
            s.append(results.size());
            s.append("\n");
            for (SeatBlock sb : results) {
                s.append("    ");
                s.append(sb.toString());
            }
            LOG.finer(s.toString());
        }

        return results;
    }

    /**
     * Retrieve the best starting seat index for a new block of "size".
     *
     * @param size - the size of the block needed.
     * @return the starting index of the seat index for a new block of "size".
     */
    private int bestStartingIndex(int size) {
        int bestStartingIndex = 0;
        float maxAverage = 0.0f;
        for (int i = 0; i <= this.seats.size() - size; ++i) {

            float sum = 0.0f;
            for (int j = i; j < i + size; j++) {
                sum += this.seats.get(j).getScore();
            }
            float average = sum / size;

            if (average > maxAverage) {
                maxAverage = average;
                bestStartingIndex = i;
            }
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Found best starting index: " + bestStartingIndex);
        }
        return bestStartingIndex;
    }

    /**
     * A debug string for this object.
     * Note: This implementation is not intended for production use.
     *
     * @return a debug string.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SeatBlock(" + this.score + ")");
        sb.append("\n");
        for (Seat seat : this.seats) {
            sb.append("\t");
            sb.append(seat);
            sb.append("\n");
        }
        return sb.toString();
    }
}
