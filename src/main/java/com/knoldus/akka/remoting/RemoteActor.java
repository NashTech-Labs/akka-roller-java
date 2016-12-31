package com.knoldus.akka.remoting;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;

/**
 * Created by knoldus on 31/12/16.
 */
public class RemoteActor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .matchAny(msg -> out.println("got the message " + msg))
                .build();
    }
}

class DiscoveryService {
    public static void main(String[] args) {

        final String configuration = "akka.actor.provider=remote \n" +
                "akka.remote.enabled-transports=[\"akka.remote.netty.tcp\"] \n" +
                "akka.remote.netty.tcp.hostname=\"127.0.0.1\" \n" +
                "akka.remote.netty.tcp.port=2553";

        /* These settings can be externalized  */
        final Config config = ConfigFactory.parseString(configuration);

        final ActorSystem remoteSystem = ActorSystem.apply("Node3", ConfigFactory.load(config));
        final ActorRef discoveryActor = remoteSystem.actorOf(Props.create(RemoteActor.class), "Discovery");
        out.println(discoveryActor.path());

    }
}
