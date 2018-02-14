# Seat Hold

> A Java implementation of a simple ticket service that facilitates the discovery, temporary hold, and final reservation of seats within a high-demand performance venue.

[![Build Status][travis-badge]][travis-url]

```
----------[[  STAGE  ]]----------
---------------------------------
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
sssssssssssssssssssssssssssssssss
```
        
## Provide the Following

- Find the number of seats available within the venue
- Find and hold the best available seats on behalf of a customer
- Reserve and commit a specific group of held seats for a customer

Assumptions:
- Available seats are seats that are neither held nor reserved.
- Each ticket hold should expire within a set number of seconds.

## Requirements

- The ticket service implementation should be written in Java
- The solution and tests should build and execute entirely via the command line using either Maven or Gradle as the build tool
- A README file should be included in your submission that documents your assumptions and includes instructions for building the solution and executing the tests
- Implementation mechanisms such as disk-based storage, a REST API, and a front-end GUI are not required

## Design

The following is a brief overview of the design and flow of the implementation.

- Upon creation of a TicketService iterate through all of the rows of the venue creating a SeatBlock for each.
- Using the supplied Scorer class assign each seat of the SeatBlock instances a score.  The higher the score the better
the seat is considered.
- Add the SeatBlock instances to a PriorityQueue ordered by the average score of the seats contained within the block.
- The current seats available that are not held is the sum of all the seats in the SeatBlock instances.
- The seat block priority queue contains blocks of seats in best available order.
- Taking the SeatBlock off the top of the priority queue will give you the best available block of seats that are
currently available.
- To hold the best available seats the top item in the priority queue is removed.  This SeatBlock must be split if the
number of seats required is less than the size of the SeatBlock.
-- If the best available seat block is the same size being requested to hold then the entire block is added to the
dictionary of SeatBlock holds.
-- If the best available seats are in the middle of the SeatBlock it is split into three partitions.  The left and 
right partitions are returned to the queue of available SeatBlocks while the middle partition is added to dictionary
of held seat blocks.
-- If the best available seats are on the left or the right then the SeatBlock is split into two partitions with one
block being held and the other returned to the queue of available seats.
- Held seat blocks are tracked in a dictionary keyed by the the seat hold id and the value being the held seat block.
- Seats can be reserved by supplying a seat hold id and a customer email address.  If the hold exists then the
corresponding SeatBlock is removed from the dictionary of held seats.
- A background thread is currently used to cleanup SeatHolds that have expired.  The thread currently continually 
checks if any seat holds have expired.  If the hold has expired it is added back into the priority queue of 
available seat blocks.  This design can be improved by maintaining a queue of the SeatHolds in the order that they were
held.  The hold at the top of the queue will represent the next hold that is set to expire.
- Due to concurrency concerns many of the functions implemented in the TicketServiceImpl must be synchronized.

## Configuration

### Scorers

There are two seat scoring implementations provided.  They can be substituted when creating a ticket service.

- [MiddleOutScorer](https://github.com/blueshirts/seatblock/blob/master/src/main/java/walmart/labs/seathold/scoring/MiddleOutScorer.java) - 
The middle out scoring implementation favors seating from the inner seats to the outer and from front to back.
- [StandardScorer](https://github.com/blueshirts/seatblock/blob/master/src/main/java/walmart/labs/seathold/scoring/StandardScorer.java) - 
The standard scoring implementation favors seating from left to right and front to back.

Each scoring implementation implements the Scorer interface.

- [Scorer](https://github.com/blueshirts/seatblock/blob/master/src/main/java/walmart/labs/seathold/scoring/Scorer.java)

## Tests

### Running the Tests

The tests can be running using the following command.

```bash
$ ./gradlew --rerun-tasks test

```

## Assumptions

The following are current assumptions:

- When holding seats it's assumed that you would like your seats to be contiguous and in the same row.  If you ask for
more seats than can be found together than the hold is not made and you must retry your attempt with a smaller number
of seats.
- By default seat holds expire after 2 minutes.  This value can be configured.

[travis-badge]: https://api.travis-ci.org/blueshirts/seatblock.svg
[travis-url]: https://travis-ci.org/blueshirts/seatblock


