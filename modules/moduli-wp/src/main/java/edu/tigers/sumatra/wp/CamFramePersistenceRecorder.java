/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABufferedPersistenceRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.PersistenceCamDetectionFrame;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Persistence storage for cam frames
 */
@Log4j2
public class CamFramePersistenceRecorder extends ABufferedPersistenceRecorder<PersistenceCamDetectionFrame>
		implements IWorldFrameObserver
{
	private final Map<Integer, ExtendedCamDetectionFrame> camFrameMap = new HashMap<>();


	/**
	 * Create persistence storage for cam frames
	 */
	public CamFramePersistenceRecorder(PersistenceDb db)
	{
		super(db, PersistenceCamDetectionFrame.class);
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(this);
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(this);
	}

	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		camFrameMap.put(frame.getCameraId(), frame);
		camFrameMap.values().removeIf(f -> (frame.getTimestamp() - f.getTimestamp()) / 1e9 > 0.2);
		queue(new PersistenceCamDetectionFrame(frame.getTimestamp(), new HashMap<>(camFrameMap)));
	}
}
