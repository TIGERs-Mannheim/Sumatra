/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Tiny tool that can merge multiple logfiles.
 *
 * @author AndreR <andre@ryll.cc>
 */
@Log4j2
public class MergeTool
{
	private List<String>				inputs;
	private String						output;
	private Function<GameLogMessage, GameLogCompareResult> filter;


	/**
	 * @param inputs
	 * @return
	 */
	public MergeTool withInputFiles(final List<String> inputs)
	{
		this.inputs = inputs;
		return this;
	}


	/**
	 * @param output
	 * @return
	 */
	public MergeTool withOutputFile(final String output)
	{
		this.output = output;
		return this;
	}


	public MergeTool withFilter(final Function<GameLogMessage, GameLogCompareResult> filter)
	{
		this.filter = filter;
		return this;
	}


	/**
	 * Merge files.
	 */
	public void merge()
	{
		new Thread(this::mergeBlocking, "Merge Tool").start();
	}


	/**
	 * Merge files.
	 */
	public void mergeBlocking()
	{
		if ((inputs == null) || (output == null))
		{
			return;
		}

		log.info("Loading logfiles files");

		// load all logfiles, sort by timestamp, combine into single list
		List<GameLogMessage> fullLog = inputs.stream()
				.map(i -> {
					GameLogReader reader = new GameLogReader();
					reader.loadFileBlocking(i);
					return reader.getMessages();
				})
				.sorted(Comparator.comparingLong(l -> l.get(0).getTimestampNs()))
				.flatMap(List::stream)
				.collect(Collectors.toCollection(LinkedList::new));

		log.info("Loading done, processing filter verdicts...");

		if (filter != null)
		{
			removeFilteredMessages(fullLog);
		}

		log.info("Processing complete. Writing output file.");

		GameLogWriter writer = new GameLogWriter(GameLogType.LOG_FILE);
		writer.openPath(output);
		fullLog.forEach(writer::write);
		writer.close();

		log.info("Write complete");
	}


	@SuppressWarnings("squid:ForLoopCounterChangedCheck")
	private void removeFilteredMessages(final List<GameLogMessage> fullLog)
	{
		long timeOffset = 0;
		int removeStartIndex = -1;
		for (int index = 0; index < fullLog.size(); index++)
		{
			GameLogMessage msg = fullLog.get(index);
			var verdict = filter.apply(msg);

			if(verdict == GameLogCompareResult.MATCH && removeStartIndex < 0)
			{
				removeStartIndex = index;
			}

			if(verdict == GameLogCompareResult.MISMATCH && removeStartIndex > 0)
			{
				long timeAdjust = msg.getTimestampNs() - fullLog.get(removeStartIndex).getTimestampNs();
				timeOffset -= timeAdjust;
				fullLog.subList(removeStartIndex, index).clear();
				index = removeStartIndex;
				removeStartIndex = -1;
			}

			msg.adjustTimestamp(timeOffset);
		}
	}


}
