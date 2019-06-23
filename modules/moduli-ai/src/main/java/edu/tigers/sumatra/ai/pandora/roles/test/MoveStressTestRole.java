/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.Random;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveStressTestRole extends ARole
{
	
	/**
	 * 
	 */
	public MoveStressTestRole()
	{
		super(ERole.MOVE_STRESS_TEST);
		setInitialState(new SmallDestChanges());
	}
	
	
	private class SmallDestChanges implements IState {
		private MovementCon	moveCon			= null;
		private IVector2		initBotPos		= null;
		private double			initBotAngle	= 0;
		private double			b					= 0;
		private final Random	rnd				= new Random();
		
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
			initBotPos = getPos();
			initBotAngle = getBot().getOrientation();
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 baseDest = initBotPos.addNew(Vector2.fromXY(1000, 0));
			
			b += (1.0 / 50.0) * AngleMath.PI_TWO;
			
			double a = Math.cos(b);
			
			IVector2 dest = baseDest.addNew(Vector2.fromXY((a * 10), (a * 10)));
			moveCon.updateDestination(dest);
			
			double baseAngle = AngleMath.normalizeAngle(initBotAngle + AngleMath.PI);
			double angle = baseAngle + (rnd.nextGaussian() * 0.15);
			moveCon.updateTargetAngle(angle);
		}
		
		
		@Override
		public void doExitActions()
		{
		}


	}
}
