/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.presenter;

import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.PersistenceShapeMapFrame;
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
	public void update(final PersistenceDb db, final long sumatraTimestampNs)
	{
		PersistenceShapeMapFrame shapeMapFrame = db.getTable(PersistenceShapeMapFrame.class).get(sumatraTimestampNs);
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
