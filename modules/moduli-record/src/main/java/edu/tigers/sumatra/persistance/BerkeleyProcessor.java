/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Run through existing databases and filter out uninteresting frames.
 * Only use frames from a game!
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyProcessor
{
	static
	{
		// init logging
		SumatraModel.changeLogLevel(Level.INFO);
	}
	
	@SuppressWarnings("unused")
	private static final Logger			log							= Logger.getLogger(BerkeleyProcessor.class.getName());
	
	private static int						FRAME_OUT_BUFFER_SIZE	= 4000;
	
	private final List<RecordFrame>		frameOutBuffer				= new ArrayList<>(FRAME_OUT_BUFFER_SIZE);
	private RecordBerkeleyPersistence	outDb							= null;
	private String								outDbName					= null;
	
	
	/**
	 * 
	 */
	public BerkeleyProcessor()
	{
	}
	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException
	{
		BerkeleyProcessor bp = new BerkeleyProcessor();
		Files.list(Paths.get(RecordBerkeleyPersistence.getDefaultBasePath()))
				.filter(path -> path.toFile().isDirectory())
				.filter(fileName -> fileName.toFile().getName().startsWith("record_"))
				.sorted()
				.forEach(path -> bp.processDb(path.toFile().getAbsolutePath()));
		bp.close();
	}
	
	
	/**
	 * @param dbPath
	 */
	public void processDb(final String dbPath)
	{
		log.info("Processing " + dbPath);
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(dbPath, true);
		
		long key = db.getFirstKey();
		
		do
		{
			RecordFrame recFrame = db.getRecordFrame(key);
			{
				RefereeMsg refereeMsg = recFrame.getWorldFrameWrapper().getRefereeMsg();
				if (endOfGame(refereeMsg))
				{
					if (!frameOutBuffer.isEmpty())
					{
						writeOut();
					}
					close();
				} else if (acceptFrame(refereeMsg))
				{
					frameOutBuffer.add(recFrame);
					if (frameOutBuffer.size() >= FRAME_OUT_BUFFER_SIZE)
					{
						writeOut();
					}
				}
			}
			Long nextKey = db.getNextKey(key);
			if (nextKey == null)
			{
				break;
			}
			key = nextKey;
		} while (true);
		db.close();
	}
	
	
	/**
	 * 
	 */
	public void close()
	{
		if ((outDb != null))
		{
			log.info("Closing " + outDbName);
			outDb.close();
			String zipName = RecordBerkeleyPersistence.getDefaultBasePath() + "/" + outDbName + ".zip";
			log.info("Start zipping to " + zipName);
			long tStart = System.nanoTime();
			ZipUtil.pack(new File(RecordBerkeleyPersistence.getDefaultBasePath() + "/" + outDbName), new File(
					zipName));
			long tEnd = System.nanoTime();
			double time = (tEnd - tStart) / 1e9;
			log.info("Zipping took " + time + "s");
			outDb = null;
			outDbName = null;
		}
	}
	
	
	private String getDbOutName()
	{
		if (frameOutBuffer.isEmpty())
		{
			log.error("Can not determine DB name: No frame available!");
			return "undetermined";
		}
		WorldFrameWrapper lastFrame = frameOutBuffer.get(frameOutBuffer.size() - 1).getWorldFrameWrapper();
		WorldFrameWrapper firstFrame = frameOutBuffer.get(0).getWorldFrameWrapper();
		String blueName = lastFrame.getRefereeMsg().getTeamInfoBlue().getName();
		String yellowName = lastFrame.getRefereeMsg().getTeamInfoYellow().getName();
		blueName = blueName.replaceAll("[ ]", "_");
		yellowName = yellowName.replaceAll("[ ]", "_");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String time = sdf.format(new Date(firstFrame.getRefereeMsg().getPacketTimestamp()));
		return String.format("%s_%s_vs_%s", time, yellowName, blueName);
	}
	
	
	private void writeOut()
	{
		if (outDbName == null)
		{
			outDbName = getDbOutName();
			log.info("Creating " + outDbName);
			outDb = new RecordBerkeleyPersistence(outDbName, false);
		}
		outDb.saveRecordFrames(frameOutBuffer);
		frameOutBuffer.clear();
	}
	
	
	private boolean acceptFrame(final RefereeMsg refereeMsg)
	{
		if (refereeMsg == null)
		{
			return false;
		}
		switch (refereeMsg.getStage())
		{
			case EXTRA_FIRST_HALF:
			case EXTRA_SECOND_HALF:
			case NORMAL_FIRST_HALF:
			case NORMAL_SECOND_HALF:
			case PENALTY_SHOOTOUT:
			case EXTRA_FIRST_HALF_PRE:
			case NORMAL_SECOND_HALF_PRE:
			case NORMAL_FIRST_HALF_PRE:
			case EXTRA_SECOND_HALF_PRE:
				break;
			case EXTRA_HALF_TIME:
			case EXTRA_TIME_BREAK:
			case NORMAL_HALF_TIME:
			case PENALTY_SHOOTOUT_BREAK:
			case POST_GAME:
				return false;
		}
		switch (refereeMsg.getCommand())
		{
			case HALT:
				return false;
			case DIRECT_FREE_BLUE:
			case DIRECT_FREE_YELLOW:
			case FORCE_START:
			case GOAL_BLUE:
			case GOAL_YELLOW:
			case INDIRECT_FREE_BLUE:
			case INDIRECT_FREE_YELLOW:
			case NORMAL_START:
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_PENALTY_BLUE:
			case PREPARE_PENALTY_YELLOW:
			case STOP:
			case TIMEOUT_BLUE:
			case TIMEOUT_YELLOW:
			default:
				break;
		}
		return true;
	}
	
	
	private boolean endOfGame(final RefereeMsg refereeMsg)
	{
		if (refereeMsg == null)
		{
			return true;
		}
		switch (refereeMsg.getStage())
		{
			case EXTRA_FIRST_HALF:
			case EXTRA_SECOND_HALF:
			case NORMAL_FIRST_HALF:
			case NORMAL_SECOND_HALF:
			case PENALTY_SHOOTOUT:
			case EXTRA_FIRST_HALF_PRE:
			case EXTRA_HALF_TIME:
			case EXTRA_SECOND_HALF_PRE:
			case EXTRA_TIME_BREAK:
			case NORMAL_HALF_TIME:
			case NORMAL_SECOND_HALF_PRE:
			case PENALTY_SHOOTOUT_BREAK:
				break;
			case NORMAL_FIRST_HALF_PRE:
			case POST_GAME:
				return true;
		}
		return false;
	}
}
