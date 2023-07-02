/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
public class ReplayShapeMapController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers;
	private Set<ShapeMapSource> lastSources = Set.of();


	@Override
	public void update(final BerkeleyDb db, final long sumatraTimestampNs)
	{
		BerkeleyShapeMapFrame shapeMapFrame = db.get(BerkeleyShapeMapFrame.class, sumatraTimestampNs);
		if (shapeMapFrame != null)
		{
			var currentSources = shapeMapFrame.getShapeMaps().keySet();
			var obsoleteSources = new HashSet<>(lastSources);
			obsoleteSources.removeAll(currentSources);
			wFrameObservers.forEach(o -> {
				obsoleteSources.forEach(o::onRemoveSourceFromShapeMap);
				shapeMapFrame.getShapeMaps().forEach((source, map) ->
						o.onNewShapeMap(shapeMapFrame.getTimestamp(), map, source)
				);
			});
			lastSources = currentSources;
		}
	}
}
