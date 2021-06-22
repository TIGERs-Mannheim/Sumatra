/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;
import edu.tigers.sumatra.calibration.ICalibrationDataObserver;
import edu.tigers.sumatra.export.CSVExporter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class RedirectCalibrationStream implements ICalibrationDataObserver
{
	private final IRedirectDataCollector redirectDataCollector = new RedirectDataCollectorVisionFilter();
	@Getter
	private final List<RedirectSample> redirectSamples = new ArrayList<>();

	private CSVExporter sampleExporter;


	private void processSample(CalibrationDataSample sample)
	{
		if (sampleExporter == null)
		{
			redirectDataCollector.start();
			sampleExporter = new CSVExporter("data/redirect-data/", "live", CSVExporter.EMode.PREPEND_DATE);
		}
		sampleExporter.addValues(sample.getNumberList());

		redirectDataCollector.process(sample).ifPresent(redirectSamples::add);
	}


	private void reset()
	{
		flushRedirectSamples();
		if (sampleExporter != null)
		{
			redirectDataCollector.stop();
			sampleExporter.close();
			sampleExporter = null;
		}
	}


	private void flushRedirectSamples()
	{
		if (redirectSamples.isEmpty())
		{
			return;
		}
		try (var exporter = new CSVExporter("data/redirect-samples/", "live", CSVExporter.EMode.PREPEND_DATE))
		{
			redirectSamples.stream().map(RedirectSample::getNumberList).forEach(exporter::addValues);
			redirectSamples.clear();
		}
	}


	@Override
	public void onNewCalibrationData(CalibrationDataSample sample)
	{
		processSample(sample);
	}


	@Override
	public void onNoData()
	{
		reset();
	}
}
