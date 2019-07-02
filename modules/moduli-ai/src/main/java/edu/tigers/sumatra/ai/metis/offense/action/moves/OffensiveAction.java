/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.finisher.IFinisherMove;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;

import java.util.Optional;


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
	
	private IRatedPassTarget ratedPassTarget = null;
	private boolean allowRedirect = false;
	
	private transient IFinisherMove finisherMove;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private OffensiveAction()
	{
		move = null;
		viability = 0;
		action = null;
		kickTarget = null;
		finisherMove = null;
	}
	
	
	OffensiveAction(final EOffensiveActionMove move, final double viability, final EOffensiveAction action,
			final KickTarget kickTarget)
	{
		this.move = move;
		this.viability = viability;
		this.action = action;
		this.kickTarget = kickTarget;
	}
	
	
	OffensiveAction(final EOffensiveActionMove move, final double viability, final EOffensiveAction action,
			final KickTarget kickTarget, final IFinisherMove finisherMove)
	{
		this.move = move;
		this.viability = viability;
		this.action = action;
		this.kickTarget = kickTarget;
		this.finisherMove = finisherMove;
	}
	
	
	public OffensiveAction(final EOffensiveActionMove move, final EOffensiveAction action, final KickTarget kickTarget)
	{
		this.move = move;
		this.viability = 0;
		this.action = action;
		this.kickTarget = kickTarget;
	}
	
	
	OffensiveAction withPassTarget(final IRatedPassTarget passTarget)
	{
		this.ratedPassTarget = passTarget;
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
	
	
	public Optional<IRatedPassTarget> getRatedPassTarget()
	{
		return Optional.ofNullable(ratedPassTarget);
	}
	
	
	public boolean isAllowRedirect()
	{
		return allowRedirect;
	}
	
	
	public IFinisherMove getFinisherMove()
	{
		return finisherMove;
	}
	
}
