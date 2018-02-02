# Seat Hold

> A Java implementation of a simple ticket service that facilitates the discovery, temporary hold, and final reservation of seats within a high-demand performance venue.

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

## Software Requirements

- Keep track of which seats are locked
-- Provide a function to lock a seat
-- Run a background thread that will unlock seats that have been locked for greater than N seconds
- Implement a best available algorithm
-- Keeps the group available though splits them if necessary
-- Closest to the stage
-- Closest to the center of the venue
-- Avoids leaving orphaned single seats
- Implement a commit function that will commit and purchase seats for a customer
-- Assumes that seats must be held before they can be purchased
- Implement a visualization of the current seating state for testing purposes.

## Additional Assumptions

- TBD

## ToDo

Implement the service
- Need to handle the case where the block requested is bigger than the current row.
- Need to remove debugging in the TicketServiceImpl class.
- Need to change the middle out scoring to favor closer rows rather than center.
-- Update the test case scores.
- Need to implement the standard scorer.
- Add logic for people who are looking for single seats to avoid fragging?

Implement test cases

Implement code coverage and inspection

Implement TravisCI integration
- Add github badging

Documentation
- Document any assumptions
- Document how to build the code.
- Document how to run the tests.
- Add a top level image.
- Implement the README


