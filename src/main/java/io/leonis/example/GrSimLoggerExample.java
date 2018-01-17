package io.leonis.example;

import io.leonis.subra.ipc.network.GameStatePublisher;
import io.leonis.zosma.function.LambdaExceptions;
import io.leonis.zosma.ipc.ip.MulticastPublisher;
import java.io.IOException;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.*;
import org.robocup.ssl.*;
import reactor.core.publisher.Flux;

/**
 * The Class GrSimLoggerExample.
 *
 * This example expects grSim to be broadcasting on 224.5.23.2:10020 and ssl-refbox on
 * 224.5.23.1:10003.
 *
 * @author Rimon Oz
 */
@Slf4j
public class GrSimLoggerExample {

  public static void main(final String[] args) throws IOException {
    Flux.from(new GameStatePublisher(
        new MulticastPublisher<>(
            InetAddress.getByName("224.5.23.2"),
            10020,
            LambdaExceptions.rethrowFunction(Wrapper.WrapperPacket.parser()::parseFrom)
        ),
        new MulticastPublisher<>(
            InetAddress.getByName("224.5.23.1"),
            10003,
            LambdaExceptions.rethrowFunction(RefereeOuterClass.Referee.parser()::parseFrom))))
        .subscribe(new LoggingSubscriber<>());
  }

  public static class LoggingSubscriber<O> implements Subscriber<O> {

    @Override
    public void onSubscribe(final Subscription s) {
      s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(final O o) {
      log.info(o.toString());
    }

    @Override
    public void onError(final Throwable t) {
      log.error("Error: " + t.getMessage());
    }

    @Override
    public void onComplete() {
      log.info("Completed!");
    }

  }

}
