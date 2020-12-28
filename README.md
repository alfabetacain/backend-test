# How to run

Run the following commands:

```
sbt "project primeNumberService" run
sbt "project proxyService" run
```

Note that the commands are blocking.
The `primeNumberService` will run on port 8082. 
`proxyService` will run on port 8081.

Then run

```
curl http://localhost:8081/prime/15
```

You can replace 15 with whatever other number you want. 

# Project structure

The prime number service can be found in the folder `prime-number-service/`.

The proxy service can be found in the folder `proxy-service/`.

The contract can be found in the folder `contract/`.

Everything is built via sbt as specified in the `build.sbt`.

# Discussion

## Differences with spec

The implementation has the following differences with the spec:

- prime number service does not stream its results
	- While it is capable of doing so (since the prime calculation is a stream anyway), Thrift (as far as I can tell) do not support streaming responses, hence the prime number service does not either
		- https://dzone.com/articles/moving-from-apache-thrift-to-grpc-a-perspective-fr
		- Given the protocol itself (https://thrift.apache.org/docs/concepts.html) it does seem possible to implement streaming in Thrift
- proxy service does not stream its results
	- Since the prime number service is not streaming, neither is the proxy service (though it is capable of it, had the prime number service supported it)
- The results from the proxy service is returned as a newline separated list instead of a comma separated list

## Implementation

### Prime number calculation

The prime numbers are calculated using the [Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes). 

Using a stream of infinite integers, the non-prime numbers are filtered out by going through each integer and filtering out any following integer who is divisble by that number, thus resulting in a stream of primes.

### primeNumberService

The prime number service exposes the prime number calculation per the contract contract/src/main/thrift/primeNumberService.thrift and uses Finagle Thrift to do so.

I do not have experience with either Thrift or grpc, so I chose the first one mentioned. In hindsight, grpc might have been better since it supports streaming out of the box.

### proxyService

The proxy service is implemented in Finch. I don't have any experience in Finch, but since it is related to Finagle, which I used for the rpc call, I decided to use Finch. 
