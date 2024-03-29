/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * INSTRUCTOR  : Nima Davarpanah
 * COURSE      : CS 3700
 * ASSIGNMENT  : 5-1
 * DUE         : 11-5-18
 * DESCRIPTION : Main class for an implementation of the Producer-Consumer
 *               scenario using atomics.
 ******************************************************************************/

package atomicproducerconsumer;

public class AtomicProducerConsumer {

    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();
        
        System.out.println("Producing and Consuming with Atomics");
        System.out.println("5 Producers, 2 Consumers: "
                + pc.produceAndConsume(10, 100, 5, 2, 1000) + " ms.");
        System.out.println("2 Producers, 5 Consumers: "
                + pc.produceAndConsume(10, 100, 2, 5, 1000) + " ms.");
    }
    
}
