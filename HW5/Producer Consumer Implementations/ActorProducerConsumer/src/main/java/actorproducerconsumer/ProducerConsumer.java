package actorproducerconsumer;

import java.util.Queue;
import java.util.LinkedList;
import akka.actor.UntypedAbstractActor;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumer {
    
    private static ActorSystem system;
    
    private static ActorRef bufferRef;
    private static ActorRef[] producerRef;
    private static ActorRef[] consumerRef;
    
    private static int producerGoal;
    private static AtomicInteger consumerGoal;
    private static int sleepMillis;
    
    public static class Buffer extends UntypedAbstractActor {
        
        public static Props props(int capacity) {
            return Props.create(Buffer.class, () -> new Buffer(capacity));
        }
        
        private final int capacity;
        private final Queue<Integer> buffer;

        
        public Buffer(int capacity) {
            this.capacity = capacity;
            buffer = new LinkedList<>();
        }
        
        @Override
        public void onReceive(Object msg) throws Exception {
            if(msg instanceof Integer) {
                if(buffer.size() < capacity) {
                    buffer.add((Integer)msg);
                    getSender().tell("", getSelf());
                } else {
                    getSender().tell((Integer)msg, getSelf());
                }
            } else {
                if(buffer.size() > 0)
                    getSender().tell(buffer.poll(), getSelf());
                else
                    getSender().tell("", getSelf());
            }
        }
    }
    
    public static class Producer extends UntypedAbstractActor {
        
        public static Props props() {
            return Props.create(Producer.class, () -> new Producer());
        }
        
        private int produced;
        
        Producer() {
            produced = -1;
        }
        
        @Override
        public void onReceive(Object msg) throws Exception {
            if(msg instanceof Integer) {
                bufferRef.tell((Integer)msg, getSelf());
            } else {
                produced++;
                if(produced < producerGoal)
                    bufferRef.tell(new Integer((int)(Math.random() * 10 + 1)), getSelf());
            }
        }
        
    }
    
    public static class Consumer extends UntypedAbstractActor {
        
        public static Props props() {
            return Props.create(Consumer.class, () -> new Consumer());
        }
        
        @Override
        public void onReceive(Object msg) throws Exception {
            if(msg instanceof Integer) {
                consumerGoal.decrementAndGet();
                if(consumerGoal.get() > 0) {
                    //Thread.sleep(sleepMillis);
                    bufferRef.tell("", getSelf());
                }
            } else {
                if(consumerGoal.get() > 0)
                    bufferRef.tell("", getSelf());
            }
        }
        
    }
    
    public long produceAndConsume(int bufferCapacity, int producerGoal, int numProducers, int numConsumers, int sleepMillis) {
        long startTime = System.currentTimeMillis();
        
        this.producerGoal = producerGoal;
        this.consumerGoal = new AtomicInteger(numProducers * producerGoal);
        this.sleepMillis = sleepMillis;
        
        system = ActorSystem.create("producer-consumer-system");
        bufferRef = system.actorOf(Buffer.props(bufferCapacity), "buffer-actor");
        producerRef = new ActorRef[numProducers];
        for(int i = 0; i < numProducers; i++)
            producerRef[i] = system.actorOf(Producer.props(), "producer-actor-" + i);
        consumerRef = new ActorRef[numConsumers];
        for(int i = 0; i < numConsumers; i++)
            consumerRef[i] = system.actorOf(Consumer.props(), "consumer-actor-" + i);
        
        for(int i = 0; i < numProducers; i++)
            producerRef[i].tell("", ActorRef.noSender());
        for(int i = 0; i < numConsumers; i++)
            consumerRef[i].tell("", ActorRef.noSender());
        
        while(consumerGoal.get() > 0);
           
        system.terminate();
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
}
