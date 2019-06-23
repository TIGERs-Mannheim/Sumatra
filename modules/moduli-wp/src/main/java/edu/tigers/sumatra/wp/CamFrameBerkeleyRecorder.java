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
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * Berkeley storage for cam frames
 */
public class CamFrameBerkeleyRecorder implements IBerkeleyRecorder
{
	private final Queue<ExtendedCamDetectionFrame> camFrames = new ConcurrentLinkedQueue<>();
	private final CamFrameObserver camObserver = new CamFrameObserver();
	private final BerkeleyDb db;
	
	
	/**
	 * Create berkeley storage for cam frames
	 */
	public CamFrameBerkeleyRecorder(BerkeleyDb db)
	{
		this.db = db;
	}
	
	
	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(camObserver);
	}
	
	
	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(camObserver);
	}
	
	
	@Override
	public void flush()
	{
		List<BerkeleyCamDetectionFrame> camFrameToSave = new ArrayList<>();
		ExtendedCamDetectionFrame camFrame = camFrames.poll();
		while (camFrame != null)
		{
			camFrameToSave.add(new BerkeleyCamDetectionFrame(camFrame));
			camFrame = camFrames.poll();
		}
		db.write(BerkeleyCamDetectionFrame.class, camFrameToSave);
	}
	
	
	private class CamFrameObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
		{
			camFrames.add(frame);
		}
	}
}
