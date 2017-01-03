package com.knoldus.akka.future;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;

import static akka.pattern.PatternsCS.ask;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;


class A1 extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> sender().tell("Hello", self()))
                .build();
    }
}

class A2 extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> out.println("got this " + msg))
                .matchAny(msg -> out.println("got something else"))
                .build();
    }
}

public class WithActorNonBlocking {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        final ActorRef a1 = system.actorOf(Props.create(A1.class));
        final ActorRef a2 = system.actorOf(Props.create(A2.class));
        final CompletableFuture<Object> f = ask(a1, "Hello", Timeout.apply(5, SECONDS))
                .toCompletableFuture();

        f.thenAccept(value -> {
            out.println("success");
            a2.tell(value, ActorRef.noSender());
        })
        .exceptionally(ex -> {
            out.println("failed");
            a2.tell(ex, ActorRef.noSender());
            return null;
        });
    }
}
