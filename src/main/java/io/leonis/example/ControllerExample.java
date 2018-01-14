package io.leonis.example;

import io.leonis.subra.game.data.*;
import io.leonis.subra.game.data.Player;
import io.leonis.subra.game.data.Player.PlayerIdentity;
import io.leonis.subra.ipc.network.StrategyMulticastSubscriber;
import io.leonis.subra.ipc.peripheral.*;
import io.leonis.subra.ipc.peripheral.JamepadController.JamepadControllerIdentity;
import io.leonis.zosma.ipc.ip.MulticastSubscriber;
import io.leonis.zosma.ipc.peripheral.Controller.MappingSupplier;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.*;
import lombok.Value;
import reactor.core.publisher.Flux;

/**
 * @author Jeroen de Jong
 */

public class ControllerExample {
  public static void main(final String[] args) throws IOException {
    // The mapping of controller number to player identity
    final Map<JamepadControllerIdentity, Set<PlayerIdentity>> mapping = new HashMap<>();
    mapping.put(new JamepadControllerIdentity(1), new HashSet<>(Arrays.asList(
                                                    new PlayerIdentity(1, TeamColor.BLUE),
                                                    new PlayerIdentity(2, TeamColor.BLUE))));

    final Function<JamepadController, PlayerCommand> handler = new JamepadControllerHandler();

    // Create a stream of gamepad states based on the mapping
    Flux.from(new JamepadPublisher(mapping))
        .map(MappingSupplier::getAgentMapping)
        .map(controllers ->
            controllers.entrySet().stream().collect(Collectors.toMap(
                a -> handler.apply(a.getKey()),
                Entry::getValue
            ))
        ).map(ControllerStrategySupplier::new)
        .subscribe(new StrategyMulticastSubscriber<>(new MulticastSubscriber(InetAddress.getByName("localhost"), 1234)));
  }

  @Value
  public static class ControllerStrategySupplier implements Strategy.Supplier {
    private final Map<PlayerCommand, Set<PlayerIdentity>> map;

    @Override
    public Map<PlayerIdentity, PlayerCommand> getStrategy() {
      return map.entrySet().stream().flatMap(
          entry -> entry.getValue().stream().collect(Collectors.toMap(Function.identity(), id -> entry.getKey())).entrySet().stream()
      ).collect(Collectors.toMap(
          Entry::getKey,
          Entry::getValue
      ));
    }
  }
}
