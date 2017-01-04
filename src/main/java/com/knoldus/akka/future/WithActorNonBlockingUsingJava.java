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


class NotificationSender extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> sender().tell("Hello", self()))
                .matchAny(msg -> { throw new Exception(); })
                .build();
    }
}

class FusionGenerator extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> out.println("got this " + msg))
                .matchAny(msg -> out.println("got something else"))
                .build();
    }
}

public class WithActorNonBlockingUsingJava {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        final ActorRef notificationSender = system.actorOf(Props.create(NotificationSender.class));
        final ActorRef fusionGenerator = system.actorOf(Props.create(FusionGenerator.class));
        final CompletableFuture<Object> f = ask(notificationSender, "Hello", Timeout.apply(5, SECONDS))
                .toCompletableFuture();
//        final CompletableFuture<Object> f = ask(notificationSender, 1001, Timeout.apply(5, SECONDS))
//                .toCompletableFuture();

        f.thenAccept(msg -> {
            System.out.println("success");
            fusionGenerator.tell(msg, ActorRef.noSender());
        });

        f.exceptionally(ex -> {
            System.out.println("failed");
            fusionGenerator.tell(ex, ActorRef.noSender());
            return null;
        });
    }
}
