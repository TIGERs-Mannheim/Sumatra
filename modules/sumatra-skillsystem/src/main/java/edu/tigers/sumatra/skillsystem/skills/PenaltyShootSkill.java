/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.BotSkillPenaltyShooter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;

import java.util.Random;


/**
 * Use the penalty shooter bot skill to perform a penalty shot
 */
public class PenaltyShootSkill extends AMoveSkill
{
	@Configurable(comment = "Distance for slow motion [mm]", defValue = "200.0")
	private static double slowMoveDist = 200.0;

	@Configurable(comment = "Angle for penalty shoot [deg]", defValue = "14.0")
	private static double targetAngle = 14.0;

	@Configurable(comment = "Speed for initial approach to ball [m/s]", defValue = "0.15")
	private static double approachSpeed = 0.15;

	@Configurable(comment = "Angular velocity for quick turn [rad/s]", defValue = "30")
	private static double rotationSpeed = 30;

	@Configurable(comment = "Translational speed for circular movement about ball in turn [m/s]", defValue = "0.2,-0.2")
	private static IVector2 speedInTurn = Vector2.fromXY(0.2, -0.2);

	@Configurable(comment = "Kickspeed for penalty shoot [m/s]", defValue = "6.0")
	private static double penaltyKickSpeed = 6.0;

	@Configurable(defValue = "3.0")
	private static double accMax = 3.0;

	@Configurable(defValue = "100.0")
	private static double accMaxW = 100.0;

	@Configurable(defValue = "100.0")
	private static double jerkMax = 100.0;

	@Configurable(defValue = "500.0")
	private static double jerkMaxW = 500.0;

	@Configurable(comment = "Dribblespeed for better ball handling [RPM]", defValue = "8000.0")
	private static double dribbleSpeed = 8000.0;

	private ERotateDirection rotateDirection = ERotateDirection.CW;


	public PenaltyShootSkill()
	{
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


		ERotateDirection(final double factor)
		{
			this.factor = factor;
		}


		public double getFactor()
		{
			return factor;
		}
	}


	public void setShootDirection(final ERotateDirection rotateDirection)
	{
		this.rotateDirection = rotateDirection;
	}


	private class PrepositioningState extends AState
	{
		private IVector2 targetPosition;
		private double targetOrientation;


		@Override
		public void doEntryActions()
		{
			targetPosition = getBall().getPos().subtractNew(Vector2.fromXY(slowMoveDist, 30));
			targetOrientation = getBall().getPos().subtractNew(targetPosition).getAngle();
			setTargetPose(targetPosition, targetOrientation, defaultMoveConstraints());
		}


		@Override
		public void doUpdate()
		{
			if ((getPos().distanceTo(targetPosition) < 10.0) &&
					(AngleMath.difference(targetOrientation, getAngle()) < AngleMath.deg2rad(5)))
			{
				triggerEvent(EPenaltyShootSkillEvent.PREPOSITION_REACHED);
			}
		}
	}

	private class PenaltyShooterBotSkillActiveState extends AState
	{
		@Override
		public void doEntryActions()
		{
			Random random = new Random(getWorldFrame().getTimestamp());
			double timeToShoot = (2 * random.nextDouble()) + 0.5;
			var botSkill = BotSkillPenaltyShooter.Builder.create()
					.approachSpeed(approachSpeed)
					.penaltyKickSpeed(penaltyKickSpeed)
					.rotationSpeed(rotationSpeed * rotateDirection.getFactor())
					.speedInTurn(Vector2.fromXY(speedInTurn.x() * rotateDirection.getFactor(), speedInTurn.y()))
					.targetAngle(AngleMath.deg2rad(targetAngle) * rotateDirection.getFactor())
					.accMax(accMax)
					.accMaxW(accMaxW)
					.jerkMax(jerkMax)
					.jerkMaxW(jerkMaxW)
					.dribbleSpeed(dribbleSpeed)
					.timeToShoot(timeToShoot).build();
			getMatchCtrl().setSkill(botSkill);
		}
	}
}
