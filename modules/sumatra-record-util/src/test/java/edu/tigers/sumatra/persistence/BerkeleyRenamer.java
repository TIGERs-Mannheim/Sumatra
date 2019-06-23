/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Rename databases based on referee info from last frame in DB
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyRenamer
{
	static
	{
		// init logging
		SumatraModel.changeLogLevel(Level.INFO);
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BerkeleyRenamer.class.getName());
	
	
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
		Files.list(Paths.get(RecordManager.getDefaultBasePath()))
				.filter(path -> path.toFile().isDirectory())
				.filter(fileName -> fileName.toFile().getName().startsWith("2016"))
				.sorted()
				.forEach(path -> bp.processDb(path.toFile().getAbsolutePath()));
	}
	
	
	/**
	 * @param dbPath
	 */
	public void processDb(final String dbPath)
	{
		log.info("Processing " + dbPath);
		AiBerkeleyPersistence db = new AiBerkeleyPersistence(dbPath);
		db.open();
		RecordFrame frame = db.getRecordFrame(db.getFirstKey());
		String dbname = getDbOutName(frame);
		db.close();
		
		File f = new File(dbPath);
		File nf = new File(RecordManager.getDefaultBasePath() + "/" + dbname);
		System.out.println(f + " -> " + nf);
		boolean renamed = f.renameTo(nf);
		if (!renamed)
		{
			System.err.println("Renaming failed.");
		}
	}
	
	
	private String getDbOutName(final RecordFrame frame)
	{
		if (frame == null)
		{
			log.error("Can not determine DB name: No frame available!");
			return "undetermined-" + System.currentTimeMillis();
		}
		WorldFrameWrapper lastFrame = frame.getWorldFrameWrapper();
		
		if (lastFrame.getRefereeMsg() != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
			String time = sdf.format(new Date(lastFrame.getRefereeMsg().getPacketTimestamp() / 1000));
			String blueName = lastFrame.getRefereeMsg().getTeamInfoBlue().getName().replaceAll("/", "_");
			String yellowName = lastFrame.getRefereeMsg().getTeamInfoYellow().getName().replaceAll("/", "_");
			blueName = blueName.replaceAll("[ ]", "_");
			yellowName = yellowName.replaceAll("[ ]", "_");
			
			return String.format("%s_%s_vs_%s", time, yellowName, blueName);
		}
		return String.valueOf(System.currentTimeMillis());
	}
}
