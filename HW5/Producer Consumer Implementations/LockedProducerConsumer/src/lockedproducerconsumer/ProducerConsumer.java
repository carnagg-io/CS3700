package lockedproducerconsumer;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ProducerConsumer {
    
    private final Queue<Integer> buffer;
    private final Lock bufferLock;
    private final Condition bufferNotFull;
    private final Condition bufferNotEmpty;
    
    private int bufferCapacity;
    private int producerGoal;
    private int consumerGoal;
    private int sleepMillis;
    
    private class Producer implements Runnable {
        
        @Override
        public void run() {
            int produced = 0;
            while(produced < producerGoal) {
                try {
                    bufferLock.lock();
                    while(buffer.size() >= bufferCapacity)
                        bufferNotEmpty.await();
                    if(buffer.offer(new Integer((int)(Math.random() * 10 + 1)))) { 
                        produced++;
                        bufferNotFull.signalAll();
                    }
                } catch(InterruptedException e) {
                    System.out.println("PRODUCER-"
                            + Thread.currentThread().getId()
                            + ": Await interrupted.");
                } finally {
                    bufferLock.unlock();
                }
            }
        }
        
    }
    
    private class Consumer implements Runnable {
        
        @Override
        public void run() {
            boolean successful = false;
            while(consumerGoal > 0) {
                try {
                    bufferLock.lock();
                    while(buffer.size() <= 0 && consumerGoal > 0)
                        bufferNotFull.awaitNanos(1000);
                    successful = buffer.poll() != null;
                    if(successful) {
                        consumerGoal--;
                        bufferNotEmpty.signalAll();
                    }
                } catch(InterruptedException e) {
                    System.out.println("CONSUMER-"
                            + Thread.currentThread().getId()
                            + ": Await interrupted.");
                } finally {
                    bufferLock.unlock();
                    if(successful) {
                        try {
                            Thread.sleep(sleepMillis);
                        } catch(InterruptedException e) {
                            System.out.println("CONSUMER-"
                            + Thread.currentThread().getId()
                            + ": Sleep interrupted.");
                        }
                    }
                }
            }
        }
        
    }
    
    ProducerConsumer() {
        buffer = new LinkedList<>();
        bufferLock = new ReentrantLock();
        bufferNotFull = bufferLock.newCondition();
        bufferNotEmpty = bufferLock.newCondition();
    }
    
    public long produceAndConsume(int bufferCapacity, int producerGoal, int numProducers, int numConsumers, int sleepMillis) {
        long startTime = System.currentTimeMillis();
        
        this.bufferCapacity = bufferCapacity;
        this.producerGoal = producerGoal;
        this.consumerGoal = numProducers * producerGoal;
        this.sleepMillis = sleepMillis;
        
        Thread[] producers = new Thread[numProducers];
        Thread[] consumers = new Thread[numConsumers];
        
        for(int i = 0; i < numProducers; i++)
            producers[i] = new Thread(new Producer());
        
        for(int i = 0; i < numConsumers; i++)
            consumers[i] = new Thread(new Consumer());
        
        for(int i = 0; i < numProducers; i++)
            producers[i].start();
        
        for(int i = 0; i < numConsumers; i++)
            consumers[i].start();
        
        try {
            for(int i = 0; i < numProducers; i++)
                producers[i].join();
        } catch(InterruptedException e) {
            System.out.println("INTERRUPT: Failed to join all producers.");
        }
        
        try {
            for(int i = 0; i < numConsumers; i++)
                consumers[i].join();
        } catch(InterruptedException e) {
            System.out.println("INTERRUPT: Failed to join all consumers.");
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
}
