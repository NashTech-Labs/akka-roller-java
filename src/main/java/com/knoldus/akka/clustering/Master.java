package com.knoldus.akka.clustering;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static akka.pattern.PatternsCS.ask;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Created by harmeet on 31/12/16.
 */
class TransformationFrontend extends AbstractActor {

    private List<ActorRef> backends = new ArrayList<>();
    int jobCounter = 0;

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(TransformationJob.class, job -> {
                    if (job.getText().isEmpty()) {
                        sender().tell(new JobFailed("Service unavailable, try again later", job), self());
                    } else {
                        jobCounter += 1;
                        backends.get(jobCounter % backends.size()).forward(job, context());
                    }
                })
                .match(BackendRegistration.class, msg -> {
                    if (!backends.contains(sender())) {
                        context().watch(sender());
                        backends.add(sender());
                    }
                })
                .match(Terminated.class, msg -> {
                    backends = backends.stream()
                            .filter(actorRef -> !actorRef.equals(msg.getActor()))
                            .collect(toList());
                }).build();
    }
}

public class Master {
    public static void main(String[] args) {

        // Override the configuration of the port when specified as program argument
        Config config = (args.length != 0) ?
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + args[0])
                : ConfigFactory.empty();

        config = config.withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
                .withFallback(ConfigFactory.load());

        final ActorSystem system = ActorSystem.create("ClusterSystem", config);
        final ActorRef frontend = system.actorOf(Props.create(TransformationBackend.class), "frontend");

        IntStream.range(1, 121).forEach(n -> {

            ask(frontend, new TransformationJob("hello-" + n), Timeout.apply(5, SECONDS))
                    .thenAccept(result -> out.println(result));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        system.shutdown();
    }
}

final class TransformationJob implements Serializable {
    private final String text;

    public TransformationJob(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return "TransformationJob{" +
                "text='" + text + '\'' +
                '}';
    }
}

final class TransformationResult implements Serializable {
    private final String text;

    public TransformationResult(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return "TransformationResult{" +
                "text='" + text + '\'' +
                '}';
    }
}

final class JobFailed implements Serializable {
    private final String reason;
    private final TransformationJob job;

    public JobFailed(String reason, TransformationJob job) {
        this.reason = reason;
        this.job = job;
    }

    public String getReason() {
        return this.reason;
    }

    public TransformationJob getJob() {
        return this.job;
    }

    @Override
    public String toString() {
        return "JobFailed{" +
                "reason='" + reason + '\'' +
                ", job=" + job +
                '}';
    }
}

interface BackendRegistration {
}