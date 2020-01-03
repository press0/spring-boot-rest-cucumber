package weathertracker.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;

public class AggregateResult implements Comparable {
    private String metric;
    private Statistic statistic;
    private double value;

    public AggregateResult() {
    }

    public AggregateResult(String metric, Statistic statistic, double value) {
        this.metric = metric;
        this.statistic = statistic;
        this.value = value;
    }

    @JsonGetter("metric")
    public String getMetric() {
        return this.metric;
    }

    @JsonGetter("stat")
    public Statistic getStatistic() {
        return this.statistic;
    }

    @JsonGetter("value")
    public double getValue() {
        return this.value;
    }

    @Override
    public int compareTo(Object o) {
        AggregateResult a = (AggregateResult) o;
        return this.metric.equals(a.metric)
                && this.statistic.equals(a.statistic)
                && this.value == a.value
                ? 0 : -1;
    }
}
