/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.visualizer.presenter;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.gui.ai.visualizer.view.BotStatus;
import edu.tigers.sumatra.gui.ai.visualizer.view.RobotScrollPanel;
import edu.tigers.sumatra.gui.ai.visualizer.view.RobotsPanel;
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

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;


@Getter
public class RobotsPresenter implements ISumatraPresenter, IWorldFrameObserver
{
	private final RobotScrollPanel robotScrollPanel = new RobotScrollPanel();


	public RobotsPresenter()
	{
		getRobotsPanel().onRobotClicked(this::onRobotClick);
	}


	private RobotsPanel getRobotsPanel()
	{
		return robotScrollPanel.getRobotsPanel();
	}


	@Override
	public void onModuliStarted()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void onModuliStopped()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);

		SwingUtilities.invokeLater(() -> {
			getRobotsPanel().getBotStati().clear();
			getRobotsPanel().clearView();
		});
	}


	public List<BotID> getSelectedBots()
	{
		return getRobotsPanel().getSelectedBots();
	}


	private void onRobotClick(ActionEvent event, BotID botId)
	{
		if ((event.getModifiers() & ActionEvent.CTRL_MASK) != 0)
		{
			getRobotsPanel().selectRobot(botId);
		} else
		{
			for (BotID selectedBot : getRobotsPanel().getSelectedBots())
			{
				getRobotsPanel().deselectRobot(selectedBot);
			}
			getRobotsPanel().selectRobot(botId);
		}
		SwingUtilities.invokeLater(() -> getRobotsPanel().repaint());
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
		SwingUtilities.invokeLater(() -> getRobotsPanel().updateBotStati());
	}


	@Override
	public void onClearWorldFrame()
	{
		SwingUtilities.invokeLater(() -> getRobotsPanel().clearView());
	}
}
