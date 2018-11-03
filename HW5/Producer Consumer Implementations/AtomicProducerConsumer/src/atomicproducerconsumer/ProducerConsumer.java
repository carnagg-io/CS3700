package atomicproducerconsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumer {
    
    private BlockingQueue<Integer> buffer;
    
    private int bufferCapacity;
    private int producerGoal;
    private AtomicInteger consumerGoal;
    private int sleepMillis;
    
    private class Producer implements Runnable {
        
        @Override
        public void run() {
            int produced = 0;
            while(produced < producerGoal)
                if(buffer.offer(new Integer((int)(Math.random() * 10 + 1))))
                    produced++;
        }
        
    }
     
    private class Consumer implements Runnable {
        
        @Override
        public void run() {
            while(consumerGoal.get() > 0) {
                if(buffer.poll() != null) {
                    consumerGoal.decrementAndGet();
                    try {
                        Thread.sleep(sleepMillis);
                    } catch(Exception e) {
                        System.out.println("CONSUMER-"
                                + Thread.currentThread().getId()
                               + ": Sleep interrupted.");
                    }
                }
            }
        }
        
    }
    
    ProducerConsumer() {
        buffer = new LinkedBlockingQueue<>();
    }
    
    public long produceAndConsume(int bufferCapacity, int producerGoal, int numProducers, int numConsumers, int sleepMillis) {
        long startTime = System.currentTimeMillis();
        
        buffer = new LinkedBlockingQueue<>(bufferCapacity);
        this.bufferCapacity = bufferCapacity;
        this.producerGoal = producerGoal;
        this.consumerGoal = new AtomicInteger(numProducers * producerGoal);
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
