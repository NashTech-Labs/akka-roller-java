package com.knoldus.akka.future;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created by knoldus on 30/12/16.
 */
public class ComposingFuture {

    public int timeTakingIdentityFunction(int number) throws RuntimeException {
        try {
            // we sleep for 1 seconds and return number
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return number;
    }

    public void sumWithoutFuture() throws RuntimeException {
        long startTime = System.currentTimeMillis();

        int number1 = timeTakingIdentityFunction(1);
        int number2 = timeTakingIdentityFunction(2);
        int number3 = timeTakingIdentityFunction(3);
        int sum = number1 + number2 + number3;

        double elapsedTime = ((System.currentTimeMillis() - startTime) / 1000.0);
        System.out.println("Without Future Sum of 1, 2 and 3 is " + sum + " calculated in " + elapsedTime + " seconds");
    }

    public void sumWithFuture() throws Exception {
        long startTime = System.currentTimeMillis();

        CompletableFuture<Integer> number1 = supplyAsync(() -> timeTakingIdentityFunction(1));
        CompletableFuture<Integer> number2 = supplyAsync(() -> timeTakingIdentityFunction(2));
        CompletableFuture<Integer> number3 = supplyAsync(() -> timeTakingIdentityFunction(3));

        CompletableFuture.<Integer>allOf(number1, number2, number3).join();
        int sum = number1.get() + number2.get() + number3.get();

        double elapsedTime = ((System.currentTimeMillis() - startTime) / 1000.0);
        System.out.println("With Future Sum of 1, 2 and 3 is " + sum + " calculated in " + elapsedTime + " seconds");
    }

    public static void main(String[] args) throws Exception {
        ComposingFuture composingFuture = new ComposingFuture();
        composingFuture.sumWithoutFuture();
        composingFuture.sumWithFuture();
    }
}
