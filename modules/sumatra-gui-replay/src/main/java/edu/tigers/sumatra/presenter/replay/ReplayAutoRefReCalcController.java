/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

import edu.tigers.autoreferee.AutoRefFramePreprocessor;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayAutoRefReCalcController implements IReplayController
{
	private final AutoRefFramePreprocessor refPreprocessor = new AutoRefFramePreprocessor();
	private final PassiveAutoRefEngine autoRefEngine = new PassiveAutoRefEngine(
			EnumSet.allOf(EGameEventDetectorType.class));

	private final List<IWorldFrameObserver> wFrameObservers = new ArrayList<>();
	private IAutoRefFrame lastAutoRefFrame = null;

	private boolean active = false;


	/**
	 * Default
	 */
	public ReplayAutoRefReCalcController(final List<ASumatraView> sumatraViews)
	{
		super();

		for (ASumatraView view : sumatraViews)
		{
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				ReplayControlPresenter replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
				replayControlPresenter.getReplayPanel().addMenuCheckbox(new RunAutoRefAction());
			}
			if (view.getPresenter() instanceof IWorldFrameObserver)
			{
				wFrameObservers.add((IWorldFrameObserver) view.getPresenter());
			}
		}
	}


	@Override
	public void update(final BerkeleyDb db, final WorldFrameWrapper wfw)
	{
		if (!active)
		{
			return;
		}

		IAutoRefFrame refFrame;
		/*
		 * We only run the ref engine if the current frame is not equal to the last frame. Otherwise we
		 * simply repaint the last frame.
		 */
		if ((lastAutoRefFrame != null) && (lastAutoRefFrame.getTimestamp() == wfw.getSimpleWorldFrame().getTimestamp()))
		{
			refFrame = lastAutoRefFrame;
		} else
		{
			boolean hasLastFrame = refPreprocessor.hasLastFrame();
			refFrame = refPreprocessor.process(wfw);

			if (hasLastFrame)
			{
				autoRefEngine.process(refFrame);
			}
		}

		for (IWorldFrameObserver o : wFrameObservers)
		{
			o.onNewShapeMap(refFrame.getTimestamp(), refFrame.getShapes(),
					ShapeMapSource.of("Recalculated AutoRef", "Recalculated"));
		}
		lastAutoRefFrame = refFrame;
	}


	private class RunAutoRefAction extends AbstractAction
	{
		private RunAutoRefAction()
		{
			super("Run AutoRef");
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			active = chk.isSelected();

			if (!active)
			{
				for (IWorldFrameObserver o : wFrameObservers)
				{
					o.onRemoveSourceFromShapeMap("Recalculated AutoRef");
				}
			}
		}
	}
}
