# Versions

There are two versions of this assignment: 
- Akka based (this one)
- Thrift based (check the branch `thrift`)

Note that the thrift based version does not comply with every aspect of the spec. 

# How to run

Run the following commands:

```
sbt "project primeNumberService" run
sbt "project proxyService" run
```

Note that the commands are blocking. The `primeNumberService` will run on port 8080.
`proxyService` will run on port 8081.

Then run

```
curl http://localhost:8081/prime/15
```

You can replace 15 with whatever other number you want.

# Project structure

The prime number service can be found in the
folder `prime-number-service/` ([main class](prime-number-service/src/main/scala/dk/alfabetacain/backendtest/primenumberservice/Main.scala))
.

The proxy service can be found in the
folder `proxy-service/` ([main class](proxy-service/src/main/scala/dk/alfabetacain/backendtest/proxyservice/Main.scala))
.

The contract can be found in the folder `contract/` ([thrift file](contract/src/main/protobuf/contract.proto)).

Everything is built via sbt as specified in the [build.sbt](build.sbt).

# Discussion

## Implementation

### Prime number calculation

The prime numbers are calculated using the [Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes).

Using a stream of infinite integers, the non-prime numbers are filtered out by going through each integer and filtering
out any following integer who is divisble by that number, thus resulting in a stream of primes.

### primeNumberService

The prime number service exposes the prime number calculation per the contract
contract/src/main/protobuf/contract.proto and uses Akka GRPC to do so.

The only error case the prime number service handles explicitly is if the number used is lower than 0. In that case, it
returns an empty stream.

Since the protobuf contract explicitly requires a number as input, non-numbers are not considered.

### proxyService

The proxy service is implemented in Akka Http. I have used Akka Actors before, and the Akka GRPC support seems excellent, 
which is why I chose it.

The proxy service parses the input as a string instead of a number, since that allows me to return 400 if a negative number is used.
This was primarily to show a bit of validation. I could have used the builtin Akka matcher for numbers (`IntNumber`), but that would not have matched negative numbers, 
and would have resulted in a 404 instead.

