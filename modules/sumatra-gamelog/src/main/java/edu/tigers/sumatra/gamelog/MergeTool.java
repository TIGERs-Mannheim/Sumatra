/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import edu.tigers.sumatra.gamelog.SSLGameLogReader.SSLGameLogfileEntry;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Tiny tool that can merge multiple logfiles.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class MergeTool
{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(MergeTool.class.getName());

	private List<String>				inputs;
	private String						output;
	private boolean					removeIdle	= false;


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


	/**
	 * @param removeIdle
	 * @return
	 */
	public MergeTool withRemoveIdleFrames(final boolean removeIdle)
	{
		this.removeIdle = removeIdle;
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
		List<SSLGameLogfileEntry> fullLog = inputs.stream()
				.map(i -> {
					SSLGameLogReader reader = new SSLGameLogReader();
					reader.loadFileBlocking(i);
					return reader.getPackets();
				})
				.sorted(Comparator.comparingLong(l -> l.get(0).getTimestamp()))
				.flatMap(List::stream)
				.collect(Collectors.toCollection(LinkedList::new));

		log.info("Loading done, processing idle stages...");

		if (removeIdle)
		{
			removeIdleStages(fullLog);
		}

		log.info("Processing complete. Writing output file.");

		SSLGameLogWriter writer = new SSLGameLogWriter();
		writer.openPath(output);
		fullLog.forEach(writer::write);
		writer.close();

		log.info("Write complete");
	}


	@SuppressWarnings("squid:ForLoopCounterChangedCheck")
	private void removeIdleStages(final List<SSLGameLogfileEntry> fullLog)
	{
		long timeOffset = 0;
		int idleStartIndex = -1;
		for (int index = 0; index < fullLog.size(); index++)
		{
			SSLGameLogfileEntry entry = fullLog.get(index);

			if (entry.getRefereePacket().isPresent())
			{
				if (isIdle(entry.getRefereePacket().get()) && (idleStartIndex < 0))
				{
					// idle started
					idleStartIndex = index;
				}

				if (!isIdle(entry.getRefereePacket().get()) && (idleStartIndex > 0))
				{
					// idle ended
					long timeAdjust = entry.getTimestamp() - fullLog.get(idleStartIndex).getTimestamp();
					timeOffset -= timeAdjust;
					fullLog.subList(idleStartIndex, index).clear();
					index = idleStartIndex;
					idleStartIndex = -1;
				}
			}

			entry.adjustTimestamp(timeOffset);
		}
	}


	private boolean isIdle(final Referee ref)
	{
		List<Stage> idleStages = new ArrayList<>();
		idleStages.add(Stage.NORMAL_FIRST_HALF_PRE);
		idleStages.add(Stage.NORMAL_SECOND_HALF_PRE);
		idleStages.add(Stage.EXTRA_FIRST_HALF_PRE);
		idleStages.add(Stage.EXTRA_SECOND_HALF_PRE);
		idleStages.add(Stage.EXTRA_TIME_BREAK);
		idleStages.add(Stage.NORMAL_HALF_TIME);
		idleStages.add(Stage.PENALTY_SHOOTOUT_BREAK);
		idleStages.add(Stage.POST_GAME);

		List<Command> idleCmds = new ArrayList<>();
		idleCmds.add(Command.HALT);
		idleCmds.add(Command.TIMEOUT_BLUE);
		idleCmds.add(Command.TIMEOUT_YELLOW);

		return idleStages.contains(ref.getStage()) || idleCmds.contains(ref.getCommand());
	}
}
