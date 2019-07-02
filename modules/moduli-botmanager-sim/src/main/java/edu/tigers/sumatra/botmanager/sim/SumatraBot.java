/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim;

import java.util.Optional;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillInput;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillOutput;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Bot for internal Sumatra simulation
 */
public class SumatraBot extends ASimBot
{
	private boolean barrierInterrupted;
	
	
	/**
	 * @param id
	 * @param bs
	 */
	public SumatraBot(final BotID id, final SumatraBaseStation bs)
	{
		super(EBotType.SUMATRA, id, bs);
	}
	

	
	@Override
	public Optional<BotState> getSensoryState(final long timestamp)
	{
		return Optional.empty();
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return barrierInterrupted;
	}
	
	
	public SimBotAction simulate(final SimBotState botState, final MatchCommand matchCommand, final long timestamp)
	{
		BotSkillInput input = new BotSkillInput(matchCommand.getSkill(), botState, timestamp,
				matchCommand.isStrictVelocityLimit());
		
		final BotSkillOutput botSkillOutput = botSkillSim.execute(input);
		
		barrierInterrupted = botState.isBarrierInterrupted();
		
		return botSkillOutput.getAction();
	}
}
