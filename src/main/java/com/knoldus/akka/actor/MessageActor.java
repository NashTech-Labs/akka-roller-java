package com.knoldus.akka.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static com.knoldus.akka.actor.Messages.SendMessage;

/**
 * Created by knoldus on 30/12/16.
 */
public class MessageActor extends AbstractLoggingActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> log().info("Ok, i got it from {}", sender())).build();
    }
}

class MessageSender extends AbstractLoggingActor {

    private final ActorRef messageActor;

    public MessageSender(ActorRef messageActor) {
        this.messageActor = messageActor;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.matchEquals(SendMessage, msg -> {
            log().info("The sender to this actor is {}", sender());
            messageActor.tell("Hi", self());
            messageActor.forward("Hi", context());
        }).build();
    }
}

enum Messages {SendMessage}

class TestMessages {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("MessageSimulation");
        final ActorRef messageActorRef = system.actorOf(Props.create(MessageActor.class));
        final ActorRef messageSenderRef = system.actorOf(Props.create(MessageSender.class, messageActorRef));
        messageSenderRef.tell(SendMessage, ActorRef.noSender());
    }
}