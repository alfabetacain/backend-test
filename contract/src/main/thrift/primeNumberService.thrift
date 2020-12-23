namespace java dk.alfabetacain.backendtest.contract
#@namespace scala dk.alfabetacain.backendtest.contract

exception InvalidNumber {
    1: required string message;
}

service PrimeNumberService {
    list<i32> primes(1: i32 ceiling) throws (1: InvalidNumber ex)
}