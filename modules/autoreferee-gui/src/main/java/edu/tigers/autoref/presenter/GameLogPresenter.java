/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoref.view.gamelog.GameLogPanel;
import edu.tigers.autoreferee.IAutoRefObserver;
import edu.tigers.autoreferee.engine.log.AutoRefGameEventGameLogEntry;
import edu.tigers.autoreferee.engine.log.ELogEntryType;
import edu.tigers.autoreferee.engine.log.GameStateGameLogEntry;
import edu.tigers.autoreferee.engine.log.GameTime;
import edu.tigers.autoreferee.engine.log.RefereeCommandGameLogEntry;
import edu.tigers.autoreferee.engine.log.RefereeGameEventGameLogEntry;
import edu.tigers.autoreferee.engine.log.RefereeGameEventProposalGameLogEntry;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.GameEventProposalGroup;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class GameLogPresenter
		implements ISumatraViewPresenter, IAutoRefObserver, EnumCheckBoxPanel.IEnumPanelObserver<ELogEntryType>,
		           IWorldFrameObserver
{
	private static final String ACTIVE_LOG_TYPES_KEY = GameLogPresenter.class + ".activeLogTypes";

	private final GameLogTableModel gameLogTableModel = new GameLogTableModel();
	@Getter
	private GameLogPanel viewPanel = new GameLogPanel(gameLogTableModel);
	private WorldFrameWrapper lastWorldFrameWrapper;


	public GameLogPresenter()
	{
		EnumCheckBoxPanel<ELogEntryType> logPanel = viewPanel.getLogTypePanel();

		Set<ELogEntryType> types = new HashSet<>();
		String activeLogTypes = SumatraModel.getInstance().getUserProperty(ACTIVE_LOG_TYPES_KEY);
		if (activeLogTypes == null)
		{
			types.add(ELogEntryType.DETECTED_GAME_EVENT);
		} else
		{
			Arrays.stream(activeLogTypes.split(","))
					.filter(logType -> Arrays.stream(ELogEntryType.values()).map(ELogEntryType::name)
							.anyMatch(name -> name.equals(logType)))
					.map(ELogEntryType::valueOf)
					.forEach(types::add);
		}
		logPanel.setSelectedBoxes(types);
		viewPanel.setActiveLogTypes(types);

		logPanel.addObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfw)
	{
		gameLogTableModel.removeTooRecentEntries(wfw.getTimestamp());
		if (lastWorldFrameWrapper != null)
		{
			checkForNewGameEventFromReferee(wfw);
			checkForNewGameEventProposalsFromReferee(wfw);
			checkForNewRefereeCommand(wfw);
			checkForNewGameState(wfw);
		}
		lastWorldFrameWrapper = wfw;
	}


	private void checkForNewRefereeCommand(final WorldFrameWrapper wfw)
	{
		if (wfw.getRefereeMsg().getCommand() != lastWorldFrameWrapper.getRefereeMsg().getCommand())
		{
			gameLogTableModel.add(new RefereeCommandGameLogEntry(
					wfw.getTimestamp(),
					GameTime.of(wfw.getRefereeMsg()),
					wfw.getRefereeMsg().getCommand()
			));
		}
	}


	private void checkForNewGameState(final WorldFrameWrapper wfw)
	{
		if (!lastWorldFrameWrapper.getGameState().equals(wfw.getGameState()))
		{
			gameLogTableModel.add(new GameStateGameLogEntry(
					wfw.getTimestamp(),
					GameTime.of(wfw.getRefereeMsg()),
					wfw.getGameState()
			));
		}
	}


	private void checkForNewGameEventFromReferee(final WorldFrameWrapper wfw)
	{
		final ArrayList<IGameEvent> newGameEvents = new ArrayList<>(
				wfw.getRefereeMsg().getGameEvents());
		newGameEvents.removeAll(lastWorldFrameWrapper.getRefereeMsg().getGameEvents());
		for (IGameEvent gameEvent : newGameEvents)
		{
			gameLogTableModel.add(new RefereeGameEventGameLogEntry(
					wfw.getTimestamp(),
					GameTime.of(wfw.getRefereeMsg()),
					gameEvent
			));
		}
	}


	private void checkForNewGameEventProposalsFromReferee(final WorldFrameWrapper wfw)
	{
		List<IGameEvent> proposals = wfw.getRefereeMsg().getGameEventProposalGroups().stream()
				.map(GameEventProposalGroup::getGameEvents)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		List<IGameEvent> lastProposals = lastWorldFrameWrapper.getRefereeMsg().getGameEventProposalGroups().stream()
				.map(GameEventProposalGroup::getGameEvents)
				.flatMap(Collection::stream)
				.toList();
		proposals.removeAll(lastProposals);
		for (IGameEvent gameEvent : proposals)
		{
			gameLogTableModel.add(new RefereeGameEventProposalGameLogEntry(
					wfw.getTimestamp(),
					GameTime.of(wfw.getRefereeMsg()),
					gameEvent
			));
		}
	}


	@Override
	public void onNewGameEventDetected(final IGameEvent gameEvent)
	{
		if (lastWorldFrameWrapper == null)
		{
			return;
		}
		SwingUtilities.invokeLater(() -> gameLogTableModel.add(new AutoRefGameEventGameLogEntry(
				lastWorldFrameWrapper.getTimestamp(),
				GameTime.of(lastWorldFrameWrapper.getRefereeMsg()),
				gameEvent
		)));
	}


	@Override
	public void onModuliStarted()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(m -> m.addObserver(this));
	}


	@Override
	public void onModuliStopped()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(m -> m.removeObserver(this));
		gameLogTableModel.onClear();
	}


	@Override
	public void onValueTicked(final ELogEntryType type, final boolean value)
	{
		Set<ELogEntryType> activeTypes = viewPanel.getLogTypePanel().getValues();
		viewPanel.setActiveLogTypes(activeTypes);
		String propValue = StringUtils.join(activeTypes, ",");
		SumatraModel.getInstance().setUserProperty(ACTIVE_LOG_TYPES_KEY, propValue);
	}
}
