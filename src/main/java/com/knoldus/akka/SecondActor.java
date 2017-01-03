package com.knoldus.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import static java.lang.System.out;

public class SecondActor extends AbstractActor {

    public SecondActor() {
        receive(ReceiveBuilder
                .matchEquals("Good Morning", msg -> out.println("Actor: A very good morning to you"))
                .matchEquals("You're terrible", msg -> out.println("Actor: Seriously ?"))
                .build()
        );
    }
}

class CheapShakespeareanMain {

    private final ActorSystem system = ActorSystem.apply("CheapShakespeareanMain");
    private final ActorRef secondActorRed = system.actorOf(Props.create(SecondActor.class));


    public void send(String msg) throws InterruptedException {
        out.println("Me: " + msg);
        secondActorRed.tell(msg, ActorRef.noSender());
        Thread.sleep(100);
    }

    public void actorSystemTerminate() {
        system.terminate();
    }

    public static void main(String[] args) throws InterruptedException {
        CheapShakespeareanMain cheapShakespeareanMain = new CheapShakespeareanMain();
        cheapShakespeareanMain.send("Good Morning");
        cheapShakespeareanMain.send("You're terrible");
        cheapShakespeareanMain.actorSystemTerminate();
    }
}
