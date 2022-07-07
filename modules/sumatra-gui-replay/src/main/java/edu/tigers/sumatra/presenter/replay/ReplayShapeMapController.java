/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ReplayShapeMapController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers;


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
