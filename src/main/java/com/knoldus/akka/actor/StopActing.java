package com.knoldus.akka.actor;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;

/**
 * Created by knoldus on 30/12/16.
 */
class ShowStopper extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> out.println(" I process almost anything! like - "+msg))
                .build();
    }

    @Override
    public void postStop() throws Exception {
        out.println("Aah! , I would seek revenge! ");
    }
}

public class StopActing {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.apply("Now");
        final ActorRef show = system.actorOf(Props.create(ShowStopper.class));
        show.tell("hey", ActorRef.noSender());
        show.tell("hey again", ActorRef.noSender());
        Thread.sleep(100); //comment if required
        system.stop(show);
        show.tell("hey yet again", ActorRef.noSender());
        system.terminate();
    }
}
