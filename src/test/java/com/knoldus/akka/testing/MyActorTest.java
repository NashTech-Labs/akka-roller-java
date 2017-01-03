package com.knoldus.akka.testing;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MyActorTest {

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
    public void testMyActor() {
        final Props props = Props.create(MyActor.class);
        final TestActorRef<MyActor> myActorRef = TestActorRef.create(system, props);
        MyActor myActor = myActorRef.underlyingActor();
        assertThat(myActor.doSomeFunnyCalculations(5), is(equalTo(5)));
    }
}
