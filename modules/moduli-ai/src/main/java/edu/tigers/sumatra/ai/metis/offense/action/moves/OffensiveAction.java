/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;


/**
 * The offensive action gives instructions to the attacker
 */
@Persistent(version = 3)
public class OffensiveAction
{
	private final EOffensiveActionMove move;
	private final double viability;
	private final EOffensiveAction action;
	private final KickTarget kickTarget;
	
	private IPassTarget passTarget = null;
	private boolean allowRedirect = false;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private OffensiveAction()
	{
		move = null;
		viability = 0;
		action = null;
		kickTarget = null;
	}
	
	
	OffensiveAction(final EOffensiveActionMove move, final double viability, final EOffensiveAction action,
			final KickTarget kickTarget)
	{
		this.move = move;
		this.viability = viability;
		this.action = action;
		this.kickTarget = kickTarget;
	}
	
	
	OffensiveAction withPassTarget(final IPassTarget passTarget)
	{
		this.passTarget = passTarget;
		return this;
	}
	
	
	OffensiveAction withAllowRedirect(final boolean allowRedirect)
	{
		this.allowRedirect = allowRedirect;
		return this;
	}
	
	
	public EOffensiveActionMove getMove()
	{
		return move;
	}
	
	
	/**
	 * @return viability score [0,1]. 1 is good
	 */
	public double getViability()
	{
		return viability;
	}
	
	
	public EOffensiveAction getAction()
	{
		return action;
	}
	
	
	public KickTarget getKickTarget()
	{
		return kickTarget;
	}
	
	
	public Optional<IPassTarget> getPassTarget()
	{
		return Optional.ofNullable(passTarget);
	}
	
	
	public boolean isAllowRedirect()
	{
		return allowRedirect;
	}
}
