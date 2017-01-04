package com.knoldus.akka.future;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;
import scala.concurrent.*;

import static akka.pattern.PatternsCS.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class WithActor {

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        final ActorRef emailSender = system.actorOf(Props.create(EmailSenderActor.class));
        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        final Future<Object> f = Patterns.ask(emailSender, "Hello", timeout);
        final String result = (String) Await.result(f,timeout.duration());
        System.out.println(result);
    }

}

class EmailSenderActor extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> sender().tell("Hello", self()))
                .build();
    }
}