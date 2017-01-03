package com.knoldus.akka.faulttolerance;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import scala.Option;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.*;
import static java.lang.System.out;

class Supervisor extends AbstractLoggingActor {

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.apply(1, TimeUnit.MINUTES),
                DeciderBuilder
                        .match(ArithmeticException.class, msg -> {
                            out.println("Resuming the child");
                            return resume();
                        })
                        .match(NullPointerException.class, msg -> restart())
                        .match(IllegalArgumentException.class, msg -> stop())
                        .match(Exception.class, msg -> escalate())
                        .build()
        );
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(Props.class, props -> sender().tell(context().actorOf(props), self()))
                .build();
    }
}

class Child extends AbstractActor {

    private int state = 0;

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        out.println("This is the ugly message that killed me " + message);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(Exception.class, msg -> {
                    throw msg;
                })
                .match(Integer.class, msg -> state = msg)
                .matchEquals("get", msg -> sender().tell(state, self()))
                .build();
    }
}

class FaultTestStarterActor extends AbstractActor {

    private final ActorRef mySupervisor = context().actorOf(Props.create(Supervisor.class), "supervisor");

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(ActorRef.class, child -> {
                    child.tell(10, self());
                    child.tell(new ArithmeticException(), self());
                    child.tell("get", self());
                })
                .match(String.class, msg -> mySupervisor.tell(Props.create(Child.class), self()))
                .match(Integer.class, msg -> out.println("State is " + msg))
                .build();
    }
}

public class SupervisorAndChild {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        system.actorOf(Props.create(FaultTestStarterActor.class))
                .tell("start", ActorRef.noSender());
    }
}
