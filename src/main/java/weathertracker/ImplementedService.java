package weathertracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import weathertracker.measurements.Measurement;
import weathertracker.measurements.MeasurementQueryService;
import weathertracker.measurements.MeasurementStore;
import weathertracker.statistics.AggregateResult;
import weathertracker.statistics.MeasurementAggregator;
import weathertracker.statistics.Statistic;

@Service
class ImplementedService implements MeasurementQueryService, MeasurementStore, MeasurementAggregator {

    private final List<Measurement> measurements = new ArrayList<>();

    @Override
    public void add(Measurement measurement) {
        measurements.add(measurement);
    }

    @Override
    public Measurement fetch(ZonedDateTime timestamp) {
        long longFetched = timestamp.toEpochSecond();
        for (Measurement measurement : measurements) {
            long longStored = measurement.getTimestamp().toEpochSecond();
            if (longFetched == longStored) {
                return measurement;
            }
        }
        return null;
    }

    @Override
    public List<Measurement> queryDateRange(ZonedDateTime from, ZonedDateTime to) {
        List<Measurement> list = new ArrayList<>();
        long longFrom = from.toEpochSecond();
        long longTo = to.toEpochSecond();
        for (Measurement measurement : measurements) {
            long longStored = measurement.getTimestamp().toEpochSecond();
            if (longStored >= longFrom && longStored < longTo) {
                list.add(measurement);
            }
        }
        return list;
    }

    @Override
    public List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats) {

        Double[][] d = new Double[metrics.size()][stats.size()];
        Map<Integer, List<Double>> dl = new HashMap<>();

        for (Measurement measurement : measurements) {
            for (int i = 0; i < metrics.size(); i++) {
                for (int j = 0; j < stats.size(); j++) {
                    Double mm = measurement.getMetric(metrics.get(i));
                    if (mm == null) {
                        continue;
                    }
                    switch (stats.get(j)) {
                        case MAX:
                            if (d[i][j] == null || mm > d[i][j]) {
                                d[i][j] = mm;
                            }
                            break;
                        case MIN:
                            if (d[i][j] == null || mm < d[i][j]) {
                                d[i][j] = mm;
                            }
                            break;
                        case AVERAGE:
                            dl.putIfAbsent((i * 1000) + j, new ArrayList<>());
                            dl.get((i * 1000) + j).add(mm);
                            break;
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<Double>> entry : dl.entrySet()) {
            int i = entry.getKey() / 1000;
            int j = entry.getKey() - i * 1000;
            double sum = entry.getValue().stream().mapToDouble(dbl -> dbl).sum();
            long count = entry.getValue().size();
            d[i][j] = round2(sum / count);
        }

        List<AggregateResult> aggregateResults = new ArrayList<>();
        for (int i = 0; i < metrics.size(); i++) {
            for (int j = 0; j < stats.size(); j++) {
                if (d[i][j] != null) {
                    aggregateResults.add(new AggregateResult(metrics.get(i), stats.get(j), d[i][j]));
                }
            }
        }
        return aggregateResults;
    }

    private double round2(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
