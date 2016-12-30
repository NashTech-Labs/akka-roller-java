package com.knoldus.akka;

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
public class FirstActor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("Good Morning", msg -> out.println("Actor: A very good morning to you"))
                .matchEquals("You're terrible", msg -> out.println("Actor: Seriously ?"))
                .build();
    }
}

class BadShakespeareanMain {

    private static final ActorSystem system = ActorSystem.apply("BadShakespearean");
    private static final ActorRef firstActorRef;

    static {
        firstActorRef = system.actorOf(Props.create(FirstActor.class));
    }

    public static void send(String msg) throws InterruptedException {
        System.out.println("Me: " + msg);
        firstActorRef.tell(msg, ActorRef.noSender());
        Thread.sleep(100);
    }

    public static void main(String[] args) throws InterruptedException {
        send("Good Morning");
        send("You're terrible");
        system.terminate();
    }
}
