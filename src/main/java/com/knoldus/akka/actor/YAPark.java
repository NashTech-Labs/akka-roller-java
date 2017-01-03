package com.knoldus.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static akka.pattern.PatternsCS.ask;
import static com.knoldus.akka.actor.YAPark.attendant;
import static com.knoldus.akka.actor.YAPark.slotMonitor;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

class MyDriver extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("Request Parking", msg -> attendant.tell("LetMePark", self()))
                .matchAny(msg -> out.println("MyDriver")).build();
    }
}

class MySlotMonitor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("GiveMeEmptySlot", msg -> sender().tell(2, self()))
                .matchAny(msg -> out.println("Slot Monitor")).build();
    }
}

class MyAttendant extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("LetMePark", msg -> {
                    out.println("Got the message to park");
                    final Integer parkingSlot = (Integer) ask(slotMonitor, "GiveMeEmptySlot", Timeout.apply(5, SECONDS))
                            .toCompletableFuture().get(5, SECONDS);
                    out.println("parking at " + parkingSlot);
                })
                .matchAny(msg -> out.println("MyAttendant")).build();
    }
}

public class YAPark {

    public static final ActorSystem system = ActorSystem.apply("PL");
    public static final ActorRef driver = system.actorOf(Props.create(MyDriver.class), "MyDriver");
    public static final ActorRef slotMonitor = system.actorOf(Props.create(MySlotMonitor.class), "MySlotMonitor");
    public static final ActorRef attendant = system.actorOf(Props.create(MyAttendant.class), "MyAttendant");

    public static void main(String[] args) {
        driver.tell("Request Parking", ActorRef.noSender());
    }
}
