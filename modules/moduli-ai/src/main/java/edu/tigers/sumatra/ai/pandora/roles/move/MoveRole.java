/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * This is a generic move role.
 * The only thing it does is to move according to moveCon.
 * So it considers your updateDestination and updateLookAtTarget.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveRole extends ARole
{
	private final AMoveToSkill skill;
	
	
	/**
	 * Create a simple move role.
	 */
	public MoveRole()
	{
		super(ERole.MOVE);
		setInitialState(new MoveState());
		skill = AMoveToSkill.createMoveToSkill();
		setNewSkill(skill);
	}
	
	
	/**
	 * @param dest moving destination
	 * @param orientation target angle
	 */
	public MoveRole(final IVector2 dest, final double orientation)
	{
		this();
		skill.getMoveCon().updateDestination(dest);
		skill.getMoveCon().updateTargetAngle(orientation);
	}
	
	
	/**
	 * @return the moveCon of the underlying skill
	 */
	public final MovementCon getMoveCon()
	{
		return skill.getMoveCon();
	}


	/**
	 * @return of destination is reached
	 */
	public final boolean isDestinationReached() {
		return getMoveCon().getDestination() == null
				|| VectorMath.distancePP(getPos(), getMoveCon().getDestination()) < 70;
	}
	
	private class MoveState extends AState
	{
		
		// No code here; just a dummy state
		
	}
}
