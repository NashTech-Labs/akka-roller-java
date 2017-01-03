package com.knoldus.akka.health;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;

public class Monitor extends AbstractLoggingActor {

    // We need an "ExecutionContext" for the scheduler.  This
    // Actor's dispatcher can serve that purpose.  The
    // scheduler's work will be dispatched on this Actor's own
    // dispatcher
    private final ExecutionContext ec = context().dispatcher();

    // The maximum ceiling of our heart in 'feet'
    private final int maxHeartRate = 200;

    // The maximum rate of climb for our heart in
    // 'feet per minute'
    private final int maxRateOfClimb = 10;

    // The varying rate of climb depending on the luMonitorng capacity
    private float rateOfClimb = 0f;

    // Our current heartRate
    private double heartRate = 0;

    private long lastTick = System.currentTimeMillis();

    // We need to periodically update our heart rate.  This
    // scheduled message send will tell us when to do that
    private Object ticker = context().system().scheduler()
            .schedule(Duration.apply(100, TimeUnit.MILLISECONDS),
                    Duration.apply(100, TimeUnit.MILLISECONDS), self(), new Tick() {
                    }, ec, ActorRef.noSender());

    // An internal message we send to ourselves to tell us
    // to update our heartRate
    public interface Tick {
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                // Our rate of climb has changed
                .match(RateChange.class, msg -> {
                    // Truncate the range of 'amount' to [-1, 1]
                    // before multiplying
                    float minimum = Math.min(msg.getAmount(), 1.0f);
                    float maximum = Math.max(minimum, -1.0f);
                    rateOfClimb = maximum * maxRateOfClimb;

                    log().info("monitor changed rate of climb to {}.", rateOfClimb);
                })

                .match(Tick.class, msg -> {
                    long tick = System.currentTimeMillis();
                    heartRate = heartRate + ((tick - lastTick) / 60000.0) * rateOfClimb;
                    lastTick = tick;
                }).build();
    }

    public static class RateChange {
        private final float amount;

        public RateChange(final float amount) {
            this.amount = amount;
        }

        public float getAmount() {
            return this.amount;
        }
    }
}

class MonitorUpdate {
    private final float rate;

    public MonitorUpdate(final float rate) {
        this.rate = rate;
    }

    public float getRate() {
        return rate;
    }
}
