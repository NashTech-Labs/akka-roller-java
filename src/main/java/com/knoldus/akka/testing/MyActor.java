package com.knoldus.akka.testing;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class MyActor extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> sender().tell("Hello", self()))
                .matchAny(msg -> sender().tell(doSomeFunnyCalculations(msg), self()))
                .build();
    }

    public Object doSomeFunnyCalculations(Object x) {
        return x;
    }
}
