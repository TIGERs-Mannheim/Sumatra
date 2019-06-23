/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;

import java.util.Optional;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleFreeSkirmishState extends AOffensiveRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //

	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * @param role
	 */
	public OffensiveRoleFreeSkirmishState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	/**
	 * @return The current move Position of the offensiveRole
	 */
	@Override
	public IVector2 getMoveDest()
	{
		return getPos();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.FREE_SKIRMISH.name();
	}
	
	
	@Override
	public void doExitActions()
	{
		// not needed
	}
	
	
	@Override
	public void doEntryActions()
	{
		IVector2 target = getAiFrame().getTacticalField().getSkirmishInformation().getSupportiveCircleCatchPos();
		IVector2 ballToTarget = target.subtractNew(getWFrame().getBall().getPos());
		IVector2 meToBall = getWFrame().getBall().getPos().subtractNew(getPos());
		Optional<Double> angle = meToBall.angleTo(ballToTarget);
		RotationSkill skill;
		if (!angle.isPresent())
		{
			skill = new RotationSkill(AngleMath.deg2rad(170));
			setNewSkill(skill);
			return;
		}
		if (angle.get() > 0)
		{
			// right
			skill = new RotationSkill(AngleMath.deg2rad(170));
		} else
		{
			// left
			skill = new RotationSkill(AngleMath.deg2rad(-170));
		}
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		// not needed
	}
	
	
}
