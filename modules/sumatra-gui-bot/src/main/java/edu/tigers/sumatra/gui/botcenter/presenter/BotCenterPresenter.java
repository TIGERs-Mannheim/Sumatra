/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.botcenter.presenter;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.gui.botcenter.presenter.config.SavedConfigPresenter;
import edu.tigers.sumatra.gui.botcenter.view.BotCenterPanel;
import edu.tigers.sumatra.gui.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.gui.botcenter.view.config.BotConfigOverviewPanel.IBotConfigOverviewPanelObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;


/**
 * The bot center presenter
 */
public class BotCenterPresenter implements ISumatraViewPresenter, IBotConfigOverviewPanelObserver
{
	@Getter
	private final BotCenterPanel viewPanel = new BotCenterPanel();
	private final List<BaseStationPresenter> baseStationPresenters = new ArrayList<>();
	private final BotCenterOverviewPresenter botCenterOverviewPresenter = new BotCenterOverviewPresenter(
			viewPanel.getOverviewPanel()
	);

	private SavedConfigPresenter savedConfigPresenter;
	private TigerBotPresenter tigerBotPresenter;
	private TigersBotManager tigersBotManager;


	@Override
	public void onModuliStarted()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}
		viewPanel.createPanel();
		tigersBotManager = SumatraModel.getInstance().getModule(TigersBotManager.class);
		savedConfigPresenter = new SavedConfigPresenter(
				viewPanel.getSavedConfigsPanel(),
				tigersBotManager.getConfigDatabase()
		);

		BaseStationPresenter bsp = new BaseStationPresenter(tigersBotManager);
		bsp.onModuliStarted();
		baseStationPresenters.add(bsp);
		SwingUtilities.invokeLater(() -> viewPanel.addBaseStationTab(bsp.getBsPanel()));

		for (ABot bot : tigersBotManager.getBots().values())
		{
			onBotAdded(bot);
		}
		tigersBotManager.getOnBotOnline().subscribe(getClass().getCanonicalName(), this::onBotAdded);
		tigersBotManager.getOnBotOffline().subscribe(getClass().getCanonicalName(), this::onBotRemoved);

		SwingUtilities.invokeLater(() -> viewPanel.getOverviewPanel().setActive(true));
		viewPanel.getBotOverviewPanel().addObserver(this);
		viewPanel.getBotOverviewPanel().getConfigPanel().addConfigObserver(savedConfigPresenter);

		tigerBotPresenter = new TigerBotPresenter(viewPanel.getBotOverviewPanel(), tigersBotManager.getConfigDatabase());
		tigersBotManager.getOnIncomingBotCommand()
				.subscribe(getClass().getCanonicalName(), this::onIncomingBotCommand);
	}


	@Override
	public void onModuliStopped()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}

		tigersBotManager.getOnIncomingBotCommand().unsubscribe(getClass().getCanonicalName());
		if (tigerBotPresenter != null)
		{
			onBotIdSelected(BotID.noBot());
			tigerBotPresenter.dispose();
			tigerBotPresenter = null;
		}

		tigersBotManager.getOnBotOnline().unsubscribe(getClass().getCanonicalName());
		tigersBotManager.getOnBotOffline().unsubscribe(getClass().getCanonicalName());

		viewPanel.getBotOverviewPanel().removeObserver(this);
		SwingUtilities.invokeLater(viewPanel::clearPanel);
		for (BaseStationPresenter bsp : baseStationPresenters)
		{
			bsp.onModuliStopped();
		}
		baseStationPresenters.clear();
		SwingUtilities.invokeLater(() -> viewPanel.getOverviewPanel().setActive(false));
		viewPanel.getBotOverviewPanel().getConfigPanel().removeConfigObserver(savedConfigPresenter);
		SwingUtilities.invokeLater(() -> {
			viewPanel.getBotOverviewPanel().getCmbBots().removeAllItems();
			viewPanel.getBotOverviewPanel().getCmbBots().addItem(BotID.noBot());
		});
	}


	private void onBotAdded(final ABot bot)
	{
		TigerBotSummaryPanel summ = new TigerBotSummaryPanel();
		summ.setId(bot.getBotId());
		if (bot.getType() != EBotType.TIGERS)
		{
			summ.setRobotMode(ERobotMode.READY);
		}
		SwingUtilities.invokeLater(() -> {
			JComboBox<BotID> cmbBots = viewPanel.getBotOverviewPanel().getCmbBots();
			cmbBots.addItem(bot.getBotId());
			viewPanel.getOverviewPanel().addBotPanel(bot.getBotId(), summ);
		});
	}


	private void onBotRemoved(BotID botId)
	{
		SwingUtilities.invokeLater(() -> {
			JComboBox<BotID> cmbBots = viewPanel.getBotOverviewPanel().getCmbBots();
			if (botId.equals(cmbBots.getSelectedItem()))
			{
				cmbBots.setSelectedItem(BotID.noBot());
			}
			cmbBots.removeItem(botId);
			viewPanel.getOverviewPanel().removeBotPanel(botId);
		});
	}


	@Override
	public void onBotIdSelected(final BotID botId)
	{
		tigerBotPresenter.setBot(tigersBotManager.getBot(botId).orElse(null));
	}


	private void onIncomingBotCommand(BotCommand botCommand)
	{
		tigerBotPresenter.onIncomingBotCommand(botCommand);
		botCenterOverviewPresenter.onIncomingBotCommand(botCommand);
	}
}
