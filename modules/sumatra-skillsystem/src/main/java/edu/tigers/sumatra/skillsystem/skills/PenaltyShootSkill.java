/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillPenaltyShooter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;


/**
 * Use the penalty shooter bot skill to perform a penalty shoot
 */
public class PenaltyShootSkill extends AMoveSkill
{
	
	@Configurable(comment = "Distance for slow motion", defValue = "600")
	private static double		slowMoveDist					= 600;
	
	@Configurable(comment = "Angle for penalty shoot", defValue = "0.31415926")
	private static double		targetAngle						= AngleMath.deg2rad(18.0);
	
	@Configurable(comment = "Speed for initial approach to ball", defValue = "0.05")
	private static double		approachSpeed					= 0.05;
	
	@Configurable(comment = "Angular velocity for quick turn", defValue = "30")
	private static double		rotationSpeed					= 30;
	
	@Configurable(comment = "Translations speed for circular movement about ball in turn", defValue = "0.1,0.1")
	private static IVector2 speedInTurn = Vector2.fromXY(0.1, 0.1);
	
	@Configurable(comment = "Kickspeed for penalty shoot", defValue = "8.0")
	private static double		penaltyKickSpeed				= 8.0;
	
	@Configurable(defValue = "3.0")
	private static double		accMax							= 3.0;
	
	@Configurable(defValue = "150.0")
	private static double		accMaxW							= 150.0;
	
	@Configurable(defValue = "300.0")
	private static double		jerkMax							= 300.0;
	
	@Configurable(defValue = "500.0")
	private static double		jerkMaxW						= 500.0;

	@Configurable(comment = "Dribblespeed for better ball handling", defValue = "1500.0")
	private static double		dribbleSpeed					= 1500.0;
	
	private ERotateDirection	rotateDirection;
	

	/**
	 * create a penalty shooter skill
	 * 
	 * @param rotateDirection
	 */
	public PenaltyShootSkill(ERotateDirection rotateDirection)
	{
		super(ESkill.PENALTY_SHOOT);
		this.rotateDirection = rotateDirection;
		IState prepositioningState = new PrepositioningState();
		IState botSkillActiveState = new PenaltyShooterBotSkillActiveState();
		setInitialState(prepositioningState);
		addTransition(EPenaltyShootSkillEvent.PREPOSITION_REACHED, botSkillActiveState);
		addTransition(botSkillActiveState, EPenaltyShootSkillEvent.KICK_FINISHED, IDLE_STATE);
	}
	
	private enum EPenaltyShootSkillEvent implements IEvent
	{
		PREPOSITION_REACHED,
		KICK_FINISHED
	}
	
	/**
	 * Rotation direction for quick turn
	 */
	public enum ERotateDirection
	{
		CW(-1),
		CCW(1);
		
		double factor;
		
		
		ERotateDirection(double factor)
		{
			this.factor = factor;
		}
		
		
		public double getFactor()
		{
			return factor;
		}
	}
	
	
	public void setShootDirection(ERotateDirection rotateDirection)
	{
		this.rotateDirection = rotateDirection;
	}
	
	private class PrepositioningState implements IState
	{
		private IVector2	targetPosition;
		private double		targetOrientation;
		
		
		@Override
		public void doEntryActions()
		{
			targetPosition = getBall().getPos().subtractNew(Vector2.fromXY(slowMoveDist, 9));
			targetOrientation = getBall().getPos().subtractNew(targetPosition).getAngle();
			setTargetPose(targetPosition, targetOrientation);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getPos().distanceTo(targetPosition) < 10.0 &&
					AngleMath.difference(targetOrientation, getAngle()) < AngleMath.deg2rad(5))
			{
				triggerEvent(EPenaltyShootSkillEvent.PREPOSITION_REACHED);
			}
		}
	}
	
	private class PenaltyShooterBotSkillActiveState implements IState
	{
		private ABotSkill botSkill;
		
		
		@Override
		public void doEntryActions()
		{
			Random random = new Random(getWorldFrame().getTimestamp());
			double timeToShoot = 2 * random.nextDouble() + 0.3;
			botSkill = BotSkillPenaltyShooter.Builder.create()
					.approachSpeed(approachSpeed)
					.penaltyKickSpeed(penaltyKickSpeed)
					.rotationSpeed(rotationSpeed * rotateDirection.getFactor())
					.speedInTurn(speedInTurn)
					.targetAngle(targetAngle)
                    .accMax(accMax)
                    .accMax(accMaxW)
                    .jerkMax(jerkMax)
                    .jerkMaxW(jerkMaxW)
                    .dribbleSpeed(dribbleSpeed)
					.timeToShoot(timeToShoot).build();
		}
		
		
		@Override
		public void doUpdate()
		{
			getMatchCtrl().setSkill(botSkill);
		}
	}
}
