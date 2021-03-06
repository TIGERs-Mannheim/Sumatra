/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;


public class ReplayShapeMapController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers = new CopyOnWriteArrayList<>();
	
	
	public ReplayShapeMapController(List<ASumatraView> sumatraViews)
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
		BerkeleyShapeMapFrame shapeMapFrame = db.get(BerkeleyShapeMapFrame.class, sumatraTimestampNs);
		if (shapeMapFrame != null)
		{
			shapeMapFrame.getShapeMaps().forEach((source, shapeMap) -> wFrameObservers
					.forEach(o -> o.onNewShapeMap(sumatraTimestampNs, shapeMap, source)));
		}
	}
}
