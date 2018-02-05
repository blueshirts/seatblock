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

## Configuration

### Scorers

There are two seat scoring implementations provided.  They can be substituted when creating a ticket service.

- [MiddleOutScorer](https://github.com/blueshirts/seatblock/blob/master/src/main/java/scoring/MiddleOutScorer.java) - 
The middle out scoring implementation favors seating from the inner seats to the outer and from front to back.
- [StandardScorer](https://github.com/blueshirts/seatblock/blob/master/src/main/java/scoring/StandardScorer.java) - 
The standard scoring implementation favors seating from left to right and front to back.

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


