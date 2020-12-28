# How to run

Run the following commands:

```
sbt "project primeNumberService" run
sbt "project proxyService" run
```

Note that the commands are blocking. The `primeNumberService` will run on port 8082.
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

The contract can be found in the folder `contract/` ([thrift file](contract/src/main/thrift/primeNumberService.thrift)).

Everything is built via sbt as specified in the [build.sbt](build.sbt).

# Discussion

## Differences with spec

The implementation has the following differences with the spec:

- prime number service does not stream its results
    - While it is capable of doing so (since the prime calculation is a stream anyway), Thrift (as far as I can tell) do
      not support streaming responses, hence the prime number service does not either
        - https://dzone.com/articles/moving-from-apache-thrift-to-grpc-a-perspective-fr
        - Given the protocol itself (https://thrift.apache.org/docs/concepts.html) it does seem possible to implement
          streaming in Thrift
- proxy service does not stream its results
    - Since the prime number service is not streaming, neither is the proxy service (though it is capable of it, had the
      prime number service supported it)
- The results from the proxy service is returned as a newline separated list instead of a comma separated list

## Implementation

### Prime number calculation

The prime numbers are calculated using the [Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes).

Using a stream of infinite integers, the non-prime numbers are filtered out by going through each integer and filtering
out any following integer who is divisble by that number, thus resulting in a stream of primes.

### primeNumberService

The prime number service exposes the prime number calculation per the contract
contract/src/main/thrift/primeNumberService.thrift and uses Finagle Thrift to do so.

I do not have experience with either Thrift or grpc, so I chose the first one mentioned. In hindsight, grpc might have
been better since it supports streaming out of the box.

The only error case the prime number service handles explicitly is if the number used is lower than 0. In that case, it
returns an error message which is propagated via thrift as the exception `InvalidNumber`.

Since the thrift contract explicitly requires a number as input, non-numbers are not considered.

### proxyService

The proxy service is implemented in Finch. I don't have any experience in Finch, but since it is related to Finagle,
which I used for the rpc call, I decided to use Finch.

The proxy service parses the input as a string instead of a number. When I used the builtin conversion in Finch to get a
number directly (using `path[Int]` instead of `path[String]` in the proxy service) Finch returned 404 instead of 400. To
get better error messages, I handle that part manually and return 400 in case the input is not a number.

The proxy service also handles the potential error from the prime number service (if the number is below zero) and turns
that into a 400 as well.

In principle, the proxy service could have handled this check as well, which would have saved a call to the prime number
service. I would still keep the check in the prime number service though, since that service should not rely on callers
to ensure that its input is correct. 
