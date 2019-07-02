package edu.tigers.sumatra.statistics;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.influxdb.dto.Point;


/**
 * Write time series data to a file in InfluxDB line protocol.
 * <p/>
 * The complete file can be uploaded to an InfluxDB with:
 * 
 * <pre>
 * curl -u tigers -i -XPOST 'https://influxdb.tigers-mannheim.de/write?db=matchStats' --data-binary @target/matchStats/2019-03-24_18-59-53_sreuO.matchStats
 * </pre>
 */
public class TimeSeriesFileWriter implements ITimeSeriesWriter
{
	private static final Logger log = Logger.getLogger(TimeSeriesFileWriter.class.getName());
	private static final String BASE_DIR = "target/matchStats/";
	private Path filePath;
	private FileWriter fileWriter;
	private boolean firstEntry;
	
	
	@Override
	public void start()
	{
		filePath = newStatsFile();
		firstEntry = true;
	}
	
	
	@Override
	public void stop()
	{
		if (fileWriter != null)
		{
			try
			{
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e)
			{
				log.warn("Could not close file writer", e);
			}
			fileWriter = null;
		}
	}
	
	
	@Override
	public void add(TimeSeriesStatsEntry entry)
	{
		if (firstEntry)
		{
			firstEntry = false;
			fileWriter = createFileWriter();
		}
		
		if (fileWriter == null)
		{
			return;
		}
		
		final Point point = Point.measurement(entry.getMeasurement())
				.time(entry.getTimestamp(), TimeUnit.NANOSECONDS)
				.tag(entry.getTagSet())
				.fields(entry.getFieldSet())
				.build();
		
		try
		{
			fileWriter.write(point.lineProtocol() + "\n");
		} catch (IOException e)
		{
			log.warn("Could not write matchStats", e);
		}
	}
	
	
	private FileWriter createFileWriter()
	{
		try
		{
			return new FileWriter(filePath.toFile());
		} catch (IOException e)
		{
			log.warn("Could not create file writer", e);
		}
		return null;
	}
	
	
	private void setupBaseDir()
	{
		boolean baseDirCreated = new File(BASE_DIR).mkdirs();
		if (baseDirCreated)
		{
			log.debug("Match stats base dir created");
		}
	}
	
	
	private Path newStatsFile()
	{
		setupBaseDir();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		sdf.setTimeZone(TimeZone.getDefault());
		String randomPart = RandomStringUtils.randomAlphabetic(5);
		String statsFileName = sdf.format(new Date()) + "_" + randomPart + ".matchStats";
		return new File(BASE_DIR + statsFileName).toPath();
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("filePath", filePath)
				.toString();
	}
}
