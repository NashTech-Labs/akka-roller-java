package com.knoldus.akka.testing;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.List;

public class SilentActor extends AbstractActor {

    public final List<String> internalState = new ArrayList<>();

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(SilentMessage.class, msg -> internalState.add(msg.getData()))
                .match(GetState.class, msg -> msg.getReceiver().tell(internalState, self()))
                .build();
    }
}

final class SilentMessage {

    private final String data;

    public SilentMessage(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }
}

final class GetState {

    private final ActorRef receiver;

    public GetState(ActorRef receiver) {
        this.receiver = receiver;
    }

    public ActorRef getReceiver() {
        return this.receiver;
    }
}
