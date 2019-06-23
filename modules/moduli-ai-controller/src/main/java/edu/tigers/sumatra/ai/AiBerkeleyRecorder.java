/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;


/**
 * Berkeley recorder for AI data
 */
public class AiBerkeleyRecorder implements IBerkeleyRecorder
{
	private final Map<Long, BerkeleyAiFrame> recordFrames = new ConcurrentSkipListMap<>();
	private final AiObserver aiObserver = new AiObserver();
	private final BerkeleyDb db;
	
	private long latestValidTimestamp = 0;
	
	
	/**
	 * New AI berkeley storage
	 */
	public AiBerkeleyRecorder(BerkeleyDb db)
	{
		this.db = db;
	}
	
	
	@Override
	public void start()
	{
		AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
		agent.addVisObserver(aiObserver);
	}
	
	
	@Override
	public void stop()
	{
		AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
		agent.removeVisObserver(aiObserver);
	}
	
	
	@Override
	public void flush()
	{
		List<BerkeleyAiFrame> toSave = new ArrayList<>();
		for (Map.Entry<Long, BerkeleyAiFrame> entry : recordFrames.entrySet())
		{
			toSave.add(entry.getValue());
			latestValidTimestamp = entry.getKey();
			// we use a conc. map, so we can remove directly
			recordFrames.remove(entry.getKey());
		}
		
		if (toSave.isEmpty())
		{
			/*
			 * there is some weird bug that causes the thread to hang sometimes if pers.saveRecordFrames(toSave)
			 * is called with an empty list. Returning here early seams to fix this issue.
			 */
			return;
		}
		
		db.write(BerkeleyAiFrame.class, toSave);
	}
	
	private class AiObserver implements IVisualizationFrameObserver
	{
		@Override
		public void onNewVisualizationFrame(final VisualizationFrame frame)
		{
			addRecordFrame(frame);
		}
		
		
		/**
		 * Add a frame to the recorder.
		 * It will not be saved immediately, but only if buffer is full or recorder is closed
		 *
		 * @param visFrame to be added
		 */
		private void addRecordFrame(final VisualizationFrame visFrame)
		{
			// only accept frames that are not too old
			if (visFrame.getTimestamp() > latestValidTimestamp)
			{
				// copy frame, before modification
				VisualizationFrame frame = new VisualizationFrame(visFrame);
				// create new record frame if absent
				BerkeleyAiFrame recFrame = recordFrames.get(frame.getTimestamp());
				if (recFrame == null)
				{
					recFrame = new BerkeleyAiFrame(visFrame.getTimestamp());
					recordFrames.put(frame.getTimestamp(), recFrame);
				}
				// add new frame
				recFrame.addVisFrame(frame);
			}
		}
	}
}
