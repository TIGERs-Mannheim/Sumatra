/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class ReplayWfwController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers;
	private final Consumer<WorldFrameWrapper> callback;


	@Override
	public void update(final BerkeleyDb db, final long sumatraTimestampNs)
	{
		WorldFrameWrapper wfw = db.get(WorldFrameWrapper.class, sumatraTimestampNs);
		if (wfw != null)
		{
			callback.accept(wfw);
			wFrameObservers.forEach(o -> o.onNewWorldFrame(wfw));
		}
	}
}
