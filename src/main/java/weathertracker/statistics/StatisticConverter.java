package weathertracker.statistics;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
class StatisticConverter implements Converter<String, Statistic> {
  private final ObjectMapper mapper;

  public StatisticConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Statistic convert(String source) {
    return mapper.convertValue(source, Statistic.class);
  }
}
