/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * This skill uses a run up to archive longer chip distances
 */
public class RunUpChipSkill extends AMoveSkill
{
	@Configurable(comment = "Dribbler speed", defValue = "15000")
	private static int dribberSpeed = 15000;
	
	private final DynamicPosition target;
	private final double kickSpeed;
	
	private boolean dribblerOn = false;
	
	
	/**
	 * Default
	 * 
	 * @param target
	 * @param kickSpeed
	 */
	public RunUpChipSkill(final DynamicPosition target, final double kickSpeed)
	{
		super(ESkill.RUN_UP_CHIP);
		this.target = target;
		this.kickSpeed = kickSpeed;
		setInitialState(new PrepareState());
		addTransition(RunUpEvents.PREPARE, new PrepareState());
		addTransition(RunUpEvents.START_POS, new MoveToStartPos());
		addTransition(RunUpEvents.RUN_UP, new RunUpState());
		addTransition(RunUpEvents.KICK, new KickState());
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		if (dribblerOn)
		{
			kickerDribblerOutput.setDribblerSpeed(dribberSpeed);
		}
		kickerDribblerOutput.setKick(KickParams.limitKickSpeed(kickSpeed), EKickerDevice.CHIP, EKickerMode.ARM);
	}
	
	
	private IVector2 validateAndCorrectPosition(IVector2 destination)
	{
		IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPost = Geometry.getGoalOur().getRightPost();
		IVector2 goalDepthVector = Vector2.fromXY(-Geometry.getGoalOur().getDepth(), 0);
		
		ICircle goalPostLeftCircle = Circle.createCircle(leftPost, Geometry.getBotRadius());
		ICircle goalPostRightCircle = Circle.createCircle(rightPost, Geometry.getBotRadius());
		Rectangle goalBack = Rectangle.aroundLine(leftPost.addNew(goalDepthVector), rightPost.addNew(goalDepthVector),
				Geometry.getBotRadius());
		
		ILine runway = Lines.lineFromPoints(getBall().getPos(), destination);
		List<IVector2> interceptions = goalPostLeftCircle.lineIntersections(runway);
		interceptions.addAll(goalPostRightCircle.lineIntersections(runway));
		interceptions.addAll(goalBack.lineIntersections(runway.toLegacyLine()));
		interceptions.addAll(
				Geometry.getFieldWBorders().withMargin(-Geometry.getBotRadius()).lineIntersections(runway.toLegacyLine()));
		if (getMoveCon().isPenaltyAreaForbiddenOur())
		{
			interceptions.addAll(Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())
					.lineIntersections(runway.toLegacyLine()));
		}
		if (getMoveCon().isPenaltyAreaForbiddenTheir())
		{
			interceptions.addAll(Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius())
					.lineIntersections(runway.toLegacyLine()));
		}
		
		Optional<IVector2> nearestInterception = interceptions.stream()
				.min(Comparator.comparingDouble(b -> b.distanceTo(getBall().getPos())));
		if (nearestInterception.isPresent()
				&& nearestInterception.get().distanceTo(getBall().getPos()) < destination.distanceTo(getBall().getPos()))
		{
			getShapes().get(ESkillShapesLayer.KICK_SKILL)
					.add(new DrawableCircle(Circle.createCircle(nearestInterception.get(), 50), Color.BLUE));
			return nearestInterception.get();
		}
		
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableCircle(Circle.createCircle(destination, 50), Color.BLUE));
		return destination;
	}
	
	
	private void drawShapes()
	{
		List<IDrawableShape> shapes = getShapes().get(ESkillShapesLayer.KICK_SKILL);
		shapes.add(new DrawableCircle(Circle.createCircle(this.target.getPos(), 50), Color.RED));
	}
	
	private enum RunUpEvents implements IEvent
	{
		PREPARE,
		START_POS,
		RUN_UP,
		KICK,
	}
	
	private class PrepareState extends MoveToState
	{
		
		protected PrepareState()
		{
			super(RunUpChipSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(true);
			getMoveCon().setMinDistToBall(Geometry.getBotRadius() * 1.5);
			dribblerOn = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 passDirection = target.getPos().subtractNew(getWorldFrame().getBall().getPos());
			double safetyLength = Geometry.getBotRadius();
			if (getMoveCon().getMinDistToBall().isPresent())
			{
				safetyLength = getMoveCon().getMinDistToBall().get() + Geometry.getBotRadius();
			}
			IVector2 destination = LineMath.stepAlongLine(target.getPos(), getWorldFrame().getBall().getPos(),
					passDirection.getLength2() + safetyLength);
			destination = validateAndCorrectPosition(destination);
			getMoveCon().updateDestination(destination);
			getMoveCon().updateLookAtTarget(getWorldFrame().getBall());
			double distanceToDestination = destination.distanceTo(getTBot().getPos());
			
			double dAngle = Math.abs(getAngle() - getMoveCon().getTargetAngle());
			if (distanceToDestination < 10 && dAngle < 0.01)
			{
				triggerEvent(RunUpEvents.START_POS);
			}
			drawShapes();
			super.doUpdate();
		}
		
	}
	
	private class MoveToStartPos extends MoveToState
	{
		
		protected MoveToStartPos()
		{
			
			super(RunUpChipSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			dribblerOn = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			double additionalLength = getRunUpLength();
			double passLength = target.getPos().distanceTo(getBall().getPos());
			if (additionalLength <= 0.001)
			{
				triggerEvent(RunUpEvents.KICK);
				return;
			}
			IVector2 destination = LineMath.stepAlongLine(target.getPos(), getBall().getPos(),
					passLength + additionalLength);
			destination = validateAndCorrectPosition(destination);
			getMoveCon().updateDestination(destination);
			getMoveCon().updateTargetAngle(target.getPos().subtractNew(getBall().getPos()).getAngle());
			
			double distanceToDestination = destination.distanceTo(getTBot().getPos());
			double dAngle = Math.abs(getAngle() - getMoveCon().getTargetAngle());
			if (distanceToDestination < 10 && dAngle < 0.01 && getTBot().getVel().getLength2() < 0.1)
			{
				triggerEvent(RunUpEvents.RUN_UP);
			}
			
			drawShapes();
			super.doUpdate();
		}
		
		
		private double getRunUpLength()
		{
			double distance = getBall().getPos().distanceTo(target.getPos());
			double maxVel = getBot().getBotParams().getKickerSpecs().getMaxAbsoluteChipVelocity();
			double maxAcc = getMoveCon().getMoveConstraints().getAccMax();
			int numTouchdowns = 2;
			
			double botVel = Math.min(getMoveCon().getMoveConstraints().getVelMax(),
					getBall().getChipConsultant().botVelocityToChipFartherThanMaximumDistance(distance, numTouchdowns,
							maxVel));
			
			if (botVel <= 0)
			{
				return 0;
			}
			return 0.5 * maxAcc * Math.pow(botVel / maxAcc, 2) * 1000 + Geometry.getBotRadius() + Geometry.getBallRadius();
		}
	}
	
	private class RunUpState extends MoveToState
	{
		private IVector2 fixedTarget;
		protected RunUpState()
		{
			super(RunUpChipSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBotsObstacle(false);
			getMoveCon().setBallObstacle(false);
			getMoveCon().setArmChip(true);
			getMoveCon().setGoalPostObstacle(false);
			getMoveCon().setPenaltyAreaAllowedOur(true);
			getMoveCon().setPenaltyAreaAllowedTheir(true);
			dribblerOn = true;
			fixedTarget = target.getPos();
		}
		
		
		@Override
		public void doUpdate()
		{
			getMoveCon().updateDestination(fixedTarget.getXYVector());
			getMoveCon().updateLookAtTarget(LineMath.stepAlongLine(getPos(), fixedTarget.getXYVector(),
					fixedTarget.getXYVector().subtractNew(getPos()).getLength2() + Geometry.getBotRadius()));
			double maxAcc = getMoveCon().getMoveConstraints().getAccMax();
			double maxVel = getMoveCon().getMoveConstraints().getVelMax();
			double maxDistance = maxVel * maxVel / maxAcc;
			
			if (getBall().getPos().distanceTo(getTBot().getPos()) > maxDistance * 1000)
			{
				getMoveCon().setArmChip(false);
				getMoveCon().setBotsObstacle(true);
				triggerEvent(RunUpEvents.PREPARE);
			}
			drawShapes();
			super.doUpdate();
		}
		
	}
	
	private class KickState extends MoveToState
	{
		private KickState()
		{
			super(RunUpChipSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			dribblerOn = true;
		}
		
		
		@Override
		public void doUpdate()
		{
			getMoveCon().updateDestination(getDestination());
			getMoveCon().updateTargetAngle(getTargetOrientation());
			super.doUpdate();
		}
		
		
		protected double getTargetOrientation()
		{
			return target.getPos().subtractNew(getBall().getPos()).getAngle(0);
		}
		
		
		private IVector2 getDestination()
		{
			return LineMath.stepAlongLine(getBall().getPos(), target.getPos(), -getTBot().getCenter2DribblerDist());
		}
	}
}
