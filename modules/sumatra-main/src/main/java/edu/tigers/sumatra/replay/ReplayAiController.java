/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.replay;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.presenter.replay.IReplayController;
import edu.tigers.sumatra.views.ASumatraView;


public class ReplayAiController implements IReplayController
{
	private final List<IVisualizationFrameObserver> visFrameObservers = new CopyOnWriteArrayList<>();
	
	
	public ReplayAiController(List<ASumatraView> sumatraViews)
	{
		for (ASumatraView view : sumatraViews)
		{
			if (view.getPresenter() instanceof IVisualizationFrameObserver)
			{
				visFrameObservers.add((IVisualizationFrameObserver) view.getPresenter());
			}
		}
	}
	
	
	@Override
	public void update(final BerkeleyDb db, final long sumatraTimestampNs)
	{
		BerkeleyAiFrame frame = db.get(BerkeleyAiFrame.class, sumatraTimestampNs);
		if (frame == null)
		{
			return;
		}
		
		for (VisualizationFrame visFrame : frame.getVisFrames())
		{
			for (IVisualizationFrameObserver o : visFrameObservers)
			{
				o.onNewVisualizationFrame(visFrame);
			}
		}
	}
}
