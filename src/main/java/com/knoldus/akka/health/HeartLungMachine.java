package com.knoldus.akka.health;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.knoldus.akka.health.Monitor.RateChange;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * Created by knoldus on 2/1/17.
 */
public class HeartLungMachine extends AbstractActor {

    private final ActorRef monitorRef;

    protected HeartLungMachine(final ActorRef monitorRef) {
        this.monitorRef = monitorRef;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                // Doctor pulled the stick back by a certain amount,
                // and we inform the monitor that we're climbing
                .match(PullBack.class, msg ->
                        monitorRef.tell(new RateChange(msg.getAmount()), self())
                )

                // Doctor pushes the stick forward and we inform the
                // monitor that we're descending
                .match(PushForward.class, msg ->
                        monitorRef.tell(new RateChange((msg.getAmount() * -1)), self())
                ).build();
    }

    // The HeartLungMachine object carries messages for
    // controlling the heart
    public static class PullBack {
        private final float amount;

        public PullBack(final float amount) {
            this.amount = amount;
        }

        public float getAmount() {
            return this.amount;
        }
    }

    public static class PushForward {
        private final float amount;

        public PushForward(final float amount) {
            this.amount = amount;
        }

        public float getAmount() {
            return this.amount;
        }
    }
}
