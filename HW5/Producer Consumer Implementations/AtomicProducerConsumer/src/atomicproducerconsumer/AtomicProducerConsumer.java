package atomicproducerconsumer;

public class AtomicProducerConsumer {

    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();
        
        System.out.println("Producing and Consuming with Atomics");
        System.out.println("5 Producers, 2 Consumers: "
                + pc.produceAndConsume(10, 100, 5, 2, 100) + " ms.");
        System.out.println("2 Producers, 5 Consumers: "
                + pc.produceAndConsume(10, 100, 2, 5, 100) + " ms.");
    }
    
}
