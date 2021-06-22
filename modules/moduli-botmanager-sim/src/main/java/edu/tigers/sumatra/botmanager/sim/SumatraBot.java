/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillInput;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillOutput;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;

import java.util.Optional;


/**
 * Bot for internal Sumatra simulation
 */
public class SumatraBot extends ASimBot
{
	private boolean barrierInterrupted;
	private SimBotState botState;


	/**
	 * @param id
	 * @param bs
	 */
	public SumatraBot(final BotID id, final IBaseStation bs)
	{
		super(EBotType.SUMATRA, id, bs);
	}


	@Override
	public Optional<BotState> getSensoryState()
	{
		return Optional.ofNullable(botState)
				.map(s ->
						BotState.of(
								getBotId(),
								State.of(
										s.getPose(),
										Vector3.from2d(
												s.getVel().getXYVector().multiplyNew(1e-3),
												s.getVel().z()
										)
								)
						)
				);
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

		this.botState = botState;
		barrierInterrupted = botState.isBarrierInterrupted();
		lastFeedback = botState.getLastFeedback();

		return botSkillOutput.getAction();
	}
}
