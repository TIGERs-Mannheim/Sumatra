/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.presenter;

import edu.tigers.sumatra.persistence.PersistenceDb;
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
	public void update(final PersistenceDb db, final long sumatraTimestampNs)
	{
		WorldFrameWrapper wfw = db.getTable(WorldFrameWrapper.class).get(sumatraTimestampNs);
		if (wfw != null)
		{
			callback.accept(wfw);
			wFrameObservers.forEach(o -> o.onNewWorldFrame(wfw));
		}
	}
}
