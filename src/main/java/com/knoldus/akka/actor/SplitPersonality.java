package com.knoldus.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;

/**
 * Created by knoldus on 30/12/16.
 */
public class SplitPersonality extends AbstractActor {

    private PartialFunction<Object, BoxedUnit> angry;
    private PartialFunction<Object, BoxedUnit> happy;

    private SplitPersonality() {
        angry = ReceiveBuilder
                .matchEquals("foo", msg -> out.println("I am already angry?"))
                .matchEquals("bar", msg -> {
                    out.println("Becoming happy now");
                    context().become(happy);
                }).build();

        happy = ReceiveBuilder
                .matchEquals("bar", msg -> out.println("I am already happy :-)"))
                .matchEquals("foo", msg -> {
                    out.println("Becoming angry now");
                    context().become(angry);
                }).build();
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("foo", msg -> {
                    out.println("Becoming angry now");
                    context().become(angry);
                })
                .matchEquals("bar", msg -> {
                    out.println("Becoming happy now");
                    context().become(happy);
                }).build();
    }
}

class SplitPersonalityRunner {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("Now");
        final ActorRef show = system.actorOf(Props.create(SplitPersonality.class));
        show.tell("foo", ActorRef.noSender());
        show.tell("foo", ActorRef.noSender());
        show.tell("bar", ActorRef.noSender());
        show.tell("bar", ActorRef.noSender());
        show.tell("foo", ActorRef.noSender());
        show.tell("foo", ActorRef.noSender());

        //system.terminate(); // Comment out this if it is indeterministic behavior
    }
}