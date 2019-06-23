/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 4, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Step through all frames and look for issues:
 * * high delays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyFrameProcessor
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BerkeleyFrameProcessor.class.getName());
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		String basePath = "../sumatra-main/data/record";
		// String recordDbPath = "2016-04-03_13-47-47";
		// String recordDbPath = "2016-04-03_13-39-31";
		// String recordDbPath = "2016-04-03_13-19-59"; // 800ms
		// String recordDbPath = "2016-04-03_12-58-58";
		String recordDbPath = "2016-04-05_16-06-43";
		ETeamColor ourTeamColor = ETeamColor.BLUE;
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(basePath + "/" + recordDbPath, true);
		
		long lastTs = 0;
		long key = db.getFirstKey();
		
		do
		{
			RecordFrame recFrame = db.getRecordFrame(key);
			
			VisualizationFrame frame = recFrame.getVisFrame(ourTeamColor);
			if (frame != null)
			{
				if (frame.getSimpleWorldFrame().getCamFrame() == null)
				{
					log.error("cam frame null");
				} else
				{
					long ts = frame.getSimpleWorldFrame().getCamFrame().gettCapture();
					long diff = ts - lastTs;
					if ((lastTs != 0) && (diff > 1e8))
					{
						SimpleDateFormat sdf = new SimpleDateFormat();
						
						log.error("pause:" + diff + " time: " + sdf.format(new Date(recFrame.getTimestampMs())));
						
						long t1 = db.getRecordFrame(db.getPreviousKey(key)).getTimestamp();
						long t2 = frame.getTimestamp();
						long t3 = db.getRecordFrame(db.getNextKey(key)).getTimestamp();
						System.out.println(t1 + " " + t2 + " " + t3);
					}
					lastTs = ts;
				}
			} else
			{
				log.error("no vis frame in rec frame");
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
}
