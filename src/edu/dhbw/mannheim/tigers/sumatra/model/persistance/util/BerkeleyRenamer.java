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
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
public class BerkeleyRenamer
{
	static
	{
		// init logging
		Sumatra.touch();
		SumatraSetupHelper.setupSumatra();
		SumatraSetupHelper.changeLogLevel(Level.INFO);
	}
	
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(BerkeleyRenamer.class.getName());
	
	
	/**
	 * 
	 */
	public BerkeleyRenamer()
	{
	}
	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException
	{
		BerkeleyRenamer bp = new BerkeleyRenamer();
		Files.list(Paths.get(RecordBerkeleyPersistence.BASE_PATH))
				.filter(path -> path.toFile().isDirectory())
				.filter(fileName -> fileName.toFile().getName().startsWith("record_"))
				.sorted()
				.forEach(path -> bp.processDb(path.toFile().getName()));
	}
	
	
	/**
	 * @param dbPath
	 */
	public void processDb(final String dbPath)
	{
		log.info("Processing " + dbPath);
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(dbPath, true);
		List<IRecordFrame> frames = db.load(0, 1);
		String dbname = getDbOutName(frames);
		db.close();
		
		File f = new File(RecordBerkeleyPersistence.BASE_PATH + "/" + dbPath);
		File nf = new File(RecordBerkeleyPersistence.BASE_PATH + "/" + dbname);
		System.out.println(f + " -> " + nf);
		f.renameTo(nf);
	}
	
	
	private String getDbOutName(final List<IRecordFrame> frames)
	{
		if (frames.isEmpty())
		{
			log.error("Can not determine DB name: No frame available!");
			return "undetermined";
		}
		IRecordFrame lastFrame = frames.get(frames.size() - 1);
		IRecordFrame firstFrame = frames.get(0);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String time = sdf.format(firstFrame.getWorldFrame().getSystemTime());
		
		if (lastFrame.getLatestRefereeMsg() != null)
		{
			String blueName = lastFrame.getLatestRefereeMsg().getTeamInfoBlue().getName().replaceAll("/", "_");
			String yellowName = lastFrame.getLatestRefereeMsg().getTeamInfoYellow().getName().replaceAll("/", "_");
			blueName = blueName.replaceAll("[ ]", "_");
			yellowName = yellowName.replaceAll("[ ]", "_");
			
			return String.format("%s_%s_vs_%s", time, yellowName, blueName);
		}
		return time;
	}
}
