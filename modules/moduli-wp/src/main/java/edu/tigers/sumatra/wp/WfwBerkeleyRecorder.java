/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Berkeley recorder for AI data
 */
@Log4j2
public class WfwBerkeleyRecorder implements IBerkeleyRecorder
{
	private static final int MAX_BUFFER_SIZE = 1000;
	private final Queue<WorldFrameWrapper> worldFrames = new ConcurrentLinkedQueue<>();
	private final WfwObserver wfwObserver = new WfwObserver();
	private final BerkeleyDb db;
	private boolean droppingFrames = false;


	/**
	 * New AI berkeley storage
	 */
	public WfwBerkeleyRecorder(BerkeleyDb db)
	{
		this.db = db;
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(wfwObserver);
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(wfwObserver);
	}


	@Override
	public void flush()
	{
		List<WorldFrameWrapper> frameToSave = new ArrayList<>();
		WorldFrameWrapper wfw = worldFrames.poll();
		while (wfw != null)
		{
			frameToSave.add(wfw);
			wfw = worldFrames.poll();
		}
		db.write(WorldFrameWrapper.class, frameToSave);
	}


	private class WfwObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
		{
			if (worldFrames.size() > MAX_BUFFER_SIZE)
			{
				if (!droppingFrames)
				{
					log.warn("WFW buffer is full. Dropping frames!");
					droppingFrames = true;
				}
			} else
			{
				worldFrames.add(wFrameWrapper);
				droppingFrames = false;
			}
		}
	}
}
