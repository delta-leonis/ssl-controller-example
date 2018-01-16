package io.leonis.example;

import com.google.common.collect.*;
import io.leonis.game.engine.AverageStrategySupplier;
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

  private final static Map<String, String> DEFAULTS = ImmutableMap.of(
      "port", "10001",
      "ip", "224.0.0.1");

  /**
   * Constructs a new ControllerExample which submits {@link io.leonis.subra.protocol.Robot generated
   * commands} to multicast on the supplied IP and port.
   *
   * @param ip The IP of the multicast destination as a {@link String}
   * @param port The port of the multicast destination as an integer.
   */
  public ControllerExample(
      final String ip,
      final int port,
      final Map<JamepadControllerIdentity, Set<PlayerIdentity>> controllerMapping
  ) throws IOException {
    // the controller handler which parses the active controls
    final Function<JamepadController, PlayerCommand> handler = new JamepadControllerHandler();

    // create a stream of gamepad states based on the mapping
    Flux.from(new JamepadPublisher(controllerMapping))
        .map(MappingSupplier::getAgentMapping)
        // for each controller,
        .map(controllers ->
            // create a stream
            controllers.entrySet().stream()
                .flatMap(mapping ->
                    mapping.getValue().stream()
                        .map(identity ->
                            // of identities paired to commands
                            new SimpleImmutableEntry<>(identity, handler.apply(mapping.getKey()))))
                .collect(Collectors.groupingBy(Entry::getKey))
                .entrySet().stream()
                // and collect those pairs to a mapping of identities to lists of commands
                .collect(Collectors.toMap(
                    Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(Entry::getValue)
                        .collect(Collectors.toList()))))
        // compute the average command per identity and save it as a strategy
        .map(AverageStrategySupplier::new)
        // broadcast the strategy over multicast
        .subscribe(
            new StrategyMulticastSubscriber<>(
                new MulticastSubscriber(InetAddress.getByName(ip), port)));
  }

  public static void main(final String[] args) throws IOException {
    final Map<String, String> params = new CliSettings(DEFAULTS).apply(args);

    // the mapping of controller number to player identity
    final Map<JamepadControllerIdentity, Set<PlayerIdentity>> controllerMapping = ImmutableMap.of(
        new JamepadControllerIdentity(1),
        ImmutableSet.of(
            new PlayerIdentity(1, TeamColor.BLUE),
            new PlayerIdentity(2, TeamColor.BLUE)));

    new ControllerExample(params.get("ip"), Integer.parseInt(params.get("port")), controllerMapping);
  }
}
