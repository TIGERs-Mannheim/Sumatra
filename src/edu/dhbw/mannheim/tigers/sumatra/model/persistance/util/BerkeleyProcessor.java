/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


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
		Sumatra.touch();
		SumatraSetupHelper.setupSumatra();
		SumatraSetupHelper.changeLogLevel(Level.INFO);
	}
	
	@SuppressWarnings("unused")
	private static final Logger			log							= Logger.getLogger(BerkeleyProcessor.class.getName());
	
	private static int						FRAME_OUT_BUFFER_SIZE	= 4000;
	private static int						FRAME_IN_BUFFER_SIZE		= 4000;
	
	private final List<IRecordFrame>		frameOutBuffer				= new ArrayList<>(FRAME_OUT_BUFFER_SIZE);
	private RecordBerkeleyPersistence	outDb							= null;
	private String								outDbName					= null;
	private int									id								= 0;
	
	
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
		Files.list(Paths.get(RecordBerkeleyPersistence.BASE_PATH))
				.filter(path -> path.toFile().isDirectory())
				.filter(fileName -> fileName.toFile().getName().startsWith("record_"))
				.sorted()
				.forEach(path -> bp.processDb(path.toFile().getName()));
		bp.close();
	}
	
	
	/**
	 * @param dbPath
	 */
	public void processDb(final String dbPath)
	{
		log.info("Processing " + dbPath);
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(dbPath, true);
		int dbSize = db.size();
		for (int posLower = 0; posLower < dbSize; posLower += FRAME_IN_BUFFER_SIZE)
		{
			List<IRecordFrame> frames = db.load(posLower, FRAME_IN_BUFFER_SIZE);
			for (IRecordFrame frame : frames)
			{
				if (endOfGame(frame))
				{
					if (!frameOutBuffer.isEmpty())
					{
						writeOut();
					}
					close();
				} else if (acceptFrame(frame))
				{
					frameOutBuffer.add(frame);
					if (frameOutBuffer.size() >= FRAME_OUT_BUFFER_SIZE)
					{
						writeOut();
					}
				}
			}
		}
		db.close();
	}
	
	
	/**
	 * 
	 */
	public void close()
	{
		if ((outDb != null))
		{
			log.info("Closing " + outDbName + " with " + outDb.size() + " frames");
			outDb.close();
			String zipName = RecordBerkeleyPersistence.BASE_PATH + "/" + outDbName + ".zip";
			log.info("Start zipping to " + zipName);
			long tStart = System.nanoTime();
			ZipUtil.pack(new File(RecordBerkeleyPersistence.BASE_PATH + "/" + outDbName), new File(
					zipName));
			long tEnd = System.nanoTime();
			float time = (tEnd - tStart) / 1e9f;
			log.info("Zipping took " + time + "s");
			outDb = null;
			outDbName = null;
			id = 0;
		}
	}
	
	
	private String getDbOutName()
	{
		if (frameOutBuffer.isEmpty())
		{
			log.error("Can not determine DB name: No frame available!");
			return "undetermined";
		}
		IRecordFrame lastFrame = frameOutBuffer.get(frameOutBuffer.size() - 1);
		IRecordFrame firstFrame = frameOutBuffer.get(0);
		String blueName = lastFrame.getLatestRefereeMsg().getTeamInfoBlue().getName();
		String yellowName = lastFrame.getLatestRefereeMsg().getTeamInfoYellow().getName();
		blueName = blueName.replaceAll("[ ]", "_");
		yellowName = yellowName.replaceAll("[ ]", "_");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String time = sdf.format(firstFrame.getWorldFrame().getSystemTime());
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
		for (IRecordFrame frame : frameOutBuffer)
		{
			frame.setId(id);
			id++;
		}
		outDb.saveFrames(frameOutBuffer);
		frameOutBuffer.clear();
	}
	
	
	private boolean acceptFrame(final IRecordFrame frame)
	{
		if (frame.getLatestRefereeMsg() == null)
		{
			return false;
		}
		switch (frame.getLatestRefereeMsg().getStage())
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
		switch (frame.getTacticalField().getGameState())
		{
			case CORNER_KICK_THEY:
			case CORNER_KICK_WE:
			case DIRECT_KICK_THEY:
			case DIRECT_KICK_WE:
			case GOAL_KICK_THEY:
			case GOAL_KICK_WE:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case PREPARE_PENALTY_THEY:
			case PREPARE_PENALTY_WE:
			case RUNNING:
			case STOPPED:
			case THROW_IN_THEY:
			case THROW_IN_WE:
				break;
			case BREAK:
			case HALTED:
			case POST_GAME:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case UNKNOWN:
				return false;
		}
		return true;
	}
	
	
	private boolean endOfGame(final IRecordFrame frame)
	{
		if (frame.getLatestRefereeMsg() == null)
		{
			return true;
		}
		switch (frame.getLatestRefereeMsg().getStage())
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
