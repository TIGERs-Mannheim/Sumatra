/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.botcenter.presenter;

import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.gui.botcenter.view.BotCenterOverviewPanel;
import edu.tigers.sumatra.gui.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.util.UiThrottler;

import javax.swing.SwingUtilities;

public class BotCenterOverviewPresenter
{
	private final BotCenterOverviewPanel botCenterOverviewPanel;
	private final UiThrottler matchFeedbackThrottler = new UiThrottler(1000);


	public BotCenterOverviewPresenter(final BotCenterOverviewPanel botCenterOverviewPanel)
	{
		this.botCenterOverviewPanel = botCenterOverviewPanel;
		matchFeedbackThrottler.start();
	}


	public void onIncomingBotCommand(BotCommand botCommand)
	{
		if (botCommand.command().getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) botCommand.command();
			final TigerBotSummaryPanel botPanel = botCenterOverviewPanel.getBotPanel(botCommand.botId());
			if (botPanel != null)
			{
				matchFeedbackThrottler.execute(() -> SwingUtilities.invokeLater(() -> botPanel.setMatchFeedback(feedback)));
			}
		}
	}
}
