package io.leonis.example;

import com.google.common.collect.ImmutableMap;
import io.leonis.ipc.CliSettings;
import io.leonis.subra.game.data.TeamColor;
import io.leonis.subra.game.engine.RobotMeasurementsDeducer;
import io.leonis.subra.protocol.Robot;
import io.leonis.zosma.ipc.db.InfluxSubscriber;
import io.leonis.zosma.ipc.ip.UDPPublisher;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.influxdb.dto.Point;
import reactor.core.publisher.Flux;

/**
 * The Class InfluxDbExample.
 *
 * This class demonstrates the use of {@link InfluxSubscriber} and the {@link Robot.Measurements}
 * protocol.
 *
 * @author Rimon Oz
 */
public class InfluxDbExample {

  private final static Map<String, String> DEFAULTS = ImmutableMap.of(
      "local-port", "10000",
      "db-address", "http://localhost:8086/",
      "db-name", "test",
      "team-color", "BLUE");

  /**
   * Constructs a new handler of {@link Robot.Measurements} packets which persists received
   * measurements to the database through the {@link InfluxSubscriber}.
   *
   * @param localUdpPort    The local UDP-port on which the handler should listen for
   *                        {@link Robot.Measurements} packets.
   * @param databaseAddress The URL of the InfluxDB database (eg. <code>http://localhost:8086/</code>
   * @param databaseName    The name of the database to write the measurements to.
   */
  public InfluxDbExample(
      final int localUdpPort,
      final String databaseAddress,
      final String databaseName
  ) {
    // listen for packets locally
    Flux.from(new UDPPublisher(localUdpPort))
        // parse the packets as Robot.Measurements
        .transform(new RobotMeasurementsDeducer(TeamColor.NONE))
        .map(measurements ->
            Point.measurement("Robot #" + measurements.getPlayerIdentity().getId())
                .fields(measurements.getMeasurements().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue))))
        .map(Point.Builder::build)
        // and send all Points to influxdb
        .subscribe(new InfluxSubscriber(databaseAddress, databaseName));
  }

  public static void main(String[] args) {
    final Map<String, String> params = new CliSettings(DEFAULTS).apply(args);
    new InfluxDbExample(
        Integer.parseInt(params.get("local-port")),
        params.get("db-address"),
        params.get("db-name"));
  }
}
