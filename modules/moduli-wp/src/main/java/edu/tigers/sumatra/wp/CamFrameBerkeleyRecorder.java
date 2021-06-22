/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Berkeley storage for cam frames
 */
@Log4j2
public class CamFrameBerkeleyRecorder implements IBerkeleyRecorder
{
	private static final int MAX_BUFFER_SIZE = 1000;
	private final Queue<Map<Integer, ExtendedCamDetectionFrame>> camFramesQueue = new ConcurrentLinkedQueue<>();
	private final Map<Integer, ExtendedCamDetectionFrame> camFrameMap = new HashMap<>();
	private final CamFrameObserver camObserver = new CamFrameObserver();
	private final BerkeleyDb db;
	private boolean droppingFrames = false;


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
		var camFrames = this.camFramesQueue.poll();
		while (camFrames != null)
		{
			long timestamp = camFrames.values().stream().mapToLong(CamDetectionFrame::gettCapture).max().orElseThrow();
			camFrameToSave.add(new BerkeleyCamDetectionFrame(timestamp, camFrames));
			camFrames = this.camFramesQueue.poll();
		}
		db.write(BerkeleyCamDetectionFrame.class, camFrameToSave);
	}


	private class CamFrameObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
		{
			camFrameMap.put(frame.getCameraId(), frame);
			camFrameMap.values().removeIf(f -> (frame.gettCapture() - f.gettCapture()) / 1e9 > 0.2);

			if (camFramesQueue.size() > MAX_BUFFER_SIZE)
			{
				if (!droppingFrames)
				{
					log.warn("CamFrame buffer is full. Dropping frames!");
					droppingFrames = true;
				}
			} else
			{
				camFramesQueue.add(new HashMap<>(camFrameMap));
				droppingFrames = false;
			}
		}
	}
}
