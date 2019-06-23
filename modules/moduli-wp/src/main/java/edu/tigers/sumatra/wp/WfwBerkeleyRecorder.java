/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Berkeley recorder for AI data
 */
public class WfwBerkeleyRecorder implements IBerkeleyRecorder
{
	private final Queue<WorldFrameWrapper> worldFrames = new ConcurrentLinkedQueue<>();
	private final WfwObserver wfwObserver = new WfwObserver();
	private final BerkeleyDb db;
	
	
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
			worldFrames.add(wFrameWrapper);
		}
	}
}
