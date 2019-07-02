/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.SupportPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Highly "coachable" supporter role, trigger different support behavior with different states.
 * The assignment of the behaviors happens in {@link SupportPlay}.
 */
public class SupportRole extends ARole
{
	private static Logger logger = Logger.getLogger(SupportRole.class);
	
	@Configurable(comment = "[m/s]", defValue = "2.0")
	private static double maxVelInReceiverArc = 2.;

	private IPenaltyArea area = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + 20);

	private final EnumMap<ESupportBehavior, ASupportBehavior> behaviors = new EnumMap<>(ESupportBehavior.class);
	private ESupportBehavior currentBehavior;
	
	
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
		checkRedirect();
	}
	
	
	public void selectBehavior(ESupportBehavior selectedBehavior)
	{
		
		if (currentBehavior == null || currentBehavior != selectedBehavior)
		{
			currentBehavior = selectedBehavior;
			triggerEvent(selectedBehavior);
		}
	}
	
	
	private void checkRedirect()
	{
		boolean isPassable = getAiFrame().getTacticalField().getOffensiveShadows().stream()
				.anyMatch(a -> a.isPointInShape(getPos()));
		
		if (isPassable)
		{
			getCurrentSkill().getMoveCon().getMoveConstraints().setVelMax(maxVelInReceiverArc);
		} else
		{
			getCurrentSkill().getMoveCon().getMoveConstraints()
					.setVelMax(getBot().getMoveConstraints().getVelMax());
		}
	}
	
	
	/**
	 * Calculates the viability of all behaviours. Called by the Play
	 *
	 * @return A map containing the calculated viabilities for the role
	 */
	public Map<ESupportBehavior, Double> calculateViabilities()
	{
		EnumMap<ESupportBehavior, Double> viabilities = new EnumMap<>(ESupportBehavior.class);
		behaviors.forEach((e, b) -> viabilities.put(e, b.calculateViability()));
		return viabilities;
	}
	
	
	public List<ITrackedBot> getCurrentSupportBots()
	{
		return getAiFrame().getPlayStrategy().getActiveRoles(EPlay.SUPPORT).stream()
				.map(ARole::getBot)
				.collect(Collectors.toList());
	}
	
	
	/**
	 * This should return the same results on any instance of a SupportRole
	 *
	 * @return A list of all inactive Behaviors
	 */
	public List<ESupportBehavior> getInactiveBehaviors()
	{
		List<ESupportBehavior> ret = new ArrayList<>();
		
		for (Map.Entry<ESupportBehavior, ASupportBehavior> behavior : this.behaviors.entrySet())
		{
			if (!behavior.getValue().getIsActive())
			{
				ret.add(behavior.getKey());
			}
		}
		
		return ret;
	}

	public ESupportBehavior getCurrentBehavior()
	{
		return currentBehavior;
	}
	
	
	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();
		
		// determine critical foe bots
		getCurrentSkill().getMoveCon().setCriticalFoeBots(
				getWFrame().getFoeBots().values().stream()
						.filter(b -> OffensiveMath.isBotCritical(b.getPos(), area))
						.map(ITrackedBot::getBotId)
						.collect(Collectors.toSet()));
	}
}
