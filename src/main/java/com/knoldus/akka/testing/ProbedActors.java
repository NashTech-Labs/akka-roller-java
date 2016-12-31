package com.knoldus.akka.testing;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;

/**
 * Created by knoldus on 31/12/16.
 */
// An annoying Actor that just keeps screaming at us
class AnnoyingActor extends AbstractActor {

    private final ActorRef snooper;

    protected AnnoyingActor(ActorRef snooper) {
        this.snooper = snooper;
    }

    @Override
    public void preStart() throws Exception {
        self().tell("send", self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("send", msg -> {
                    snooper.tell("Hello!!!", self());
                    self().tell("send", self());
                }).build();
    }
}

// A nice Actor that just says Hi once
class NiceActor extends AbstractActor {

    private final ActorRef snooper;

    protected NiceActor(ActorRef snooper) {
        this.snooper = snooper;
    }

    @Override
    public void preStart() throws Exception {
        snooper.tell("Hi", self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> out.println())
                .build();
    }
}

public class ProbedActors {
}
