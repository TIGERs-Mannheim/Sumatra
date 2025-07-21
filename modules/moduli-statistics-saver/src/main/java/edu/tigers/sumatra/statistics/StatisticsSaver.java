/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDBException;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A moduli module that saves time series statistics data
 */
public class StatisticsSaver extends AModule
{
	private static final Logger log = LogManager.getLogger(StatisticsSaver.class.getName());
	private static final String DB_NAME = "matchStats";
	private ITimeSeriesWriter timeSeriesWriter;
	private String identifierBase;


	@Override
	public void startModule()
	{
		super.startModule();

		identifierBase = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		if (StringUtils.isNotBlank(SumatraModel.getVersion()))
		{
			identifierBase += "_" + SumatraModel.getVersion();
		}

		EOperationMode operationMode = EOperationMode
				.valueOf(getSubnodeConfiguration().getString("operation-mode", EOperationMode.OFF.name()));

		startInfluxDbWriter(operationMode);
	}


	@Override
	public void stopModule()
	{
		super.stopModule();
		if (timeSeriesWriter != null)
		{
			timeSeriesWriter.stop();
		}
	}


	/**
	 * Add a new entry
	 *
	 * @param identifierSuffix the suffix to add to the identifier
	 * @param entry            the entry to add
	 */
	public void add(final String identifierSuffix, final TimeSeriesStatsEntry entry)
	{
		if (timeSeriesWriter != null)
		{
			addCommonTags(identifierSuffix, entry);
			timeSeriesWriter.add(entry);
		}
	}


	/**
	 * @return true, if there is a writer that data is written to
	 */
	public boolean hasWriter()
	{
		return timeSeriesWriter != null;
	}


	private InfluxDbConnectionParameters readInfluxDbConnectionParameters()
	{
		InfluxDbConnectionParameters p = new InfluxDbConnectionParameters();
		p.setUrl(System.getenv("SUMATRA_INFLUX_DB_URL"));
		p.setDbName(DB_NAME);
		p.setUsername(System.getenv("SUMATRA_INFLUX_DB_USERNAME"));
		p.setPassword(System.getenv("SUMATRA_INFLUX_DB_PASSWORD"));
		return p;
	}


	private ITimeSeriesWriter createTimeSeriesWriter(EOperationMode operationMode)
	{
		return switch (operationMode)
		{
			case FILE_LINE_PROTOCOL -> new TimeSeriesFileWriter();
			case INFLUX_DB ->
			{
				InfluxDbConnectionParameters influxDbConnectionParameters = readInfluxDbConnectionParameters();
				if (influxDbConnectionParameters.isComplete())
				{
					yield new InfluxDbWriter(influxDbConnectionParameters);
				}
				yield null;
			}
			default -> null;
		};
	}


	private void startInfluxDbWriter(EOperationMode operationMode)
	{
		timeSeriesWriter = createTimeSeriesWriter(operationMode);

		if (timeSeriesWriter != null)
		{
			try
			{
				timeSeriesWriter.start();
				log.info("Started {} with {}", identifierBase, timeSeriesWriter);
				return;
			} catch (InfluxDBException e)
			{
				log.debug("Failed to connect to InfluxDB", e);
			}
		}

		EOperationMode operationModeFallback = EOperationMode
				.valueOf(getSubnodeConfiguration().getString("operation-mode-fallback", EOperationMode.OFF.name()));
		if (operationModeFallback == EOperationMode.FILE_LINE_PROTOCOL)
		{
			log.info("Failed to connect to InfluxDB. Falling back to {}", operationModeFallback);
			startInfluxDbWriter(EOperationMode.FILE_LINE_PROTOCOL);
		}
	}


	private void addCommonTags(final String identifierSuffix, final TimeSeriesStatsEntry entry)
	{
		entry.addTag("identifier", identifierBase + "_" + identifierSuffix);
		entry.addTag("sumatra.version", SumatraModel.getVersion());
	}
}
