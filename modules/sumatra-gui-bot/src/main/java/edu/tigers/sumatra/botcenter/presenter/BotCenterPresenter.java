/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.presenter;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botcenter.presenter.config.SavedConfigPresenter;
import edu.tigers.sumatra.botcenter.view.BotCenterPanel;
import edu.tigers.sumatra.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.botcenter.view.config.BotConfigOverviewPanel.IBotConfigOverviewPanelObserver;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
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
	private final BotManagerObserver botManagerObserver = new BotManagerObserver();
	private final List<BaseStationPresenter> baseStationPresenters = new ArrayList<>();
	private final BotCenterOverviewPresenter botCenterOverviewPresenter = new BotCenterOverviewPresenter(
			viewPanel.getOverviewPanel()
	);

	private SavedConfigPresenter savedConfigPresenter;
	private TigerBotPresenter tigerBotPresenter;
	private TigersBotManager tigersBotManager;

	@Override
	public void onStopModuli()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}

		if (tigerBotPresenter != null)
		{
			onBotIdSelected(BotID.noBot());
			tigersBotManager.getBaseStation().removeTigersBsObserver(tigerBotPresenter);
			tigerBotPresenter.dispose();
			tigerBotPresenter = null;
		}

		viewPanel.getBotOverviewPanel().removeObserver(this);
		viewPanel.clearPanel();
		ABotManager bm = SumatraModel.getInstance().getModule(ABotManager.class);
		bm.removeObserver(botManagerObserver);
		for (BaseStationPresenter bsp : baseStationPresenters)
		{
			bsp.onModuliStateChanged(ModulesState.RESOLVED);
		}
		baseStationPresenters.clear();
		for (ABot bot : bm.getBots().values())
		{
			botManagerObserver.onBotRemoved(bot);
		}
		viewPanel.getOverviewPanel().setActive(false);
		viewPanel.getBotOverviewPanel().getConfigPanel().removeConfigObserver(savedConfigPresenter);
		viewPanel.getBotOverviewPanel().getCmbBots().removeAllItems();
		viewPanel.getBotOverviewPanel().getCmbBots().addItem(BotID.noBot());
	}


	@Override
	public void onStartModuli()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}
		viewPanel.createPanel();
		tigersBotManager = SumatraModel.getInstance().getModule(TigersBotManager.class);
		savedConfigPresenter = new SavedConfigPresenter(viewPanel.getSavedConfigsPanel(),
				tigersBotManager.getConfigDatabase());

		TigersBaseStation baseStation = tigersBotManager.getBaseStation();
		BaseStationPresenter bsp = new BaseStationPresenter(baseStation);
		bsp.onModuliStateChanged(ModulesState.ACTIVE);
		baseStationPresenters.add(bsp);
		viewPanel.addBaseStationTab(bsp.getBsPanel());

		for (ABot bot : tigersBotManager.getBots().values())
		{
			botManagerObserver.onBotAdded(bot);
		}
		tigersBotManager.addObserver(botManagerObserver);

		viewPanel.getOverviewPanel().setActive(true);
		viewPanel.getBotOverviewPanel().addObserver(this);
		viewPanel.getBotOverviewPanel().getConfigPanel().addConfigObserver(savedConfigPresenter);

		tigersBotManager.getBaseStation().addTigersBsObserver(botCenterOverviewPresenter);

		tigerBotPresenter = new TigerBotPresenter(viewPanel.getBotOverviewPanel(), tigersBotManager.getConfigDatabase());
		tigersBotManager.getBaseStation().addTigersBsObserver(tigerBotPresenter);
	}


	private class BotManagerObserver implements IBotManagerObserver
	{

		@Override
		public void onBotAdded(final ABot bot)
		{
			JComboBox<BotID> cmbBots = viewPanel.getBotOverviewPanel().getCmbBots();
			cmbBots.addItem(bot.getBotId());
			TigerBotSummaryPanel summ = new TigerBotSummaryPanel();
			summ.setId(bot.getBotId());
			summ.setBotName(BotNames.get(bot.getBotId().getNumber()));
			if (bot.getType() != EBotType.TIGERS)
			{
				SwingUtilities.invokeLater(() -> summ.setRobotMode(ERobotMode.READY));
			}
			viewPanel.getOverviewPanel().addBotPanel(bot.getBotId(), summ);
		}


		@Override
		public void onBotRemoved(final ABot bot)
		{
			JComboBox<BotID> cmbBots = viewPanel.getBotOverviewPanel().getCmbBots();
			if (bot.getBotId().equals(cmbBots.getSelectedItem()))
			{
				cmbBots.setSelectedItem(BotID.noBot());
			}
			cmbBots.removeItem(bot.getBotId());
			viewPanel.getOverviewPanel().removeBotPanel(bot.getBotId());
		}
	}


	@Override
	public void onBotIdSelected(final BotID botId)
	{
		tigerBotPresenter.setBot(tigersBotManager.getTigerBot(botId).orElse(null));
	}
}
