package com.knoldus.akka.testing;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SilentActorTest {

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
    public void testSilentActorState() {
        final SilentMessage message = new SilentMessage("whisper");

        final Props props = Props.create(SilentActor.class);
        final TestActorRef<SilentActor> silentActor = TestActorRef.create(system, props);
        silentActor.tell(message, ActorRef.noSender());

        SilentActor sa = silentActor.underlyingActor();
        assertThat(sa.internalState.size(), is(equalTo(1)));
        assertThat(sa.internalState, hasItems("whisper"));
    }

    @Test
    public void testSilentActorMultipleStates() {
        final SilentMessage message1 = new SilentMessage("whisper1");
        final SilentMessage message2 = new SilentMessage("whisper2");

        final Props props = Props.create(SilentActor.class);
        final TestActorRef<SilentActor> silentActor = TestActorRef.create(system, props);
        silentActor.tell(message1, ActorRef.noSender());
        silentActor.tell(message2, ActorRef.noSender());

        SilentActor sa = silentActor.underlyingActor();
        assertThat(sa.internalState, hasItems("whisper1", "whisper2"));
    }
}
