/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 4
 * DUE         : 15 October 2018
 * DESCRIPTION : Simulates the Dining Philosophers problem with the use of both
 *               structured and unstructured locks.
 ******************************************************************************/

package diningphilosophers;

// DiningPhilosophers : A class which performs both structured and unstructured
// dinners in the Dining Philosophers problem.
public class DiningPhilosophers {
    
    public static void main(String[] args) {
        StructuredDiningPhilosophers structured
                = new StructuredDiningPhilosophers(5, 5, 10);
        structured.dine(300);
        
        System.out.println("\n --- \n");
        
        UnstructuredDiningPhilosophers unstructured
                = new UnstructuredDiningPhilosophers(5, 5, 10);
        unstructured.dine(300);
    }
    
}
