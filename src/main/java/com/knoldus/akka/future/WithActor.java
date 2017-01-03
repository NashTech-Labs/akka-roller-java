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
import static java.util.concurrent.TimeUnit.SECONDS;

public class WithActor {

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        final ActorRef a = system.actorOf(Props.create(A.class));
        final CompletableFuture<Object> f = ask(a, "Hello", Timeout.apply(5, SECONDS))
                .toCompletableFuture();
        final String result = (String) f.get(5, SECONDS);
        System.out.println(result);
    }

}

class A extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> sender().tell("Hello", self()))
                .build();
    }
}