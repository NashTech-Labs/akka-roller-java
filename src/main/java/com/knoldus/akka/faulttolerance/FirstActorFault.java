package com.knoldus.akka.faulttolerance;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;


// Our Actor
public class FirstActorFault extends AbstractActor {

    // The 'Business Logic'
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchEquals("Good Morning", msg -> {
                    out.println("Actor: A very good morning to you");
                    throw new Exception();
                })
                .matchEquals("You're terrible", msg -> out.println("Actor: Seriously ?"))
                .build();
    }
}

class FaultTest {

    private final static ActorSystem system = ActorSystem.apply("BadShakespearean");
    private final ActorRef actor = system.actorOf(Props.create(FirstActorFault.class), "Greetings");

    public void send(String msg) throws InterruptedException {
        out.println("Me:  "+msg);
        actor.tell(msg, ActorRef.noSender());
        Thread.sleep(1000);
    }
    public static void main(String[] args) throws InterruptedException {
        FaultTest faultTest = new FaultTest();

        // And our driver
        faultTest.send("Good Morning");
        faultTest.send("You're terrible");

        //system.shutdown()
    }
}
