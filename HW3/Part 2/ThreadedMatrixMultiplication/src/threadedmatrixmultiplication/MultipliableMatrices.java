/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 3-2
 * DUE         : 8 October 2018
 * DESCRIPTION : Facilitates unpooled and pooled threaded matrix multiplication.
 ******************************************************************************/

package threadedmatrixmultiplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MultipliableMatrices {
    
    private final int i;
    private final int j;
    private final int k;
    private final int[][] A;
    private final int[][] B;
    private final int[][] C;
    
    MultipliableMatrices(int i, int j, int k, int min, int max) {
        this.i = i;
        this.j = j;
        this.k = k;
        A = new int[i][j];
        B = new int[j][k];
        C = new int[i][k];
        
        fill(A, i, j, min, max);
        fill(B, j, k, min, max);
        fill(C, i, k, 0, 0);
    }
    
    private class UnpooledMultiply {
    
        private final int NUM_THREADS;
        private final int NUM_OPS;

        UnpooledMultiply(int numThreads) {
            NUM_THREADS = numThreads;
            if((i * k) > NUM_THREADS)
                NUM_OPS = (i * k) / NUM_THREADS;
            else
                NUM_OPS = 1;
        }

        public void perform(int id) {
            int first = id * NUM_OPS;
            int last = first + NUM_OPS - 1;

            if(id == NUM_THREADS - 1)
                last += (i * k) % NUM_THREADS;

            if(last >= i * k)
                return;

            for(int curr = first; curr <= last; curr++) {
                int p = curr / k;
                int r = curr % k;
                for(int q = 0; q < j; q++)
                    C[p][r] += A[p][q] * B[q][r];
            }
        }
        
    }
    
    private class UnpooledMultiplier implements Runnable {
    
        private final UnpooledMultiply multiply;
        private final int id;

        public UnpooledMultiplier(UnpooledMultiply mul, int id) {
            this.multiply = mul;
            this.id = id;
        }

        @Override
        public synchronized void run() {
            multiply.perform(id);
        }

    }
    
    public long unpooledMultiply(int numThreads) {
        fill(C, i, k, 0, 0);
        
        UnpooledMultiply multiply = new UnpooledMultiply(numThreads);
        Thread threads[] = new Thread[numThreads];
        
        long start = System.currentTimeMillis();
        
        try {
            for(int p = 0; p < numThreads; p++)
                threads[p] = new Thread(new UnpooledMultiplier(multiply, p));

            for(int p = 0; p < numThreads; p++)
                threads[p].start();

            for(int p = 0; p < numThreads; p++)
                threads[p].join();
        } catch(Exception e) {}
        
        long finish = System.currentTimeMillis();
        
        return finish - start;
    }
    
    private class PooledMultiplier implements Runnable {
        
        private final int p;
        private final int r;
        
        public PooledMultiplier(int p, int r) {
            this.p = p;
            this.r = r;
        }
        
        @Override
        public void run() {
            for(int q = 0; q < j; q++)
                C[p][r] += A[p][q] * B[q][r];
        }
        
    }
    
    public long pooledMultiply(int numThreads) {
        fill(C, i, k, 0, 0);
        
        long start = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for(int p = 0; p < i; p++) {
            for(int r = 0; r < k; r++) {
                PooledMultiplier thread = new PooledMultiplier(p, r);
                executor.submit(thread);
            }
        }
        executor.shutdown();
        
        try {
           executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch(InterruptedException e) { }
        
        long finish = System.currentTimeMillis();
        
        return finish - start;
    }
    
    private void fill(int M[][], int i, int j, int min, int max) {
        for(int p = 0; p < i; p++)
            for(int q = 0; q < j; q++)
                M[p][q] = ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
    private void print(int M[][], int i, int j) {
        for(int p = 0; p < i; p++) {
            for(int q = 0; q < j; q++)
                System.out.print(M[p][q] + " ");
            System.out.println();
        }
    }
    
    public void printAll() {
        print(A, i, j);
        System.out.println();
        print(B, j, k);
        System.out.println();
        print(C, i, k);
        System.out.println();
    }
    
    public void printResult() {
        print(C, i, k);
        System.out.println();
    }
    
}
