package com.knoldus.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import static java.lang.System.out;

/**
 * Created by knoldus on 31/12/16.
 */
public class LegacyActor extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof String) {
            String msg = (String) message;

            if(msg.equals("Good Morning")) {
                out.println("Actor: A very good morning to you");
            } else if(msg.equals("You're terrible")) {
                out.println("Actor: Seriously ?");
            }
        }
    }
}

class WorstShakespeareanMain {

    private final ActorSystem system = ActorSystem.apply("WorstShakespeareanMain");
    private final ActorRef legacyActorRef = system.actorOf(Props.create(LegacyActor.class));


    public void send(String msg) throws InterruptedException {
        out.println("Me: " + msg);
        legacyActorRef.tell(msg, ActorRef.noSender());
        Thread.sleep(100);
    }

    public void actorSystemTerminate() {
        system.terminate();
    }

    public static void main(String[] args) throws InterruptedException {
        WorstShakespeareanMain worstShakespeareanMain = new WorstShakespeareanMain();
        worstShakespeareanMain.send("Good Morning");
        worstShakespeareanMain.send("You're terrible");
        worstShakespeareanMain.actorSystemTerminate();
    }
}
