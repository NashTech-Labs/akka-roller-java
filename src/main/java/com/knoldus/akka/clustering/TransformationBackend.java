package com.knoldus.akka.clustering;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Spliterator;
import java.util.stream.StreamSupport;


public class TransformationBackend extends AbstractLoggingActor {

    private final Cluster cluster = Cluster.get(context().system());

    // subscribe to cluster changes, MemberUp
    // re-subscribe when restart
    @Override
    public void preStart() throws Exception {
        cluster.subscribe(self(), MemberUp.class);
    }

    @Override
    public void postStop() throws Exception {
        cluster.unsubscribe(self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(TransformationJob.class, text -> {
                    log().info("Job received");
                    sender().tell(new TransformationResult(text.getText().toUpperCase()), self());
                })
                .match(CurrentClusterState.class, state -> {
                    Spliterator<Member> members = state.getMembers().spliterator();
                    StreamSupport.stream(members, false)
                            .filter(member -> member.status().equals(MemberStatus.up()));
                })
                .match(MemberUp.class, m -> register(m.member()))
                .build();
    }

    public void register(Member member) {
        if (member.hasRole("frontend")) {
            ActorSelection frontEnd = context().actorSelection(new RootActorPath(member.address(), "")
                    .child("user").child("frontend"));
            frontEnd.tell(new BackendRegistration() {
            }, ActorRef.noSender());
        }
    }
}

//#backend

class TransformationBackendLaunch {
    public static void main(String[] args) {

        // Override the configuration of the port when specified as program argument
        Config config = (args.length != 0) ?
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + args[0])
                : ConfigFactory.empty();

        config = config.withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
                .withFallback(ConfigFactory.load());

        final ActorSystem system = ActorSystem.create("ClusterSystem", config);
        system.actorOf(Props.create(TransformationBackend.class), "backend");
    }
}
