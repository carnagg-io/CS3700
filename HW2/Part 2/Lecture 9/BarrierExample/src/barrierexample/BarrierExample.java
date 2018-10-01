package barrierexample;

class BarrierExample {

    static class MyThread1 implements Runnable {

        private Barrier barrier;
        
        public MyThread1(Barrier barrier) {
            this.barrier = barrier;
        }

        public void run() {
            try {
                Thread.sleep(1000);
                System.out.println("MyThread1 waiting on barrier");
                barrier.block();
                System.out.println("MyThread1 has been released");
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }
        
    }

    static class MyThread2 implements Runnable {

        private Barrier barrier;
        
        public MyThread2(Barrier barrier) {
            this.barrier = barrier;
        }

        public void run() {
            try {
                Thread.sleep(3000);
                System.out.println("MyThread2 releasing blocked threads\n");
                barrier.release();
                System.out.println("MyThread1 releasing blocked threads\n");
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Barrier BR = new Barrier();
        Thread t1 = new Thread(new BarrierExample.MyThread1(BR));
        Thread t2 = new Thread(new BarrierExample.MyThread2(BR));

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

}
