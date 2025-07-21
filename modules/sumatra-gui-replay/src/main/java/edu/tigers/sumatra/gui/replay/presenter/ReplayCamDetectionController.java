/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.presenter;

import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.PersistenceCamDetectionFrame;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ReplayCamDetectionController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers;


	@Override
	public void update(final PersistenceDb db, final long sumatraTimestampNs)
	{
		PersistenceCamDetectionFrame camFrame = db.getTable(PersistenceCamDetectionFrame.class).get(sumatraTimestampNs);
		if (camFrame != null)
		{
			for (IWorldFrameObserver vp : wFrameObservers)
			{
				camFrame.getCamFrames().values().forEach(vp::onNewCamDetectionFrame);
			}
		}
	}
}
