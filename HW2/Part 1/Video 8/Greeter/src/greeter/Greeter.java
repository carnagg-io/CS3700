package greeter;

public class Greeter {
    
    public static void main(String[] args) {
        Greeting myLambdaFunction = () -> System.out.println("Hello world!");
        myLambdaFunction.perform();
    }
}