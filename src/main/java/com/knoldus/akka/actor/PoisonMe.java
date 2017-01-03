package com.knoldus.akka.actor;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;

public class PoisonMe extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> out.println("I process almost anything! like - "+ msg))
                .build();
    }

    @Override
    public void postStop() throws Exception {
        out.println("Aah! , I would seek revenge! ");
    }
}

class PoisonPillMe {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("Now");
        final ActorRef show = system.actorOf(Props.create(PoisonMe.class));
        show.tell("hey1", ActorRef.noSender());
        show.tell("hey2", ActorRef.noSender());
        show.tell("hey3", ActorRef.noSender());
        show.tell("hey4", ActorRef.noSender());
        show.tell("hey5", ActorRef.noSender());
        show.tell("hey6", ActorRef.noSender());
        show.tell(PoisonPill.getInstance(), ActorRef.noSender()); // Try Kill
        show.tell("hey again", ActorRef.noSender());
        system.terminate();
    }
}
