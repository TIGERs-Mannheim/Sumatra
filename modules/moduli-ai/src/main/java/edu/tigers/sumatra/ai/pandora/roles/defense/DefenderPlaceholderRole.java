/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * A placeholder role for new defenders
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefenderPlaceholderRole extends ADefenseRole
{
	private IVector2 target = null;
	
	
	/**
	 * Default instance
	 */
	public DefenderPlaceholderRole()
	{
		super(ERole.DEFENDER_PLACEHOLDER);
		setInitialState(new MoveState());
	}
	
	
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	public void setTarget(final IVector2 target)
	{
		this.target = target;
	}
	
	
	@Override
	public ILineSegment getProtectionLine(final ILineSegment threatLine)
	{
		return threatLine;
	}
	
	private class MoveState extends AState
	{
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
			skill.getMoveCon().setIgnoreGameStateObstacles(getAiFrame().getGamestate().isStop());
			skill.getMoveCon().setBallObstacle(false);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (target != null)
			{
				skill.getMoveCon().updateDestination(target);
				double targetAngle = target.subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
				skill.getMoveCon().updateTargetAngle(targetAngle);
			}
		}
	}
}
