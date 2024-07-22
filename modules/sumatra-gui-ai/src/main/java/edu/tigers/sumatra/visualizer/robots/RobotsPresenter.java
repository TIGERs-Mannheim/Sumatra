/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.robots;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;

import java.util.Map;


@Getter
public class RobotsPresenter implements ISumatraPresenter, IBotManagerObserver, IWorldFrameObserver
{
	private final RobotScrollPanel robotScrollPanel = new RobotScrollPanel();

	private BotID selectedRobotId = BotID.noBot();


	public RobotsPresenter()
	{
		getRobotsPanel().onRobotClicked(this::onRobotClick);
	}


	private RobotsPanel getRobotsPanel()
	{
		return robotScrollPanel.getRobotsPanel();
	}


	@Override
	public void onStartModuli()
	{
		ISumatraPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);

		SumatraModel.getInstance().getModuleOpt(ABotManager.class).ifPresent(
				botManager -> {
					botManager.addObserver(this);
					botManager.getBots().values().forEach(this::onBotAdded);
				}
		);
	}


	@Override
	public void onStopModuli()
	{
		ISumatraPresenter.super.onStopModuli();
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);

		SumatraModel.getInstance().getModuleOpt(ABotManager.class).ifPresent(
				botManager -> botManager.removeObserver(this)
		);
		getRobotsPanel().getBotStati().clear();
		getRobotsPanel().clearView();
	}


	private void onRobotClick(final BotID botId)
	{
		if (selectedRobotId.equals(botId))
		{
			selectedRobotId = BotID.noBot();
			getRobotsPanel().deselectRobots();
		} else
		{
			selectedRobotId = botId;
			getRobotsPanel().selectRobot(botId);
		}
	}


	private void updateRobotsPanel(WorldFrame wFrame)
	{
		Map<BotID, ITrackedBot> tigerBots = wFrame.getTigerBotsVisible();

		for (ITrackedBot tBot : tigerBots.values())
		{
			BotStatus status = getRobotsPanel().getBotStatus(tBot.getBotId());
			RobotInfo robotInfo = tBot.getRobotInfo();
			status.setConnected(robotInfo.isConnected());
			status.setVisible(tBot.getFilteredState().isPresent());
			status.setBatRel(robotInfo.getBatteryRelative());
			status.setKickerRel(robotInfo.getKickerLevelRelative());
			status.setBotFeatures(robotInfo.getBotFeatures());
			status.setRobotMode(robotInfo.getRobotMode());
		}

		getRobotsPanel().getBotStati().entrySet()
				.removeIf(e -> e.getKey().getTeamColor() == wFrame.getTeamColor() && !tigerBots.containsKey(e.getKey()));
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		updateRobotsPanel(wfWrapper.getWorldFrame(EAiTeam.BLUE));
		updateRobotsPanel(wfWrapper.getWorldFrame(EAiTeam.YELLOW));
		getRobotsPanel().updateBotStati();
	}


	@Override
	public void onClearWorldFrame()
	{
		getRobotsPanel().clearView();
	}
}
