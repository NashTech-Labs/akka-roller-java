package com.knoldus.akka.remoting;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.Scanner;

import static java.lang.System.out;

public class LocalActor extends AbstractActor {

    private final ActorSelection remote;

    protected LocalActor(ActorSelection remote) {
        this.remote = remote;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(Send.class, msg -> remote.tell(msg, self()))
                .match(Get.class, msg -> {
                    out.println("----------------------------------------------------------");
                    out.println("Message Received from Remote :" + msg.getMessage());
                    out.println("----------------------------------------------------------");
                }).build();
    }
}

class LocalApplication {
    public static void main(String[] args) {

        final String configuration = "akka.actor.provider=remote \n" +
                "akka.remote.enabled-transports=[\"akka.remote.netty.tcp\"] \n" +
                "akka.remote.netty.tcp.hostname=\"127.0.0.1\" \n" +
                "akka.remote.netty.tcp.port=2552";

        /** These settings can be externalized  */
        final Config config = ConfigFactory.parseString(configuration);

        final ActorSystem system = ActorSystem.apply("Node1", ConfigFactory.load(config));


        final ActorSelection remoteActorReference =
                system.actorSelection("akka.tcp://Node3@" + "127.0.0.1" + ":" + 2553 + "/user/Discovery");


        final ActorRef local = system.actorOf(Props.create(LocalActor.class, remoteActorReference));
        final Scanner scanner = new Scanner(System.in);

        out.println("Send message to Remote");

        while (true) {
            String input = scanner.nextLine();
            local.tell(new Send(input), ActorRef.noSender());
        }
    }
}

final class Send implements Serializable {

    private final String message;

    public Send(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "Send{" +
                "message='" + message + '\'' +
                '}';
    }
}

final class Get implements Serializable {

    private final String message;

    public Get(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "Get{" +
                "message='" + message + '\'' +
                '}';
    }
}