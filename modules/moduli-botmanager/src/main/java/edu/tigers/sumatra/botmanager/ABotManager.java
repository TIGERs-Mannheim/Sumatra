/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.basestation.ABaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.BotSkillFactory;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsProvider;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.botparams.NoBotParamsProvider;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.util.DefaultRobotInfoProvider;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 *
 * @author Gero
 */
public abstract class ABotManager extends AModule
		implements IBotProvider, IBaseStationObserver, BotParamsManager.IBotParamsManagerObserver
{
	private static final Logger log = Logger.getLogger(ABotManager.class.getName());

	protected final List<IBotManagerObserver> observers = new CopyOnWriteArrayList<>();
	private final Map<BotID, ABot> botTable = new ConcurrentSkipListMap<>(BotID.getComparator());

	protected ABaseStation baseStation = null;
	protected BotParamsManager botParamsManager;


	public IBaseStation getBaseStation()
	{
		return baseStation;
	}


	@Override
	public void initModule()
	{
		BotSkillFactory.getInstance().loadSkills();
	}


	@Override
	public void startModule()
	{
		IRobotInfoProvider robotInfoProvider = new RobotInfoProvider(this, getBotParamsProvider());
		SumatraModel.getInstance().getModule(AWorldPredictor.class).setRobotInfoProvider(robotInfoProvider);

		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(b -> b.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(b -> botParamsManager = b);

		baseStation = createBaseStation();
		baseStation.addObserver(this);
		baseStation.connect();
	}


	@Override
	public void stopModule()
	{
		baseStation.disconnect();
		baseStation.removeObserver(this);
		botTable.keySet().forEach(this::onBotOffline);

		SumatraModel.getInstance().getModule(AWorldPredictor.class)
				.setRobotInfoProvider(new DefaultRobotInfoProvider());

		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(b -> b.removeObserver(this));
		botParamsManager = null;
	}


	@Override
	public Map<BotID, ABot> getBots()
	{
		return Collections.unmodifiableMap(botTable);
	}


	@Override
	public Optional<ABot> getBot(final BotID botID)
	{
		return Optional.ofNullable(getBots().get(botID));
	}


	@Override
	public void onBotOffline(final BotID id)
	{
		log.debug("Bot is offline: " + id);
		ABot bot = botTable.remove(id);
		if (bot == null)
		{
			log.warn("Tried to remove a non-existing bot with id " + id);
		} else
		{
			observers.forEach(o -> o.onBotRemoved(bot));
		}
	}


	@Override
	public void onBotOnline(final ABot bot)
	{
		log.debug("Bot is online: " + bot);
		if (botTable.containsKey(bot.getBotId()))
		{
			log.warn("Bot came online, but we already have it: " + bot, new Exception());
		} else
		{
			botTable.put(bot.getBotId(), bot);
			observers.forEach(o -> o.onBotAdded(bot));
		}
	}


	@Override
	public void onBotParamsUpdated(final EBotParamLabel label, final IBotParams params)
	{
		getBots().values().stream()
				.filter(b -> b.getBotParamLabel() == label)
				.forEach(bot -> bot.setBotParams(params));
	}


	private BotParamsProvider getBotParamsProvider()
	{
		if (SumatraModel.getInstance().isModuleLoaded(BotParamsManager.class))
		{
			return SumatraModel.getInstance().getModule(BotParamsManager.class);
		}
		return new NoBotParamsProvider();
	}


	private ABaseStation createBaseStation()
	{
		String[] bsImplArr = getSubnodeConfiguration().getStringArray("basestation-impl");
		if (bsImplArr.length != 1)
		{
			throw new IllegalStateException(
					"Expected exactly one base station implementation, got: " + Arrays.toString(bsImplArr));
		}
		String impl = bsImplArr[0];
		try
		{
			Class<?> clazz = Class.forName(impl);
			Object bsObj = clazz.newInstance();
			return (ABaseStation) bsObj;
		} catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("Could not find base station class: " + impl, e);
		} catch (InstantiationException | IllegalAccessException e)
		{
			throw new IllegalStateException("Could not create base station: " + impl, e);
		} catch (ClassCastException e)
		{
			throw new IllegalStateException("Invalid base station class: " + impl, e);
		}
	}


	public void addObserver(final IBotManagerObserver o)
	{
		observers.add(o);
	}


	public void removeObserver(final IBotManagerObserver o)
	{
		observers.remove(o);
	}
}
