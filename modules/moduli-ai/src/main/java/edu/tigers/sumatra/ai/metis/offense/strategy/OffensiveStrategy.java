/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ids.BotID;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


/**
 * A offensive strategy holds information of the global strategy
 */
@Persistent(version = 3)
public class OffensiveStrategy
{
	private final Set<BotID> desiredBots = new LinkedHashSet<>();
	private BotID attackerBot = null;
	
	private final Map<BotID, EOffensiveStrategy> currentOffensivePlayConfiguration = new HashMap<>();
	
	private IPassTarget activePassTarget;
	
	private boolean attackerIsAllowedToKick = false;
	
	
	void addDesiredBot(final BotID botID)
	{
		Objects.requireNonNull(botID);
		assert botID.isBot();
		assert !desiredBots.contains(botID);
		desiredBots.add(botID);
	}
	
	
	void setAttackerBot(final BotID botID)
	{
		Objects.requireNonNull(botID);
		assert botID.isBot();
		attackerBot = botID;
		addDesiredBot(botID);
	}
	
	
	void clearDesiredBots()
	{
		desiredBots.clear();
		attackerBot = null;
	}
	
	
	void putPlayConfiguration(final BotID botID, EOffensiveStrategy strategy)
	{
		currentOffensivePlayConfiguration.put(botID, strategy);
	}
	
	
	void clearPlayConfiguration()
	{
		currentOffensivePlayConfiguration.clear();
	}
	
	
	void setActivePassTarget(final IPassTarget activePassTarget)
	{
		this.activePassTarget = activePassTarget;
	}
	
	
	void setAttackerIsAllowedToKick(final boolean isRoleReadyToKick)
	{
		this.attackerIsAllowedToKick = isRoleReadyToKick;
	}
	
	
	public Set<BotID> getDesiredBots()
	{
		return Collections.unmodifiableSet(desiredBots);
	}
	
	
	public Optional<BotID> getAttackerBot()
	{
		return Optional.ofNullable(attackerBot);
	}
	
	
	public Map<BotID, EOffensiveStrategy> getCurrentOffensivePlayConfiguration()
	{
		return Collections.unmodifiableMap(currentOffensivePlayConfiguration);
	}
	
	
	public Optional<IPassTarget> getActivePassTarget()
	{
		return Optional.ofNullable(activePassTarget);
	}
	
	
	public boolean isAttackerIsAllowedToKick()
	{
		return attackerIsAllowedToKick;
	}
}