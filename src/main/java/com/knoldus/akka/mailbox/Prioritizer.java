package com.knoldus.akka.mailbox;

import akka.actor.*;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

class MyPrioMailbox extends UnboundedStablePriorityMailbox {

    // needed for reflective instantiation
    public MyPrioMailbox(ActorSystem.Settings settings, Config config) {

        // Create a new PriorityGenerator, lower prio means more important
        super(new PriorityGenerator() {
            @Override
            public int gen(Object message) {
                if (message.equals("highpriority"))
                    return 0; // 'highpriority messages should be treated first if possible
                else if (message.equals("lowpriority"))
                    return 2; // 'lowpriority messages should be treated last if possible
                else if (message.equals(PoisonPill.getInstance()))
                    return 3; // PoisonPill when no other left
                else
                    return 1; // By default they go between high and low prio
            }
        });

    }
}

class ShowStopper extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> System.out.println(" I process almost anything! like - " + msg))
                .build();
    }
}

public class Prioritizer {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("Now");
        final ActorRef show = system.actorOf(Props.create(ShowStopper.class)
                .withMailbox("prio-mailbox"));
        show.tell("lowpriority", ActorRef.noSender());
        show.tell("lowpriority", ActorRef.noSender());
        show.tell(PoisonPill.getInstance(), ActorRef.noSender());
        show.tell("lowpriority", ActorRef.noSender());
        show.tell("lowpriority", ActorRef.noSender());
        show.tell("something", ActorRef.noSender());
        show.tell("something", ActorRef.noSender());
        show.tell("lowpriority", ActorRef.noSender());
        show.tell("highpriority", ActorRef.noSender());

        //system.terminate();
    }
}
