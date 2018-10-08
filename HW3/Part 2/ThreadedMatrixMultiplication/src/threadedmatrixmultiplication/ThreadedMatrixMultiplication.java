/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 3-2
 * DUE         : 8 October 2018
 * DESCRIPTION : Performs matrix multiplication on matrices of varying size,
 *               compares the execution times of varying unpooled and pooled
 *               threads.
 ******************************************************************************/

package threadedmatrixmultiplication;

public class ThreadedMatrixMultiplication {

    public static void main(String[] args) {
        int index = 1;
        int i = 1;
        int j = 2;
        int k = 3;
        int min = 0;
        int max = 10;
        
        while(index < 14) {
            MultipliableMatrices matrices
                    = new MultipliableMatrices(i, j, k, min, max);
            int numThreads = 1;
            
            System.out.print("RUN #" + index + " -> ");
            System.out.print("(" + i + "x" + j +") * ");
            System.out.print("(" + j + "x" + k +") = ");
            System.out.print("(" + i + "x" + k +")\n");
            
            for(int t = 0; t < 4; t++) {
                System.out.print(numThreads + " THREADS: ");
                System.out.print(matrices.unpooledMultiply(numThreads) + " ms. vs. ");
                System.out.print(matrices.pooledMultiply(numThreads) + " ms.\n");
                numThreads *= 2;
            }
            System.out.println();
            
            index++;
            i *= 2;
            j *= 2;
            k *= 2;
        }
        
    }
    
}
