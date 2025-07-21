/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.util;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@SuppressWarnings("java:S106") // allow System.out.println
public class BotTimings
{
	public void export(String name, Map<BotID, List<Long>> timestamps)
	{
		try (CSVExporter exporter = new CSVExporter("", name, CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME))
		{
			exporter.setHeader(List.of("timestamp", "botId", "diff"));
			getEntries(timestamps).stream().sorted(Comparator.comparing(RecordEntry::timestamp)).forEach(rec ->
					exporter.addValues(List.of(
							rec.timestamp,
							rec.botID,
							String.format(Locale.ENGLISH, "%.2f", rec.diff)
					)));
		}
	}


	private List<RecordEntry> getEntries(Map<BotID, List<Long>> timestamps)
	{
		List<RecordEntry> entries = new ArrayList<>();
		for (var entry : timestamps.entrySet())
		{
			List<Double> diffs = getDiffs(entry.getValue());
			for (int i = 0; i < diffs.size(); i++)
			{
				entries.add(new RecordEntry(entry.getValue().get(i), entry.getKey(), diffs.get(i)));
			}
		}
		return entries;
	}


	public void stats(Map<BotID, List<Long>> timestamps)
	{
		Map<BotID, List<Double>> diffsPerBot = getDiffsPerBot(timestamps);
		for (var entry : diffsPerBot.entrySet())
		{
			List<Double> diffs = entry.getValue();
			double dt = 0.015;
			long goods = diffs.stream().filter(e -> e <= dt).count();
			long bads = diffs.stream().filter(e -> e > dt && e < 5).count();
			double goodsSum = diffs.stream().filter(e -> e <= dt).mapToDouble(e -> e).sum();
			double badsSum = diffs.stream().filter(e -> e > dt && e < 5).mapToDouble(e -> e).sum();
			double quality = (double) goods / (goods + bads);
			double qualitySum = goodsSum / (goodsSum + badsSum);
			System.out.printf("%12s %7d %7d %7.2f | %7.2f %7.2f %7.2f %n",
					entry.getKey(), goods, bads, quality, goodsSum, badsSum, qualitySum);
		}

		for (var entry : diffsPerBot.entrySet())
		{
			List<Double> diffs = entry.getValue();
			var max = diffs.stream().filter(e -> e > 0.9 && e < 5).map(d -> String.format("%7.2f", d)).toList();
			System.out.printf("%12s %s %n", entry.getKey(), max);
		}
	}


	public Map<BotID, List<Double>> getDiffsPerBot(Map<BotID, List<Long>> timestamps)
	{
		Map<BotID, List<Double>> diffsPerBot = new HashMap<>();
		for (var entry : timestamps.entrySet())
		{
			List<Double> diffs = getDiffs(entry.getValue());
			diffsPerBot.put(entry.getKey(), diffs);
		}
		return diffsPerBot;
	}


	private List<Double> getDiffs(List<Long> timestamps)
	{
		List<Double> diffs = new ArrayList<>();
		for (int i = 1; i < timestamps.size(); i++)
		{
			diffs.add((timestamps.get(i) - timestamps.get(i - 1)) / 1e9);
		}
		return diffs;
	}


	private record RecordEntry(long timestamp, BotID botID, double diff)
	{
	}
}
