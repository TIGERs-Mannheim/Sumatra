package edu.tigers.sumatra.botcenter.presenter;

import edu.tigers.sumatra.botcenter.view.BotCenterOverviewPanel;
import edu.tigers.sumatra.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.botmanager.basestation.ITigersBaseStationObserver;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.util.UiThrottler;


public class BotCenterOverviewPresenter implements ITigersBaseStationObserver
{
	private final BotCenterOverviewPanel botCenterOverviewPanel;
	private final UiThrottler matchFeedbackThrottler = new UiThrottler(1000);


	public BotCenterOverviewPresenter(final BotCenterOverviewPanel botCenterOverviewPanel)
	{
		this.botCenterOverviewPanel = botCenterOverviewPanel;
		matchFeedbackThrottler.start();
	}


	@Override
	public void onIncomingBotCommand(final BotID id, final ACommand command)
	{
		if (command.getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) command;
			final TigerBotSummaryPanel botPanel = botCenterOverviewPanel.getBotPanel(id);
			if (botPanel != null)
			{
				matchFeedbackThrottler.execute(() -> botPanel.setMatchFeedback(feedback));
			}
		}
	}
}
