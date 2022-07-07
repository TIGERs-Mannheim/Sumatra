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
	@Getter
	private final RobotsPanel robotsPanel = new RobotsPanel();

	private BotID selectedRobotId = BotID.noBot();


	public RobotsPresenter()
	{
		robotsPanel.onRobotClicked(this::onRobotClick);
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
	}


	private void onRobotClick(final BotID botId)
	{
		if (selectedRobotId.equals(botId))
		{
			selectedRobotId = BotID.noBot();
			robotsPanel.deselectRobots();
		} else
		{
			selectedRobotId = botId;
			robotsPanel.selectRobot(botId);
		}
	}


	private void updateRobotsPanel(WorldFrame wFrame)
	{
		Map<BotID, ITrackedBot> tigerBots = wFrame.getTigerBotsVisible();

		for (ITrackedBot tBot : tigerBots.values())
		{
			BotStatus status = robotsPanel.getBotStatus(tBot.getBotId());
			RobotInfo robotInfo = tBot.getRobotInfo();
			status.setConnected(robotInfo.isConnected());
			status.setVisible(tBot.getFilteredState().isPresent());
			status.setBatRel(robotInfo.getBatteryRelative());
			status.setKickerRel(robotInfo.getKickerLevelRelative());
			status.setBotFeatures(robotInfo.getBotFeatures());
			status.setRobotMode(robotInfo.getRobotMode());
		}

		robotsPanel.getBotStati().entrySet().stream()
				.filter(e -> e.getKey().getTeamColor() == wFrame.getTeamColor())
				.filter(e -> !tigerBots.containsKey(e.getKey()))
				.map(Map.Entry::getValue)
				.forEach(botStatus -> {
					botStatus.setConnected(false);
					botStatus.setBatRel(0);
					botStatus.setKickerRel(0);
				});
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		updateRobotsPanel(wfWrapper.getWorldFrame(EAiTeam.BLUE));
		updateRobotsPanel(wfWrapper.getWorldFrame(EAiTeam.YELLOW));
		robotsPanel.updateBotStati();
	}


	@Override
	public void onClearWorldFrame()
	{
		robotsPanel.clearView();
	}
}
