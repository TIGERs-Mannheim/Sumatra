/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.export.CSVExporter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Log4j2
public class RedirectModelEstimatorRunner
{

	public static void main(String[] args)
	{
		var samples = fromFiles(List.of(
				"data/redirect-samples/2020-11-21_18-23-07-130_live.csv",
				"data/redirect-samples/2020-11-21_18-40-28-880_live.csv",
				"data/redirect-samples/2020-11-21_18-47-08-156_live.csv",
				"data/redirect-samples/2020-11-22_10-41-58-689_live.csv"
		));
		exportRedirectSamples(samples);

		var estimator = new RedirectModelEstimator(samples);
		log.info("Estimating model for angle");
		estimator.estimateModelForAngle();
	}


	private static List<RedirectSample> fromFiles(Collection<String> filename)
	{
		return filename.stream()
				.map(RedirectModelEstimatorRunner::getRedirectSamples)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}


	private static List<RedirectSample> getRedirectSamples(String filename)
	{
		try
		{
			var lines = Files.readAllLines(Paths.get(filename));
			return lines.stream()
					.map(s -> s.split(","))
					.map(Arrays::asList)
					.map(RedirectSample::fromValues)
					.collect(Collectors.toList());
		} catch (IOException e)
		{
			log.warn("Could not read file: {}", filename, e);
		}
		return Collections.emptyList();
	}


	private static void exportRedirectSamples(List<RedirectSample> samples)
	{
		try (var exporter = new CSVExporter("data/redirect-samples/", "estimator", CSVExporter.EMode.PREPEND_DATE))
		{
			samples.stream().map(RedirectSample::getNumberList).forEach(exporter::addValues);
		}
	}
}
