package com.knoldus.akka.health;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class OperationTheater extends AbstractLoggingActor {

    private final ActorRef monitor = context().actorOf(Props.create(Monitor.class), "Monitor");
    private final ActorRef heartLungMachine = context().actorOf(Props.create(
            HeartLungMachine.class, () -> new HeartLungMachine(monitor)
    ), "HeartLungMachine");

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(PassMeTheMachine.class, msg ->
                        sender().tell(heartLungMachine, self())
                )
                .match(MonitorUpdate.class, msg ->
                        log().info("HeartRate is now: {}", msg.getRate())
                ).build();
    }

    public interface PassMeTheMachine {
    }
}
