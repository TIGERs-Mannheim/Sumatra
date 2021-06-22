/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;
import edu.tigers.sumatra.export.CSVExporter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@SuppressWarnings("squid:S106") // print to standard out
@Log4j2
public class RedirectDetectorRunner
{
	public static void main(String[] args) throws IOException
	{
		var samples = fromCsvFile("data/redirect-data/2020-11-25_20-19-18-639_live.csv");
		RedirectDataCollectorVisionFilter collector = new RedirectDataCollectorVisionFilter();
		var redirectSamples = detectRedirects(samples, collector);
		log.info("Found {} / {} redirect samples", collector.getNumValidSamples(), collector.getNumPotentialSamples());

		printRedirectSamples(redirectSamples);
		exportRedirectSamples(redirectSamples);
		exportRedirectSamplesLabeled(redirectSamples);
	}


	private static List<RedirectSample> detectRedirects(
			List<CalibrationDataSample> samples,
			IRedirectDataCollector collector)
	{
		return samples.stream()
				.map(collector::process)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}


	private static void printRedirectSamples(List<RedirectSample> samples)
	{
		for (int i = 0; i < samples.size(); i++)
		{
			var sample = samples.get(i);
			System.out.printf("%4d: %s\n", i, sample);
		}
	}


	private static void exportRedirectSamples(List<RedirectSample> samples)
	{
		try (var exporter = new CSVExporter("data/redirect-samples/", "runner", CSVExporter.EMode.PREPEND_DATE))
		{
			samples.stream().map(RedirectSample::getNumberList).forEach(exporter::addValues);
		}
	}


	private static void exportRedirectSamplesLabeled(List<RedirectSample> samples)
	{
		try (var exporter = new CSVExporter("data/redirect-data-labeled/", "runner", CSVExporter.EMode.PREPEND_DATE))
		{
			for (int i = 0; i < samples.size(); i++)
			{
				RedirectSample redirectSample = samples.get(i);
				for (var sample : redirectSample.getSamplesPre())
				{
					List<Number> numbers = new ArrayList<>(sample.getNumberList());
					numbers.addAll(List.of(i, 0));
					exporter.addValues(numbers);
				}
				for (var sample : redirectSample.getSamplesPost())
				{
					List<Number> numbers = new ArrayList<>(sample.getNumberList());
					numbers.addAll(List.of(i, 1));
					exporter.addValues(numbers);
				}
			}
		}
	}


	private static List<CalibrationDataSample> fromCsvFile(String filename) throws IOException
	{
		var lines = Files.readAllLines(Paths.get(filename));
		return lines.stream()
				.map(s -> s.split(","))
				.map(Arrays::asList)
				.map(CalibrationDataSample::fromValues)
				.collect(Collectors.toList());
	}
}
