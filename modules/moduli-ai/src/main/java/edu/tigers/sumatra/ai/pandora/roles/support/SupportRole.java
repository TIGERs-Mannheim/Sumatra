/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.BreakthroughDefensive;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Highly "coachable" supporter role, trigger different support behavior with different states
 */
public class SupportRole extends ARole
{
	private static Logger logger = Logger.getLogger(SupportRole.class);
	
	private EnumMap<ESupportBehavior, ASupportBehavior> behaviors;
	private ESupportBehavior currentBehavior;
	private Map<SupportRole, EnumMap<ESupportBehavior, Double>> viabilityMap;
	
	
	/**
	 * Constructor. What else?
	 */
	public SupportRole()
	{
		super(ERole.SUPPORT);
		initBehaviors();
		initStateMachine();
	}
	
	
	private void initBehaviors()
	{
		behaviors = new EnumMap<>(ESupportBehavior.class);
		for (ESupportBehavior b : ESupportBehavior.values())
		{
			try
			{
				behaviors.put(b, (ASupportBehavior) b.getInstanceableClass().newInstance(this));
			} catch (InstanceableClass.NotCreateableException e)
			{
				logger.error("Could not create behavior", e);
			}
		}
	}
	
	
	private void initStateMachine()
	{
		setInitialState(behaviors.get(ESupportBehavior.MOVE_VORONOI));
		for (ESupportBehavior b : ESupportBehavior.values())
		{
			addTransition(b, behaviors.get(b));
		}
	}
	
	
	@Override
	public void beforeUpdate()
	{
		ESupportBehavior selectedBehavior = selectBehavior();
		if (currentBehavior == null || currentBehavior != selectedBehavior)
		{
			currentBehavior = selectedBehavior;
			triggerEvent(selectedBehavior);
		}
	}
	
	
	private ESupportBehavior selectBehavior()
	{
		
		ESupportBehavior selectedBehaviour = ESupportBehavior.TEST;
		EnumMap<ESupportBehavior, Double> viability = viabilityMap.get(this);
		for (Map.Entry<ESupportBehavior, Double> entry : viability.entrySet())
		{
			if (entry.getValue() > 0 && isConformWithSpecialBehaviourSelectionRules(entry.getKey(), entry.getValue()))
			{
				selectedBehaviour = entry.getKey();
				break;
				
			}
		}
		return selectedBehaviour;
	}
	
	
	private boolean isConformWithSpecialBehaviourSelectionRules(ESupportBehavior behaviour, double viability)
	{
		boolean isConformWithSpecialRules = true;
		if (behaviour == ESupportBehavior.BREAKTHROUGH_DEFENSIVE)
		{
			isConformWithSpecialRules = isConformWithBreakthroughDefense(viability);
		}
		return isConformWithSpecialRules;
	}
	
	
	private boolean isConformWithBreakthroughDefense(double viability)
	{
		List<Double> interceptorViability = getOtherPenaltyAreaInterceptorViability(viabilityMap).stream()
				.sorted(Comparator.reverseOrder())
				.limit(BreakthroughDefensive.getMaxNumberAtPenaltyArea())
				.collect(Collectors.toList());
		return !interceptorViability.isEmpty()
				&& interceptorViability.get(interceptorViability.size() - 1) < viability;
	}
	
	
	private List<Double> getOtherPenaltyAreaInterceptorViability(
			Map<SupportRole, EnumMap<ESupportBehavior, Double>> viabilityMap)
	{
		List<Double> interceptorViability = new ArrayList<>();
		for (Map.Entry<SupportRole, EnumMap<ESupportBehavior, Double>> entry : viabilityMap.entrySet())
		{
			if (entry.getKey() != this)
			{
				interceptorViability.add(entry.getValue().get(ESupportBehavior.BREAKTHROUGH_DEFENSIVE));
			}
		}
		return interceptorViability;
	}
	
	
	/**
	 * This Method is called by the play before role update.
	 * It is necessary because the role update need the viability of the other supporters in some cases
	 */
	public void exchangeViability(Map<SupportRole, EnumMap<ESupportBehavior, Double>> viabilityMap)
	{
		this.viabilityMap = viabilityMap;
		EnumMap<ESupportBehavior, Double> viability = new EnumMap<>(ESupportBehavior.class);
		behaviors.forEach((e, b) -> viability.put(e, b.calculateViability()));
		viabilityMap.put(this, viability);
	}
	
	
	public List<ITrackedBot> getCurrentSupportBots()
	{
		return getAiFrame().getPlayStrategy().getActiveRoles(EPlay.SUPPORT).stream()
				.map(ARole::getBot)
				.collect(Collectors.toList());
	}
	
	
}
