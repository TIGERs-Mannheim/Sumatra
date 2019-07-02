package edu.tigers.sumatra.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * A moduli module that saves time series statistics data
 */
public class StatisticsSaver extends AModule
{
	private static final Logger log = Logger.getLogger(StatisticsSaver.class.getName());
	private ITimeSeriesWriter timeSeriesWriter;
	private String identifierBase;
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		super.startModule();
		
		identifierBase = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		if (StringUtils.isNotBlank(SumatraModel.getVersion()))
		{
			identifierBase += "_" + SumatraModel.getVersion();
		}
		
		EOperationMode operationMode = EOperationMode
				.valueOf(getSubnodeConfiguration().getString("operation-mode", EOperationMode.OFF.name()));
		EOperationMode operationModeFallback = EOperationMode
				.valueOf(getSubnodeConfiguration().getString("operation-mode-fallback", EOperationMode.OFF.name()));
		final InfluxDbConnectionParameters influxDbConnectionParameters = readInfluxDbConnectionParameters();
		
		if (operationMode == EOperationMode.INFLUX_DB && !influxDbConnectionParameters.isComplete())
		{
			log.info("Can not connect to InfluxDB. Missing connection parameters: " + influxDbConnectionParameters);
			timeSeriesWriter = createTimeSeriesWriter(operationModeFallback, influxDbConnectionParameters);
		} else
		{
			timeSeriesWriter = createTimeSeriesWriter(operationMode, influxDbConnectionParameters);
		}
		
		if (this.timeSeriesWriter != null)
		{
			this.timeSeriesWriter.start();
			log.info("Started " + identifierBase + " with " + this.timeSeriesWriter);
		}
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
	 * @param entry the entry to add
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
		p.setUrl(getSubnodeConfiguration().getString("influxdb-url"));
		p.setDbName(getSubnodeConfiguration().getString("influxdb-name"));
		p.setUsername(System.getenv("SUMATRA_INFLUX_DB_USERNAME"));
		p.setPassword(System.getenv("SUMATRA_INFLUX_DB_PASSWORD"));
		return p;
	}
	
	
	private ITimeSeriesWriter createTimeSeriesWriter(
			final EOperationMode operationMode,
			final InfluxDbConnectionParameters influxDbConnectionParameters)
	{
		switch (operationMode)
		{
			case FILE_LINE_PROTOCOL:
				return new TimeSeriesFileWriter();
			case INFLUX_DB:
				return new InfluxDbWriter(influxDbConnectionParameters);
			case OFF:
			default:
				break;
		}
		return null;
	}
	
	
	private void addCommonTags(final String identifierSuffix, final TimeSeriesStatsEntry entry)
	{
		entry.addTag("identifier", identifierBase + "_" + identifierSuffix);
		entry.addTag("sumatra.version", SumatraModel.getVersion());
	}
}