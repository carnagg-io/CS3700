package memoizationexample;

import java.util.HashMap;
import java.util.Map;

class Fibber {

    private Map<Integer, Integer> memo = new HashMap<>();

    public int fib(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Index was negative. No such thing as a negative index in a series.");
        } else if (n == 0 || n == 1) {
            return n;
        }

        if (memo.containsKey(n)) {
            System.out.printf("grabbing memo[%d]\n", n);
            return memo.get(n);
        }

        System.out.printf("computing fib(%d)\n", n);
        int result = fib(n - 1) + fib(n - 2);

        memo.put(n, result);

        return result;
    }

}
