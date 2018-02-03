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
-- If the block size is larger than the biggest row then using any size blocks to fill the order.
-- If you get to the end of the venue and you cannot find a block large enough then start again from the beginning
   using individual seats.  Will most likely need an override for this.
- Need to implement and test concurrency.
-- Create a test case that uses multiple threads to create orders at the same time.
-- Create a test case that uses multiple threats to create and reserve orders at the same time.
- Create a test that tries to hold and reserve random blocks of tickets.  The run should always eventually end
  if the logic is implemented correctly.  
- Implement and test a standard scoring class for contrast.
- Add logic for people who are looking for single seats to avoid fragging?
- Provide a visualization.

Implement test cases
- Implement some sort of performance test that uses a massive venue size.
- Adjust the memory size that is used when running the tests.

Implement code coverage and inspection

Implement TravisCI integration
- Add github badging

Documentation
- Document any assumptions
- Document how to build the code.
- Document how to run the tests.
- Add a top level image.
- Implement the README


