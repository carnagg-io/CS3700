package isolatedproducerconsumer;

public class IsolatedProducerConsumer {

    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();
        
        System.out.println("Producing and Consuming with Isolated Sections");
        System.out.println("5 Producers, 2 Consumers: "
                + pc.produceAndConsume(10, 100, 5, 2, 100) + " ms.");
        System.out.println("2 Producers, 5 Consumers: "
                + pc.produceAndConsume(10, 100, 2, 5, 100) + " ms.");
    }
    
}
