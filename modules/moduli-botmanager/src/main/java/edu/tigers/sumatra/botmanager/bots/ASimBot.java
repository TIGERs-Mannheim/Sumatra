/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.botskills.sim.BotSkillSimulator;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ASimBot extends ABot implements IConfigObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger
			.getLogger(ASimBot.class.getName());
	
	private static final String	CONFIG_CATEGORY	= "botmgr";
	static
	{
		ConfigRegistration.registerClass(CONFIG_CATEGORY, ASimBot.class);
	}
	
	protected transient BotSkillSimulator botSkillSim = new BotSkillSimulator();

	
	
	protected ASimBot(final EBotType botType, final BotID botId, final IBaseStation baseStation)
	{
		super(botType, botId, baseStation);
		afterApply(null);
	}
	
	
	protected ASimBot(final ABot aBot, final EBotType botType)
	{
		super(aBot, botType);
	}
	
	
	protected ASimBot()
	{
		super();
	}
	
	
	@Override
	public void onIncommingBotCommand(final ACommand cmd)
	{
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		ConfigRegistration.applySpezis(this, CONFIG_CATEGORY, getType().name());
	}
	
	
	@Override
	public void start()
	{
		ConfigRegistration.registerConfigurableCallback(CONFIG_CATEGORY, this);
	}
	
	
	@Override
	public void stop()
	{
		ConfigRegistration.unregisterConfigurableCallback(CONFIG_CATEGORY, this);
	}
	
	
	@Override
	public int getHardwareId()
	{
		return getBotId().getNumberWithColorOffset();
	}
	
	
	@Override
	public double getKickerLevel()
	{
		return getKickerLevelMax();
	}
	
	
	@Override
	public double getBatteryRelative()
	{
		return 1;
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return false;
	}
	
	
	@Override
	public ERobotMode getRobotMode()
	{
		return ERobotMode.READY;
	}
}
