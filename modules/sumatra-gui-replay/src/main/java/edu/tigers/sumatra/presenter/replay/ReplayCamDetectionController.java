/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ReplayCamDetectionController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers = new CopyOnWriteArrayList<>();


	public ReplayCamDetectionController(List<ASumatraView> sumatraViews)
	{
		for (ASumatraView view : sumatraViews)
		{
			if (view.getPresenter() instanceof IWorldFrameObserver)
			{
				wFrameObservers.add((IWorldFrameObserver) view.getPresenter());
			}
		}
	}


	@Override
	public void update(final BerkeleyDb db, final long sumatraTimestampNs)
	{
		BerkeleyCamDetectionFrame camFrame = db.get(BerkeleyCamDetectionFrame.class, sumatraTimestampNs);
		if (camFrame != null)
		{
			for (IWorldFrameObserver vp : wFrameObservers)
			{
				camFrame.getCamFrames().values().forEach(vp::onNewCamDetectionFrame);
			}
		}
	}
}
