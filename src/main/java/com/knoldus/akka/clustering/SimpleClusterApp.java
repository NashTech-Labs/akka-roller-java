package com.knoldus.akka.clustering;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.ClusterDomainEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

class SimpleClusterListener extends AbstractLoggingActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(CurrentClusterState.class, state ->
                        log().info("Current members: {}", state.members().mkString(", "))
                )
                .match(MemberUp.class, member ->
                        log().info("Member detected as unreachable: {}", member)
                )
                .match(ClusterEvent.MemberRemoved.class, memberRemoved ->
                        log().info("Member is Removed: {} after {}",
                                memberRemoved.member(), memberRemoved.previousStatus())
                )
                .match(ClusterDomainEvent.class, msg -> log().info(""))
                .build();
    }
}

//sbt "run-main com.knoldus.akka.clustering.SimpleClusterApp 2551"

public class SimpleClusterApp {
    public static void main(String[] args) {
        if (args.length != 0) {
            // Override the configuration of the port
            // when specified as program argument
            System.setProperty("akka.remote.netty.tcp.port", args[0]);
        }

        // Create an Akka system
        final ActorSystem system = ActorSystem.create("ClusterSystem");
        final ActorRef clusterListener = system.actorOf(
                Props.create(SimpleClusterListener.class), "clusterListener");

        Cluster.get(system).subscribe(clusterListener, ClusterDomainEvent.class);
    }
}
