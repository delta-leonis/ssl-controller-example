package io.leonis.ipc;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.*;
import lombok.AllArgsConstructor;

/**
 * The Class CliSettings.
 *
 * A {@link Function} which transforms a {@link String[] string array} to a {@link Map} merged with
 * the provided defaults. Extra keys will be ignored.
 *
 * @author Jeroen de Jong
 */
@AllArgsConstructor
public final class CliSettings implements Function<String[], Map<String, String>> {

  /**
   * Default settings. note that all keys should be present
   */
  private final Map<String, String> defaults;

  public CliSettings() {
    this(ImmutableMap.of(
      "port", "1234",
      "ip", "localhost"));
  }

  /**
   * @param args Array of strings in {@code <key>:<value>}-format
   * @return The input as a map merged with the existing defaults.
   */
  @Override
  public Map<String, String> apply(final String[] args) {
    Map<String, String> input = Stream.of(args)
      .map(arg -> arg.split(":"))
      .collect(Collectors.toMap(strings -> strings[0], strings -> strings[1]));
    return this.defaults.entrySet().stream().collect(Collectors.toMap(
      Entry::getKey,
      def -> input.getOrDefault(def.getKey(), def.getValue())));
  }
}
