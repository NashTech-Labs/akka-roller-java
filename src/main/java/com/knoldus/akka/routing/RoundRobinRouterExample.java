package com.knoldus.akka.routing;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.BroadcastPool;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.stream.IntStream;

import static java.lang.System.out;

class PrintlnActor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> {
                    Thread.sleep(500);
                    out.printf("Received message '%s' in actor %s \n", msg, self().path().name());
                }).build();
    }
}

public class RoundRobinRouterExample {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");

        // Try with RoundRobinPool, RandomPool, SmallestMailboxPool etc
        final ActorRef roundRobinRouter = system.actorOf(Props.create(PrintlnActor.class)
        .withRouter(new BroadcastPool(5)), "router");

        IntStream.range(1, 11)
                .forEach(num -> roundRobinRouter.tell(num, ActorRef.noSender()));
    }
}
