/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayWfwController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers = new CopyOnWriteArrayList<>();
	
	
	public ReplayWfwController(List<ASumatraView> sumatraViews)
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
		WorldFrameWrapper wfw = db.get(WorldFrameWrapper.class, sumatraTimestampNs);
		if (wfw != null)
		{
			for (IWorldFrameObserver vp : wFrameObservers)
			{
				vp.onNewWorldFrame(wfw);
			}
		}
	}
	
	
	/**
	 * @param observer
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
	public void addWFrameObserver(final IWorldFrameObserver observer)
	{
		wFrameObservers.add(observer);
	}
}
