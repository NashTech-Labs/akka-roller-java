package com.knoldus.akka.faulttolerance;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.Option;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class LifeCycleHooks extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        System.out.println("Pre Start " + this);
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("Post Stop " + this);
        super.postStop();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        System.out.println("Pre RE Start " + this);
        super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Post RE Start " + this);
        super.postRestart(reason);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> {
                    throw new Exception();
                }).build();
    }
}

class LifeCycle {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("Now");
        final ActorRef lifeCycleHooks = system.actorOf(Props.create(LifeCycleHooks.class));
        lifeCycleHooks.tell("yikes", ActorRef.noSender());
    }
}
