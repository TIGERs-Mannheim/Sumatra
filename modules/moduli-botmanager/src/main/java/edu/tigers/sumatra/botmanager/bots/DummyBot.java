/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EDribblerState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.basestation.DummyBaseStation;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Dummy stub class for bots to prevent using null
 */
public class DummyBot extends ABot
{
	/**
	 * Default constructor.
	 */
	public DummyBot()
	{
		super(EBotType.UNKNOWN, BotID.noBot(), new DummyBaseStation());
	}
	
	
	/**
	 * @param botId
	 */
	public DummyBot(final BotID botId)
	{
		super(EBotType.UNKNOWN, botId, new DummyBaseStation());
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return false;
	}
	
	
	@Override
	public double getCenter2DribblerDist()
	{
		return 75;
	}
	
	
	@Override
	public int getHardwareId()
	{
		return getBotId().getNumberWithColorOffsetBS();
	}
	
	
	@Override
	public EDribblerState getDribblerState()
	{
		return EDribblerState.COLD;
	}
	
	
	@Override
	public double getKickerLevel()
	{
		return 0;
	}
	
	
	@Override
	public double getBatteryRelative()
	{
		return 0;
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
