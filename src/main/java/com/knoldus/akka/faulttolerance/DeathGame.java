package com.knoldus.akka.faulttolerance;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import static akka.actor.SupervisorStrategy.restart;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by knoldus on 31/12/16.
 */

class MyActor extends AbstractActor {

    private final ActorRef watchMe;

    protected MyActor(ActorRef watchMe) {
        this.watchMe = watchMe;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(Terminated.class, msg -> out.println(msg.actor().path().name() + " has died"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().watch(watchMe);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create(1, MINUTES),
                DeciderBuilder.matchAny(msg -> restart()).build()
        );
    }
}

class SomeOtherActor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> self().tell(PoisonPill.getInstance(), ActorRef.noSender()))
                .build();
    }
}

public class DeathGame {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("DeathGame");
        final ActorRef soa = system.actorOf(Props.create(SomeOtherActor.class, () -> new SomeOtherActor()));
        system.actorOf(Props.create(MyActor.class, () -> new MyActor(soa)));
        soa.tell("die", ActorRef.noSender());
    }
}
