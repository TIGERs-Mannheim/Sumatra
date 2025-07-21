/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.data.MatchBroadcast;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsProvider;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.botparams.NoBotParamsProvider;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.observer.EventSubscriber;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.util.DefaultRobotInfoProvider;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 */
@Log4j2
public class BotManager extends AModule
		implements IBotProvider, BotParamsManager.IBotParamsManagerObserver
{
	private Map<BotID, ABot> connectedBots = new ConcurrentHashMap<>();

	private final EventDistributor<ABot> onBotOnline = new EventDistributor<>();
	private final EventDistributor<BotID> onBotOffline = new EventDistributor<>();

	private BotParamsProvider botParamsProvider = new NoBotParamsProvider();
	private final Map<String, Set<BotID>> allocatedBotsMap = new ConcurrentSkipListMap<>();

	protected final MatchBroadcast matchBroadcast = new MatchBroadcast();


	@Override
	public void startModule()
	{
		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(b -> {
			b.addObserver(this);
			botParamsProvider = b;
		});

		IRobotInfoProvider robotInfoProvider = new RobotInfoProvider(this, botParamsProvider);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).setRobotInfoProvider(robotInfoProvider);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).setRobotInfoProvider(new DefaultRobotInfoProvider());
		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(b -> b.removeObserver(this));
		botParamsProvider = new NoBotParamsProvider();
	}


	@Override
	public Map<BotID, ? extends ABot> getBots()
	{
		return connectedBots;
	}


	@Override
	public Optional<? extends ABot> getBot(final BotID botID)
	{
		return Optional.ofNullable(getBots().get(botID));
	}


	public void addAllocatedBots(final String identifier, final Collection<BotID> botIds)
	{
		allocatedBotsMap.computeIfAbsent(identifier, k -> new HashSet<>()).addAll(botIds);
	}


	public void removeAllocatedBots(final String identifier, final Collection<BotID> botIds)
	{
		allocatedBotsMap.computeIfAbsent(identifier, k -> new HashSet<>()).removeAll(botIds);
	}


	/**
	 * Get bot IDs the base station tries to connect to.
	 *
	 * @return allocated bot IDs
	 */
	public Set<BotID> getAllocatedBots()
	{
		Set<BotID> ids = new HashSet<>();
		allocatedBotsMap.values().forEach(ids::addAll);
		return ids;
	}


	protected void addBot(final ABot bot)
	{
		bot.setBotParams(botParamsProvider.get(bot.getBotParamLabel()));

		log.debug("Bot is online: {}", bot);
		ABot previousBot = connectedBots.put(bot.getBotId(), bot);
		if (previousBot != null)
		{
			log.warn("Bot {} already connected. Replacing with new instance.", bot.getBotId());
		}

		onBotOnline.newEvent(bot);
	}


	protected void removeBot(final BotID botId)
	{
		log.debug("Bot is offline: {}", botId);
		ABot bot = connectedBots.remove(botId);
		if (bot == null)
		{
			log.warn("Bot {} not connected. Cannot remove.", botId);
			return;
		}

		onBotOffline.newEvent(botId);
	}


	@Override
	public void onBotParamsUpdated(final EBotParamLabel label, final IBotParams params)
	{
		getBots().values().stream()
				.filter(b -> b.getBotParamLabel() == label)
				.forEach(bot -> bot.setBotParams(params));
	}


	public EventSubscriber<ABot> getOnBotOnline()
	{
		return onBotOnline;
	}


	public EventSubscriber<BotID> getOnBotOffline()
	{
		return onBotOffline;
	}
}
