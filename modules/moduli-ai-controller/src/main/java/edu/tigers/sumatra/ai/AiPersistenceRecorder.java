/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABufferedPersistenceRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;


/**
 * Persistance recorder for AI data
 */
public class AiPersistenceRecorder extends ABufferedPersistenceRecorder<PersistenceAiFrame>
		implements IVisualizationFrameObserver
{
	/**
	 * New AI persistance storage
	 */
	public AiPersistenceRecorder(PersistenceDb db)
	{
		super(db, PersistenceAiFrame.class);
	}
	
	
	@Override
	public void start()
	{
		AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
		agent.addVisObserver(this);
	}
	
	
	@Override
	public void stop()
	{
		AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
		agent.removeVisObserver(this);
	}

	@Override
	public void onNewVisualizationFrame(final VisualizationFrame visFrame)
	{
		PersistenceAiFrame frame = new PersistenceAiFrame(visFrame.getTimestamp());
		frame.addVisFrame(visFrame);
		queue(frame);
	}
}
