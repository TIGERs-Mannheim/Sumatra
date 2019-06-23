/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

import edu.tigers.autoref.presenter.GameLogPresenter;
import edu.tigers.autoreferee.AutoRefFramePreprocessor;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayAutoRefReCalcController implements IReplayController
{
	private final AutoRefFramePreprocessor refPreprocessor = new AutoRefFramePreprocessor();
	private final PassiveAutoRefEngine autoRefEngine = new PassiveAutoRefEngine();
	
	private final List<IWorldFrameObserver> wFrameObservers = new ArrayList<>();
	private final List<IAutoRefStateObserver> refObservers = new CopyOnWriteArrayList<>();
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
			if (view.getType() == ESumatraViewType.AUTOREFEREE_GAME_LOG)
			{
				GameLogPresenter gameLogPresenter = (GameLogPresenter) view.getPresenter();
				gameLogPresenter.setGameLog(autoRefEngine.getGameLog());
			}
			if (view.getPresenter() instanceof IAutoRefStateObserver)
			{
				IAutoRefStateObserver observer = (IAutoRefStateObserver) view.getPresenter();
				refObservers.add(observer);
			}
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
		
		if (lastAutoRefFrame == null || lastAutoRefFrame.getTimestamp() > wfw.getSimpleWorldFrame().getTimestamp())
		{
			reset();
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
		
		for (IAutoRefStateObserver observer : refObservers)
		{
			observer.onNewAutoRefFrame(refFrame);
		}
		for (IWorldFrameObserver o : wFrameObservers)
		{
			o.onNewShapeMap(refFrame.getTimestamp(), refFrame.getShapes(), "AUTO_REF_UPDATED");
		}
		lastAutoRefFrame = refFrame;
	}
	
	
	private void reset()
	{
		refPreprocessor.clear();
		autoRefEngine.reset();
		autoRefEngine.getGameLog().clearEntries();
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
			reset();
			
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			active = chk.isSelected();
			
			if (!active)
			{
				for (IWorldFrameObserver o : wFrameObservers)
				{
					o.onClearShapeMap("AUTO_REF_UPDATED");
				}
			}
		}
	}
}
