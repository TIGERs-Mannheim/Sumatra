package edu.tigers.sumatra.statistics;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;


/**
 * Time series data writer for a remote InfluxDB.
 */
public class InfluxDbWriter implements ITimeSeriesWriter
{
	private static final Logger log = Logger.getLogger(InfluxDbWriter.class.getName());
	private final InfluxDbConnectionParameters connectionParameters;
	private InfluxDB influxDB;
	
	
	public InfluxDbWriter(final InfluxDbConnectionParameters connectionParameters)
	{
		this.connectionParameters = connectionParameters;
	}
	
	
	@Override
	public void add(final TimeSeriesStatsEntry entry)
	{
		final Point.Builder builder = Point.measurement(entry.getMeasurement())
				.time(entry.getTimestamp(), TimeUnit.NANOSECONDS)
				.tag(entry.getTagSet())
				.fields(entry.getFieldSet());
		influxDB.write(builder.build());
	}
	
	
	@Override
	public void start()
	{
		influxDB = InfluxDBFactory.connect(
				connectionParameters.getUrl(),
				connectionParameters.getUsername(),
				connectionParameters.getPassword());
		influxDB.setDatabase(connectionParameters.getDbName());
		influxDB.enableGzip();
		influxDB.enableBatch(BatchOptions.DEFAULTS.exceptionHandler(
				(failedPoints, throwable) -> log.warn("Batch processing failed", throwable)));
	}
	
	
	@Override
	public void stop()
	{
		influxDB.flush();
		influxDB.close();
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("connectionParameters", connectionParameters)
				.toString();
	}
}
