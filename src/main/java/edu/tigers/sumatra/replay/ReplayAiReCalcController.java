/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.replay;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.presenter.replay.IReplayController;
import edu.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ReplayAiReCalcController implements IReplayController
{
	private static final String RECALCULATED = "Recalculated";
	private static final ShapeMapSource SKILL_SHAPE_MAP_SOURCE = ShapeMapSource.of("Skills",
			ShapeMapSource.of(RECALCULATED));

	private final List<IWorldFrameObserver> wFrameObservers = new ArrayList<>();
	private final Map<EAiTeam, Ai> ais = new EnumMap<>(EAiTeam.class);
	private final Set<EAiTeam> aisToBeStopped = new HashSet<>();
	private final Map<EAiTeam, ShapeMapSource> shapeMapSources = new EnumMap<>(EAiTeam.class);
	private GenericSkillSystem skillSystem = GenericSkillSystem.forAnalysis();
	private long lastTimestamp = 0;
	private boolean runSkills = false;


	public ReplayAiReCalcController(List<ASumatraView> sumatraViews)
	{
		for (ASumatraView view : sumatraViews)
		{
			view.getPresenters()
					.filter(IWorldFrameObserver.class::isInstance)
					.map(IWorldFrameObserver.class::cast)
					.forEach(wFrameObservers::add);
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				ReplayControlPresenter replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
				replayControlPresenter.getViewPanel().addMenuCheckbox(new RunAiAction());
				replayControlPresenter.getViewPanel().addMenuCheckbox(new RunSkillsAction());
			}
		}
		Arrays.stream(EAiTeam.values()).forEach(team -> shapeMapSources.put(
				team,
				ShapeMapSource.of(team.getTeamColor().name(),
						ShapeMapSource.of("AI", ShapeMapSource.of(RECALCULATED)))
		));

		var botParamsMgr = SumatraModel.getInstance().getModule(BotParamsManager.class);
		BotID.getAll().forEach(id -> {
			DummyBot bot = new DummyBot(id);
			bot.setBotParams(botParamsMgr.get(bot.getBotParamLabel()));
			skillSystem.onBotAdded(bot);
		});
	}


	@Override
	public void update(final BerkeleyDb db, final WorldFrameWrapper wfw)
	{
		if (lastTimestamp > wfw.getSimpleWorldFrame().getTimestamp())
		{
			resetAis();
		}
		lastTimestamp = wfw.getSimpleWorldFrame().getTimestamp();

		aisToBeStopped.forEach(this::stopAi);
		aisToBeStopped.clear();

		for (Ai ai : ais.values())
		{
			AIInfoFrame aiFrame = ai.processWorldFrame(wfw);
			if (aiFrame != null)
			{
				final boolean inverted = wfw.getWorldFrame(ai.getAiTeam()).isInverted();
				aiFrame.getShapeMap().setInverted(inverted);
				wFrameObservers.forEach(
						o -> o.onNewShapeMap(lastTimestamp, aiFrame.getShapeMap(), shapeMapSources.get(ai.getAiTeam())));

				if (runSkills)
				{
					Map<BotID, ShapeMap> skillShapeMap = skillSystem.process(wfw, ai.getAiTeam().getTeamColor());
					skillShapeMap.values().forEach(m -> m.setInverted(inverted));

					wFrameObservers.forEach(o -> skillShapeMap.forEach((id, shapeMap) -> o.onNewShapeMap(
							lastTimestamp,
							shapeMap,
							ShapeMapSource.of(id.toString(), SKILL_SHAPE_MAP_SOURCE)
					)));
				}
			}
		}
	}


	private void startAi(EAiTeam aiTeam)
	{
		Ai newAi = new Ai(aiTeam, skillSystem);
		Ai preAi = ais.put(aiTeam, newAi);
		if (preAi != null)
		{
			preAi.stop();
		}
		newAi.start();
	}


	private void stopAi(EAiTeam aiTeam)
	{
		Ai preAi = ais.remove(aiTeam);
		if (preAi != null)
		{
			preAi.stop();
			wFrameObservers.forEach(o -> o.onRemoveSourceFromShapeMap(shapeMapSources.get(aiTeam)));
			BotID.getAll(aiTeam.getTeamColor()).forEach(id ->
					wFrameObservers.forEach(
							o -> o.onRemoveSourceFromShapeMap(ShapeMapSource.of(id.toString(), SKILL_SHAPE_MAP_SOURCE)))
			);
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
					aisToBeStopped.add(eAiTeam);
				}
			}
		}
	}

	private class RunSkillsAction extends AbstractAction
	{
		private RunSkillsAction()
		{
			super("Run Skills");
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			runSkills = chk.isSelected();
		}
	}
}
