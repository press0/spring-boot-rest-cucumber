package weathertracker.measurements;

import java.time.ZonedDateTime;
import java.util.List;

public interface MeasurementQueryService {
  List<Measurement> queryDateRange(ZonedDateTime from, ZonedDateTime to);
}
