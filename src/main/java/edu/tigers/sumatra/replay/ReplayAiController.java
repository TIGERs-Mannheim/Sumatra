/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.replay;

import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.gui.replay.presenter.IReplayController;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.views.ASumatraView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


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
	public void update(final PersistenceDb db, final long sumatraTimestampNs)
	{
		PersistenceAiFrame frame = db.getTable(PersistenceAiFrame.class).get(sumatraTimestampNs);
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
