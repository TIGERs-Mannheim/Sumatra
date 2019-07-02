package edu.tigers.sumatra.statistics;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * An entry point for time series data, aligned to the InfluxDB line protocol.
 */
public class TimeSeriesStatsEntry
{
	private final String measurement;
	private final Map<String, String> tagSet = new TreeMap<>();
	private final Map<String, Object> fieldSet = new TreeMap<>();
	private final Long timestamp;
	
	
	public TimeSeriesStatsEntry(final String measurement, final Long timestamp)
	{
		assert !measurement.contains(" ") : measurement;
		assert timestamp >= 0;
		this.measurement = measurement;
		this.timestamp = timestamp;
	}
	
	
	public void addTag(String key, String value)
	{
		assertValidKey(key);
		assert !value.contains(" ") : value;
		assert !value.contains(",") : value;
		tagSet.put(key, value);
	}
	
	
	public void addField(String key, Object value)
	{
		assertValidKey(key);
		fieldSet.put(key, value);
	}
	
	
	private void assertValidKey(String key)
	{
		assert !key.contains(" ") : key;
		assert !key.contains(",") : key;
	}
	
	
	public String getMeasurement()
	{
		return measurement;
	}
	
	
	public Map<String, String> getTagSet()
	{
		return tagSet;
	}
	
	
	public Map<String, Object> getFieldSet()
	{
		return fieldSet;
	}
	
	
	public Long getTimestamp()
	{
		return timestamp;
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("measurement", measurement)
				.append("tagSet", tagSet)
				.append("fieldSet", fieldSet)
				.append("timestamp", timestamp)
				.toString();
	}
}
