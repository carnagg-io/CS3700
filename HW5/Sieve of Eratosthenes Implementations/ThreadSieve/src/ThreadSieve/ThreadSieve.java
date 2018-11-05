/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * INSTRUCTOR  : Nima Davarpanah
 * COURSE      : CS 3700
 * ASSIGNMENT  : 5-2
 * DUE         : 11-5-18
 * DESCRIPTION : Main class for an implementation of the Sieve of Eratosthenes
 *               using a single thread.
 ******************************************************************************/

package ThreadSieve;

import java.util.List;
import java.util.ArrayList;

public class ThreadSieve {

    private static int primeCount = 0;
    
    public static long printPrimes_1(int n) {
        long startTime = System.currentTimeMillis();
        
        primeCount = 0;
        
        boolean[] isPrime = new boolean[n + 1];
        for(int i = 2; i <= n; i++)
            isPrime[i] = true;
        
        for(int f = 2; f * f <= n; f++)
            if(isPrime[f])
                for(int i = f; f * i <= n; i++)
                    isPrime[f * i] = false;
        
        for(int i = 1; i <= n; i++) {
            if(isPrime[i]) {
                primeCount++;
                System.out.println(i);
            }
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    public static long printPrimes_2(int n) {
        long startTime = System.currentTimeMillis();
        
        primeCount = 1;
        
        List<Integer> primes = new ArrayList<>();
        for(int i = 0; i < n; i++)
            primes.add(i + 1);
        
        int localPrime;
        
        System.out.println("1");
        for(int i = 2; i < primes.size(); i++) {
            primeCount++;
            localPrime = primes.get(i - 1);
            System.out.println(localPrime);
            for(int j = i; j < primes.size(); j++) {
                if(primes.get(j) % localPrime == 0) {
                    primes.remove(j);
                    j--;
                }
            }
        }
        
        
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    public static void main(String[] args) {
        int n = 1000000;
        System.out.println("PRIMES UP TO " + n + ": ");
        long t = printPrimes_1(n);
        System.out.println("\n" + t + " ms. -> " + primeCount);
    }
    
}
