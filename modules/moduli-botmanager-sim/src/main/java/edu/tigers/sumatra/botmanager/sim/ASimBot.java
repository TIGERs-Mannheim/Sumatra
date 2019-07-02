/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Abstract class for simulated robots
 */
public abstract class ASimBot extends ABot
{
	protected BotSkillSimulator botSkillSim = new BotSkillSimulator();
	
	
	protected ASimBot(final EBotType botType, final BotID botId, final IBaseStation baseStation)
	{
		super(botType, botId, baseStation);
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
	public double getDribblerTemp(){
		return 0.0;
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
	
	
	@Override
	public boolean isOK()
	{
		return true;
	}
	
	
	@Override
	public EBotParamLabel getBotParamLabel()
	{
		return getBotId().getTeamColor() == ETeamColor.YELLOW
				? EBotParamLabel.SIMULATION_YELLOW
				: EBotParamLabel.SIMULATION_BLUE;
	}
}
