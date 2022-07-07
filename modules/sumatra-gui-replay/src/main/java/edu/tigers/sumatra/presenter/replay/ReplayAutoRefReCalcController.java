/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

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

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ActionEvent;
import java.util.EnumSet;
import java.util.List;


public class ReplayAutoRefReCalcController implements IReplayController
{
	private static final ShapeMapSource SHAPE_MAP_SOURCE = ShapeMapSource.of("AutoRef",
			ShapeMapSource.of("Recalculated"));
	private final AutoRefFramePreprocessor refPreprocessor = new AutoRefFramePreprocessor();
	private final PassiveAutoRefEngine autoRefEngine = new PassiveAutoRefEngine(
			EnumSet.allOf(EGameEventDetectorType.class));

	private final List<IWorldFrameObserver> wFrameObservers;
	private IAutoRefFrame lastAutoRefFrame = null;

	private boolean active = false;


	public ReplayAutoRefReCalcController(List<IWorldFrameObserver> wFrameObservers, List<ASumatraView> sumatraViews)
	{
		this.wFrameObservers = wFrameObservers;
		for (ASumatraView view : sumatraViews)
		{
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				ReplayControlPresenter replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
				replayControlPresenter.getViewPanel().addMenuCheckbox(new RunAutoRefAction());
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
			o.onNewShapeMap(refFrame.getTimestamp(), refFrame.getShapes(), SHAPE_MAP_SOURCE);
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
				wFrameObservers.forEach(o -> o.onRemoveSourceFromShapeMap(SHAPE_MAP_SOURCE));
			}
		}
	}
}
