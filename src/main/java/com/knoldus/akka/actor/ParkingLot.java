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
import static com.knoldus.akka.actor.ParkingLot.attendant;
import static com.knoldus.akka.actor.ParkingLot.slotMonitor;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by knoldus on 30/12/16.
 */

class Driver extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("Request Parking", msg -> attendant.tell("LetMePark", self()))
                .matchAny(msg -> out.println("Driver")).build();
    }
}

class SlotMonitor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("GiveMeEmptySlot", msg -> sender().tell(2, self()))
                .matchAny(msg -> out.println("Slot Monitor")).build();
    }
}

class Attendant extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("LetMePark", msg -> {
                    out.println("Got the message to park");
                    final Integer parkingSlot = (Integer) ask(slotMonitor, "GiveMeEmptySlot", Timeout.apply(5, SECONDS))
                            .toCompletableFuture().get(5, SECONDS);
                    out.println("parking at " + parkingSlot);
                })
                .matchAny(msg -> out.println("Attendant")).build();
    }
}

public class ParkingLot {

    public static final ActorSystem system = ActorSystem.apply("PL");
    public static final ActorRef driver = system.actorOf(Props.create(Driver.class), "Driver");
    public static final ActorRef slotMonitor = system.actorOf(Props.create(SlotMonitor.class), "SlotMonitor");
    public static final ActorRef attendant = system.actorOf(Props.create(Attendant.class), "Attendant");

    public static void main(String[] args) {
        driver.tell("Request Parking", ActorRef.noSender());
    }
}
