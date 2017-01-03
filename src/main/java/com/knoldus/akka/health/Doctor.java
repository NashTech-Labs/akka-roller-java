package com.knoldus.akka.health;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import com.knoldus.akka.health.HeartLungMachine.PullBack;
import com.knoldus.akka.health.OperationTheater.PassMeTheMachine;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Doctor {
    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("OperationSimulation");
        final ActorRef theatre = system.actorOf(Props.create(OperationTheater.class),"OperationTheatre");
        final ExecutionContext ec = system.dispatcher();

        // Grab the controls
        CompletableFuture<Object> heartLungMachineF = ask(theatre, new PassMeTheMachine() {
        }, Timeout.apply(5, SECONDS)).toCompletableFuture();

        final ActorRef heartLungMachine = (ActorRef) heartLungMachineF.get(5, SECONDS);

        system.scheduler().scheduleOnce(Duration.apply(200, MILLISECONDS),
                heartLungMachine, new PullBack(1f), ec, ActorRef.noSender()
        );

        system.scheduler().scheduleOnce(Duration.apply(1, SECONDS),
                heartLungMachine, new PullBack(0f), ec, ActorRef.noSender()
        );

        system.scheduler().scheduleOnce(Duration.apply(2, SECONDS),
                heartLungMachine, new PullBack(0.5f), ec, ActorRef.noSender()
        );

        system.scheduler().scheduleOnce(Duration.apply(3, SECONDS),
                heartLungMachine, new PullBack(0f), ec, ActorRef.noSender()
        );

        // Shut down
//        system.scheduler().scheduleOnce(Duration.apply(3, SECONDS),
//                () -> system.shutdown(), ec
//        );

    }
}
