/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.replay;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.presenter.replay.IReplayController;
import edu.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayAiReCalcController implements IReplayController
{
	private final List<IWorldFrameObserver> wFrameObservers = new ArrayList<>();
	private final List<IVisualizationFrameObserver> visFrameObservers = new ArrayList<>();
	private final Map<EAiTeam, Ai> ais = new EnumMap<>(EAiTeam.class);
	private ASkillSystem skillSystem = GenericSkillSystem.forSimulation();
	private long lastTimestamp = 0;
	
	
	public ReplayAiReCalcController(List<ASumatraView> sumatraViews)
	{
		for (ASumatraView view : sumatraViews)
		{
			if (view.getPresenter() instanceof IWorldFrameObserver)
			{
				wFrameObservers.add((IWorldFrameObserver) view.getPresenter());
			}
			if (view.getPresenter() instanceof IVisualizationFrameObserver)
			{
				visFrameObservers.add((IVisualizationFrameObserver) view.getPresenter());
			}
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				ReplayControlPresenter replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
				replayControlPresenter.getReplayPanel().addMenuCheckbox(new RunAiAction());
			}
		}
	}
	
	
	@Override
	public void update(final BerkeleyDb db, final WorldFrameWrapper wfw)
	{
		if (lastTimestamp > wfw.getSimpleWorldFrame().getTimestamp())
		{
			resetAis();
		}
		lastTimestamp = wfw.getSimpleWorldFrame().getTimestamp();
		
		for (Ai ai : ais.values())
		{
			AIInfoFrame aiFrame = ai.processWorldFrame(wfw);
			if (aiFrame != null)
			{
				ShapeMap skillShapeMap = new ShapeMap();
				skillSystem.process(wfw, skillShapeMap);
				aiFrame.getTacticalField().getDrawableShapes().setInverted(wfw.getWorldFrame(ai.getAiTeam()).isInverted());
				VisualizationFrame visFrame = new VisualizationFrame(aiFrame);
				for (IVisualizationFrameObserver o : visFrameObservers)
				{
					o.onRecalculatedVisualizationFrame(visFrame);
				}
				for (IWorldFrameObserver o : wFrameObservers)
				{
					o.onNewShapeMap(lastTimestamp, aiFrame.getTacticalField().getDrawableShapes(),
							visFrame.getTeamColor().name() + "_UPDATED");
					o.onNewShapeMap(lastTimestamp, skillShapeMap, "SKILLS_UPDATED");
				}
			}
		}
	}
	
	
	private void startAi(EAiTeam aiTeam)
	{
		Ai preAi = ais.put(aiTeam, new Ai(aiTeam, skillSystem));
		if (preAi != null)
		{
			preAi.stop();
		}
	}
	
	
	private void resetAis()
	{
		Set<EAiTeam> curAis = new HashSet<>(ais.keySet());
		for (EAiTeam eAiTeam : curAis)
		{
			startAi(eAiTeam);
		}
	}
	
	private class RunAiAction extends AbstractAction
	{
		private RunAiAction()
		{
			super("Run AI");
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			for (ETeamColor teamColor : ETeamColor.yellowBlueValues())
			{
				EAiTeam eAiTeam = EAiTeam.primary(teamColor);
				if (chk.isSelected())
				{
					startAi(eAiTeam);
				} else
				{
					stopAi(eAiTeam);
				}
				
				if (!chk.isSelected())
				{
					for (IWorldFrameObserver o : wFrameObservers)
					{
						o.onClearShapeMap(teamColor.name() + "_UPDATED");
					}
				}
			}
		}
		
		
		private void stopAi(EAiTeam aiTeam)
		{
			Ai preAi = ais.remove(aiTeam);
			if (preAi != null)
			{
				preAi.stop();
			}
		}
	}
}
