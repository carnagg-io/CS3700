package ThreadSieve;

public class ThreadSieve {

    private static int primeCount = 0;
    
    public static long printPrimes(int n) {
        long startTime = System.currentTimeMillis();
        
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
    
    public static void main(String[] args) {
        int n = 1000000;
        System.out.println("PRIMES UP TO " + n + ": ");
        long t = printPrimes(n);
        System.out.println("\n" + t + " ms. -> " + primeCount);
    }
    
}
