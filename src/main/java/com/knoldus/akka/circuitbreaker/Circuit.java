package com.knoldus.akka.circuitbreaker;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.CircuitBreaker;
import akka.util.Timeout;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static akka.pattern.PatternsCS.ask;

/**
 * Created by knoldus on 6/1/17.
 */

class FastSlowAkkademyDb extends AbstractLoggingActor {

    private Map<String, String> map = new HashMap<>();

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(SetRequest.class, msg -> {
                    log().info("received SetRequest - key: {} value: {}", msg.getKey(), msg.getValue());
                    map.put(msg.getKey(), msg.getValue());
                    sender().tell(new Status.Success(msg.getKey()), self());
                })
                .match(GetRequest.class, msg -> {
                    Thread.sleep(70);
                    respondToGet(msg.getKey());
                })
                .matchAny(msg -> new Status.Failure(new ClassNotFoundException()))
                .build();
    }

    public void respondToGet(String key) {
        String response = map.get(key);
        if (response != null) {
            sender().tell(response, self());
        } else {
            sender().tell(new Status.Failure(new Exception(key)), self());
        }
    }
}

class SetRequest {
    private final String key;
    private final String value;

    public SetRequest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}

class GetRequest {
    private final String key;

    public GetRequest(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}

public class Circuit {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Akkademy");
        final ExecutionContext ec = system.dispatcher();

        CircuitBreaker circuitBreaker = new CircuitBreaker(ec, system.scheduler(), 10,
                Duration.create(1, "s"), Duration.create(1, "s"))
                .onOpen(() -> System.out.println("circuit breaker opened!"))
                .onClose(() -> System.out.println("circuit breaker closed!"))
                .onHalfOpen(() -> System.out.println("circuit breaker half-open"));

        final ActorRef db = system.actorOf(Props.create(FastSlowAkkademyDb.class));
        CompletionStage<Object> future = ask(db, new SetRequest("key", "value"),
                Timeout.apply(2, TimeUnit.SECONDS));

        IntStream.range(1, 1000001)
                .forEach(x -> {
                    try {
                        Thread.sleep(50);
                    } catch (Exception ex) {
                    }

                    CompletionStage<Object> askFuture = circuitBreaker.callWithCircuitBreakerCS(() ->
                            ask(db, new GetRequest("key"), Timeout.apply(2, TimeUnit.SECONDS))
                    );
                    askFuture.toCompletableFuture().
                            thenApply(y -> "got it: " + y)
                            .exceptionally(ex -> "error: " + ex.getMessage())
                            .thenAccept(value -> System.out.println(value));
                });

    }
}
