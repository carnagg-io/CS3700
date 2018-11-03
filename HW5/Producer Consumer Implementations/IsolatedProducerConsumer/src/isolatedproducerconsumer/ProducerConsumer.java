package isolatedproducerconsumer;

import java.util.Queue;
import java.util.LinkedList;

public class ProducerConsumer {
    
    private final Queue<Integer> buffer;
    private final Object bufferLock;
    
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
                    synchronized(bufferLock) {
                        if(buffer.size() >= bufferCapacity)
                            bufferLock.wait(100);
                        if(buffer.size() < bufferCapacity)
                            if(buffer.offer(new Integer((int)(Math.random() * 10 + 1))))
                                produced++;
                        bufferLock.notifyAll();
                    }
                } catch(InterruptedException e) {
                    System.out.println("PRODUCER-"
                            + Thread.currentThread().getId()
                            + ": Wait interrupted.");
                }
            }
        }
        
    }
    
    private class Consumer implements Runnable {
        
        @Override
        public void run() {
            while(consumerGoal > 0) {
                try {
                    boolean successful = false;
                    synchronized(bufferLock) {
                        if(buffer.size() <= 0)
                            bufferLock.wait(100);
                        if(buffer.size() > 0) {
                            successful = buffer.poll() != null;
                            if(successful)
                                consumerGoal--;
                        }
                        bufferLock.notifyAll();
                    }
                    if(successful)
                        Thread.sleep(sleepMillis);
                } catch(InterruptedException e) {
                    System.out.println("CONSUMER-"
                            + Thread.currentThread().getId()
                            + ": Wait interrupted.");
                }
            }
        }
        
    }
    
    ProducerConsumer() {
        buffer = new LinkedList<>();
        bufferLock = new Object();
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
