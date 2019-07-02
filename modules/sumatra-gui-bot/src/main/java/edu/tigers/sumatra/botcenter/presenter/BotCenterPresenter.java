/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.presenter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botcenter.view.BotCenterPanel;
import edu.tigers.sumatra.botcenter.view.BotConfigOverviewPanel.IBotConfigOverviewPanelObserver;
import edu.tigers.sumatra.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * The bot center presenter
 */
public class BotCenterPresenter extends ASumatraViewPresenter implements IBotConfigOverviewPanelObserver
{
	private static final String[] BOT_NAMES = {
			"Gandalf", "Alice", "Tigger",
			"Poller", "Q", "Eichbaum",
			"This Bot", "Black Betty", "Trinity",
			"Neo", "Bob", "Yoda",
			"Unnamed 12", "Unnamed 13", "Unnamed 14", "Unnamed 15" };

	private final BotCenterPanel botCenter = new BotCenterPanel();
	private final BotManagerObserver botManagerObserver = new BotManagerObserver();
	private final List<BaseStationPresenter> baseStationPresenters = new ArrayList<>();

	private final TigerBotPresenter tigerBotPresenter;
	private final BotCenterOverviewPresenter botCenterOverviewPresenter;

	private TigersBotManager tigersBotManager;


	/**
	 * Constructor.
	 */
	public BotCenterPresenter()
	{
		tigerBotPresenter = new TigerBotPresenter(botCenter.getBotOverviewPanel());
		botCenterOverviewPresenter = new BotCenterOverviewPresenter(botCenter.getOverviewPanel());
	}


	@Override
	public void onStop()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}

		botCenter.getBotOverviewPanel().removeObserver(this);
		botCenter.clearPanel();
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
		botCenter.getOverviewPanel().setActive(false);
		botCenter.getBotOverviewPanel().getCmbBots().removeAllItems();
		botCenter.getBotOverviewPanel().getCmbBots().addItem(BotID.noBot());
		if (tigersBotManager != null)
		{
			tigersBotManager.getBaseStation().removeTigersBsObserver(tigerBotPresenter);
			tigersBotManager.getBaseStation().removeTigersBsObserver(botCenterOverviewPresenter);
		}
	}


	@Override
	public void onStart()
	{
		if (!SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return;
		}

		botCenter.createPanel();
		tigersBotManager = (TigersBotManager) SumatraModel.getInstance().getModule(ABotManager.class);

		TigersBaseStation baseStation = tigersBotManager.getBaseStation();
		BaseStationPresenter bsp = new BaseStationPresenter(baseStation);
		bsp.onModuliStateChanged(ModulesState.ACTIVE);
		baseStationPresenters.add(bsp);
		botCenter.addBaseStationTab(bsp.getBsPanel());

		for (ABot bot : tigersBotManager.getBots().values())
		{
			botManagerObserver.onBotAdded(bot);
		}
		tigersBotManager.addObserver(botManagerObserver);

		tigerBotPresenter.setBotManager(tigersBotManager);
		botCenter.getOverviewPanel().setActive(true);
		botCenter.getBotOverviewPanel().addObserver(this);
		tigersBotManager.getBaseStation().addTigersBsObserver(tigerBotPresenter);

		tigersBotManager.getBaseStation().addTigersBsObserver(botCenterOverviewPresenter);
	}


	@Override
	public Component getComponent()
	{
		return botCenter;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return botCenter;
	}


	private class BotManagerObserver implements IBotManagerObserver
	{

		@Override
		public void onBotAdded(final ABot bot)
		{
			JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
			cmbBots.addItem(bot.getBotId());
			TigerBotSummaryPanel summ = new TigerBotSummaryPanel();
			summ.setId(bot.getBotId());
			summ.setBotName(BOT_NAMES[bot.getBotId().getNumber()]);
			if (bot.getType() != EBotType.TIGERS)
			{
				SwingUtilities.invokeLater(() -> summ.setRobotMode(ERobotMode.READY));
			}
			botCenter.getOverviewPanel().addBotPanel(bot.getBotId(), summ);
		}


		@Override
		public void onBotRemoved(final ABot bot)
		{
			JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
			if (bot.getBotId().equals(cmbBots.getSelectedItem()))
			{
				cmbBots.setSelectedItem(BotID.noBot());
			}
			cmbBots.removeItem(bot.getBotId());
			botCenter.getOverviewPanel().removeBotPanel(bot.getBotId());
		}
	}


	@Override
	public void onBotIdSelected(final BotID botId)
	{
		Optional<TigerBot> bot = tigersBotManager.getTigerBot(botId);
		tigerBotPresenter.updateSelectedBotId(bot.orElse(new TigerBot(BotID.noBot(), null)));
	}

}
