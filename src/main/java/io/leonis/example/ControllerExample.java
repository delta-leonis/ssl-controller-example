package io.leonis.example;

import com.google.common.collect.*;
import io.leonis.game.engine.ControllerStrategySupplier;
import io.leonis.ipc.CliSettings;
import io.leonis.subra.game.data.Player.PlayerIdentity;
import io.leonis.subra.game.data.*;
import io.leonis.subra.ipc.network.StrategyMulticastSubscriber;
import io.leonis.subra.ipc.peripheral.*;
import io.leonis.subra.ipc.peripheral.JamepadController.JamepadControllerIdentity;
import io.leonis.zosma.ipc.ip.MulticastSubscriber;
import io.leonis.zosma.ipc.peripheral.Controller.MappingSupplier;
import java.io.IOException;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;

/**
 * The Class ControllerExample.
 *
 * @author Jeroen de Jong
 */
public class ControllerExample {

  /**
   * Constructs a new ControllerExample which submits {@link io.leonis.subra.protocol.Robot generated
   * commands} to multicast on the supplied ip and port.
   *
   * @param ip The IP of the multicast destination as a {@link String}
   * @param port The port of the multicast destination as an integer.
   */
  public ControllerExample(final String ip, final int port) throws IOException {
    // the mapping of controller number to player identity
    final Map<JamepadControllerIdentity, Set<PlayerIdentity>> controllerMapping = ImmutableMap.of(
        new JamepadControllerIdentity(1),
        ImmutableSet.of(
            new PlayerIdentity(1, TeamColor.BLUE),
            new PlayerIdentity(2, TeamColor.BLUE)));

    // the controller handler which parses the active controls
    final Function<JamepadController, PlayerCommand> handler = new JamepadControllerHandler();

    // create a stream of gamepad states based on the mapping
    Flux.from(new JamepadPublisher(controllerMapping))
        .map(MappingSupplier::getAgentMapping)
        // for each controller,
        .map(controllers ->
            controllers.entrySet().stream()
                .flatMap(mapping ->
                    mapping.getValue().stream()
                        .map(identity -> new SimpleImmutableEntry<>(identity,
                            handler.apply(mapping.getKey()))))
                .collect(Collectors.groupingBy(Entry::getKey))
                .entrySet().stream()
                .collect(Collectors.toMap(
                    Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(Entry::getValue)
                        .collect(Collectors.toList()))))
        .map(ControllerStrategySupplier::new)
        .subscribe(
            new StrategyMulticastSubscriber<>(
                new MulticastSubscriber(InetAddress.getByName(ip), port)));
  }

  public static void main(final String[] args) throws IOException {
    final Map<String, String> params = new CliSettings().apply(args);
    new ControllerExample(params.get("ip"), Integer.parseInt(params.get("port")));
  }
}
