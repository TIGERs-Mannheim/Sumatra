/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim;

import edu.tigers.sumatra.botmanager.BotManager;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.ASumatraSimulator;
import edu.tigers.sumatra.sim.ISimulatorActionCallback;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.simulation.SimulationControlModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Base station implementation for local sumatra simulation
 */
public class SumatraSimBotManager extends BotManager implements ISimulatorActionCallback
{
	@Override
	public void startModule()
	{
		super.startModule();

		SumatraModel.getInstance().getModuleOpt(ASumatraSimulator.class)
				.ifPresent(s -> s.addSimulatorActionCallback(this));
		SumatraModel.getInstance().getModuleOpt(SimulationControlModule.class)
				.ifPresent(s -> s.addSimulatorActionCallback(this));
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModuleOpt(ASumatraSimulator.class)
				.ifPresent(s -> s.removeSimulatorActionCallback(this));
		SumatraModel.getInstance().getModuleOpt(SimulationControlModule.class)
				.ifPresent(s -> s.removeSimulatorActionCallback(this));

		super.stopModule();
	}


	@Override
	public Map<BotID, SumatraSimBot> getBots()
	{
		return super.getBots().values().stream()
				.map(SumatraSimBot.class::cast)
				.collect(Collectors.toMap(SumatraSimBot::getBotId, Function.identity()));
	}


	@Override
	public Optional<SumatraSimBot> getBot(BotID botID)
	{
		return super.getBot(botID).map(SumatraSimBot.class::cast);
	}


	@Override
	public void updateConnectedBotList(Set<BotID> botSet)
	{
		Set<BotID> newBots = new HashSet<>(botSet);
		newBots.removeAll(getBots().keySet());
		newBots.forEach(id -> addBot(new SumatraSimBot(id)));

		Set<BotID> removedBots = new HashSet<>(getBots().keySet());
		removedBots.removeAll(botSet);
		removedBots.forEach(this::removeBot);
	}


	@Override
	public Map<BotID, SimBotAction> nextSimBotActions(Map<BotID, SimBotState> botStates, final long timestamp)
	{
		Map<BotID, SimBotAction> map = new HashMap<>();
		for (var entry : getBots().entrySet())
		{
			var botID = entry.getKey();
			var bot = entry.getValue();
			var botState = botStates.get(botID);
			if (bot != null && botState != null)
			{
				SimBotAction simBotAction = bot.simulate(botState, matchBroadcast, timestamp);
				map.put(bot.getBotId(), simBotAction);
			}
		}
		return map;
	}
}
