package io.leonis.game.engine;

import io.leonis.subra.game.data.Player.PlayerIdentity;
import io.leonis.subra.game.data.*;
import io.leonis.subra.math.PlayerCommandRing;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

/**
 * The Class AverageStrategySupplier.
 *
 * Supplies a strategy based on a {@link Collection} of {@link PlayerCommand} mapped on
 * the applicable {@link PlayerIdentity}. It will average all fields of the {@link PlayerCommand}
 * available in the {@link Collection}.
 *
 * @author Rimon Oz
 * @author Jeroen de Jong
 */
@AllArgsConstructor
public final class AverageStrategySupplier implements Strategy.Supplier, PlayerCommandRing {

  /**
   * {@link List} of {@link PlayerCommand} mapped on the applicable {@link PlayerIdentity}.
   */
  private final Map<PlayerIdentity, List<PlayerCommand>> map;

  @Override
  public Map<PlayerIdentity, PlayerCommand> getStrategy() {
    return this.map.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue().stream()
                .reduce(this::add)
                .map(value ->
                    this.divide(
                        value,
                        new PlayerCommand.State(
                            entry.getValue().size(),
                            entry.getValue().size(),
                            entry.getValue().size(),
                            entry.getValue().size(),
                            entry.getValue().size(),
                            entry.getValue().size())))
                .orElse(PlayerCommand.State.STOP)));
  }
}
