/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyTimestampExporter
{
	private static final int	BUFFER_SIZE	= 4000;
	
	static
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException
	{
		String in = "2015-04-26_10-59_ER-Force_vs_TIGERS_Mannheim";
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(in, true);
		int dbSize = db.size();
		StringBuilder sb = new StringBuilder();
		for (int posLower = 0; posLower < dbSize; posLower += BUFFER_SIZE)
		{
			List<IRecordFrame> frames = db.load(posLower, BUFFER_SIZE);
			for (IRecordFrame frame : frames)
			{
				sb.append(frame.getWorldFrame().getSystemTime().getTime());
				sb.append('\n');
			}
		}
		Files.write(Paths.get("/tmp", "timestamps"), sb.toString().getBytes());
		db.close();
	}
}
