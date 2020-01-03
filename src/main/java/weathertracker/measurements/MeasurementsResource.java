package weathertracker.measurements;

import javax.validation.Valid;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/measurements")
public class MeasurementsResource {
  private final MeasurementStore store;
  private final DateTimeFormatter dateTimeFormatter;

  public MeasurementsResource(MeasurementStore store, DateTimeFormatter dateTimeFormatter) {
    this.store = store;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  // features/01-measurements/01-add-measurement.feature
  @PostMapping
  public ResponseEntity<?> createMeasurement(@Valid @RequestBody Measurement measurement) {
    store.add(measurement);

    return ResponseEntity
      .created(URI.create("/measurements/" +  dateTimeFormatter.format(measurement.getTimestamp())))
      .build();
  }

  // features/01-measurements/02-get-measurement.feature
  @GetMapping("/{timestamp}")
  public ResponseEntity<Measurement> getMeasurement(@PathVariable ZonedDateTime timestamp) {
    Measurement measurement = store.fetch(timestamp);

    if (measurement != null) {
      return ResponseEntity.ok(measurement);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
