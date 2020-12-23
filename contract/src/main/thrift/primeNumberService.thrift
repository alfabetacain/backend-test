namespace java dk.alfabetacain.backendtest.contract
#@namespace scala dk.alfabetacain.backendtest.contract

service PrimeNumberService {
    list<i32> primes(1: i32 ceiling)
}