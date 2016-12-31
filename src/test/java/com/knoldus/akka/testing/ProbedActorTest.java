package com.knoldus.akka.testing;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by knoldus on 31/12/16.
 */
public class ProbedActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void destroy() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAnnoyingActorHello() {
        new JavaTestKit(system) {
            {
                final JavaTestKit probe = new JavaTestKit(system);
                final ActorRef actorRef = system.actorOf(
                        Props.create(AnnoyingActor.class, () -> new AnnoyingActor(probe.getRef())));
                probe.expectMsgEquals("Hello!!!");
                system.stop(actorRef);
            }
        };
    }

    @Test
    public void testNiceActorHi() {
        new JavaTestKit(system) {
            {
                final JavaTestKit probe = new JavaTestKit(system);
                final ActorRef actorRef = system.actorOf(
                        Props.create(NiceActor.class, () -> new NiceActor(probe.getRef())));
                probe.expectMsgEquals("Hi");
                system.stop(actorRef);
            }
        };
    }
}
