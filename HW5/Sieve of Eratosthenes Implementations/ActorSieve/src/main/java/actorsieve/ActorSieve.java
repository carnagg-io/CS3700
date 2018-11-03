package actorsieve;

import akka.actor.UntypedAbstractActor;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;

public class ActorSieve {
    
    private static ActorSystem system = ActorSystem.create("sieve-of-eratosthenes-system");
    private static int primeCount = 0;
    private static final Object waitLock = new Object();
    
    private static class Sieve extends UntypedAbstractActor {
        
        public static Props props(int localPrime) {
            return Props.create(Sieve.class, () -> new Sieve(localPrime));
        }
        
        private final int localPrime;
        private ActorRef nextSieve;
        
        Sieve(int localPrime) {
            this.localPrime = localPrime;
            System.out.println(localPrime + " ");
            primeCount++;
        }
        
        @Override
        public void onReceive(Object msg) throws Exception {
            if(msg instanceof Integer) {
                int candidate = (Integer)msg;
                if(candidate % localPrime > 0) {
                    if(nextSieve == null)
                        nextSieve = getContext().actorOf(Sieve.props(candidate), "sieve-actor-" + candidate);
                    else
                        nextSieve.tell(msg, getSelf());
                }
            } else if(nextSieve == null) {
                synchronized(waitLock) {
                    waitLock.notify();
                }
            } else {
                nextSieve.tell("", ActorRef.noSender());
            }
//            else if(msg instanceof String) {
//                if(nextSieve == null) {
//                    System.out.println("Outside synch");
//                    synchronized(waitLock) {
//                        waitLock.notify();
//                    }
//                    getContext().stop(getSelf());
//                } else {
//                    getContext().watch(nextSieve);
//                    nextSieve.tell("", getSelf());
//                }
//            } else if(msg instanceof Terminated) {
//                getContext().stop(getSelf());
//            }
        }
        
    }
    
    private static long printPrimes(int n) {
        long startTime = System.currentTimeMillis();
        
        System.out.println("1 ");
        
        if(n > 1) {
            ActorRef initSieve = system.actorOf(Sieve.props(2), "sieve-actor-2");
            synchronized(waitLock) {
                for(int i = 3; i <= n; i++)
                    initSieve.tell(i, ActorRef.noSender());
                initSieve.tell("", ActorRef.noSender());
                try {
                    waitLock.wait(300000);
                } catch(InterruptedException e) {
                    System.out.println();
                }
                system.terminate();
            }
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    public static void main(String[] args) {
        int n = 1000000;
        System.out.println("PRIMES UP TO " + n + ": ");
        long t = printPrimes(n);
        System.out.println("\n" + t + " ms. -> " + primeCount);
    }
    
}
