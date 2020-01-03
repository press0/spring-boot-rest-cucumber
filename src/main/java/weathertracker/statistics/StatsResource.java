package weathertracker.statistics;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import weathertracker.measurements.Measurement;
import weathertracker.measurements.MeasurementQueryService;

@RestController
@RequestMapping("/stats")
public class StatsResource {
    private final MeasurementQueryService queryService;
    private final MeasurementAggregator aggregator;

    public StatsResource(MeasurementQueryService queryService, MeasurementAggregator aggregator) {
        this.queryService = queryService;
        this.aggregator = aggregator;
    }

    @GetMapping
    public List<AggregateResult> getStats(
            @RequestParam("metric") List<String> metrics,
            @RequestParam("stat") List<Statistic> stats,
            @RequestParam("fromDateTime") ZonedDateTime from,
            @RequestParam("toDateTime") ZonedDateTime to
    ) {
        List<Measurement> measurements = queryService.queryDateRange(from, to);
        return aggregator.analyze(measurements, metrics, stats);
    }

}
