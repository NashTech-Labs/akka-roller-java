package com.knoldus.akka.testing;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyActorIntegrationTest {

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
    public void testMyActorIntegration() {
        new JavaTestKit(system) {
            {
                final Props props = Props.create(MyActor.class);
                final TestActorRef<MyActor> myActorRef = TestActorRef.create(system, props);
                myActorRef.tell(52, getRef());
                expectMsgEquals(52);
                myActorRef.tell("52", getRef());
                expectMsgEquals("Hello");
            }
        };

    }
}
