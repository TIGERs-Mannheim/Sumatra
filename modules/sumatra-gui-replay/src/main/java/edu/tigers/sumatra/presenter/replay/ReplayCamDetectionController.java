/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ReplayCamDetectionController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers;


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
