package weathertracker;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import org.junit.Test;

import weathertracker.measurements.Measurement;

public class MeasurementTest {
    double epsilon = 0.000001;

    @Test
    public void test() {
        Measurement.Builder builder = new Measurement.Builder();
        Measurement measurement = builder
                .withTimestamp(ZonedDateTime.now())
                .withMetric("foo", 1.001)
                .build();
        assertEquals(1.001, measurement.getMetric("foo"), epsilon);
    }

}