package com.knoldus.akka.future;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.runtime.BoxedUnit;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;


class EmailSender extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> sender().tell("Hello", self()))
                .matchAny(msg -> { throw new Exception(); })
                .build();
    }
}

class PDFGenerator extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(String.class, msg -> out.println("got this " + msg))
                .matchAny(msg -> out.println("got something else"))
                .build();
    }
}

public class WithActorNonBlocking {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.apply("FaultTestingSystem");
        final ActorRef emailSender = system.actorOf(Props.create(EmailSender.class));
        final ActorRef pdfGenerator = system.actorOf(Props.create(PDFGenerator.class));
        final Future<Object> f = Patterns.ask(emailSender, "Hello", Timeout.apply(5, SECONDS));
        //final Future<Object> f = Patterns.ask(emailSender, 1001, Timeout.apply(5, SECONDS));


        final ExecutionContext ec = system.dispatcher();

        f.onSuccess(new PartialFunction<Object, Object>() {
            @Override
            public boolean isDefinedAt(Object x) {
                return true;
            }

            @Override
            public Object apply(Object v1) {
                System.out.println("success");
                final String msg = (String) v1;
                pdfGenerator.tell(msg, ActorRef.noSender());
                return v1;
            }
        }, ec);

        f.onFailure(new PartialFunction<Throwable, Object>() {
            @Override
            public boolean isDefinedAt(Throwable x) {
                return true;
            }

            @Override
            public Object apply(Throwable v1) {
                System.out.println("failed");
                pdfGenerator.tell(v1, ActorRef.noSender());
                return v1;
            }
        }, ec);


    }
}

//public final class PrintResult<T> extends OnSuccess<T> {
//    @Override public final void onSuccess(T t) {
//        System.out.println(t);
//    }
//}
