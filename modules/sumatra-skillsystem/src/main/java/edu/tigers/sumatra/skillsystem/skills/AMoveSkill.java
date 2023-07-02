/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillFastGlobalPosition;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.Optional;

import static edu.tigers.sumatra.math.AngleMath.PI;
import static edu.tigers.sumatra.math.AngleMath.normalizeAngle;
import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generatePositionTrajectory;
import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generateRotationTrajectory;


/**
 * The base class for all move-skills (basically all skills).
 */
public abstract class AMoveSkill extends ASkill
{
	@Getter(AccessLevel.PROTECTED)
	@Configurable(
			comment = "Max speed to set during STOP. This should be low enough to give robots some room for error consumption",
			defValue = "1.0"
	)
	private static double maxStopSpeed = 1.0;

	@Getter(AccessLevel.PROTECTED)
	private WorldFrame worldFrame;
	@Getter(AccessLevel.PROTECTED)
	private GameState gameState = GameState.HALT;
	@Getter(AccessLevel.PROTECTED)
	private ITrackedBot tBot;

	@Setter(AccessLevel.PROTECTED)
	private KickParams kickParams = KickParams.disarm();


	protected final void setTargetPose(
			final IVector2 destination,
			final double targetAngle,
			final IMoveConstraints moveConstraints
	)
	{
		getMatchCtrl().setSkill(getPositioningBotSkill(destination, targetAngle, moveConstraints));

		ITrajectory<IVector3> trajectory = trajectoryToDestination(destination, targetAngle, moveConstraints);
		getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(new DrawablePlanarCurve(trajectory).setColor(Color.BLACK));

		getShapes().get(ESkillShapesLayer.PATH_DEBUG)
				.add(new DrawableBot(destination, targetAngle,
						Color.red,
						Geometry.getBotRadius() + 20,
						Geometry.getBotRadius() + 20));
	}


	private ITrajectory<IVector3> trajectoryToDestination(
			final IVector2 destination,
			final double targetAngle,
			final IMoveConstraints moveConstraints
	)
	{
		var traj2d = generatePositionTrajectory(getTBot(), destination, moveConstraints);
		var trajW = generateRotationTrajectory(getTBot(), targetAngle, moveConstraints);
		return new TrajectoryXyw(traj2d, trajW);
	}


	private AMoveBotSkill getPositioningBotSkill(
			final IVector2 destination,
			final double targetAngle,
			final IMoveConstraints moveConstraints
	)
	{
		final IVector2 dest = getWorldFrame().isInverted() ? destination.multiplyNew(-1) : destination;
		final double orient = getWorldFrame().isInverted() ? normalizeAngle(targetAngle + PI) : targetAngle;

		if (moveConstraints.isFastMove())
		{
			return new BotSkillFastGlobalPosition(dest, orient, moveConstraints);
		}
		return new BotSkillGlobalPosition(dest, orient, moveConstraints);
	}


	protected final void setLocalVelocity(
			final IVector2 vel,
			final double rot,
			final IMoveConstraints moveConstraints
	)
	{
		getMatchCtrl().setSkill(new BotSkillLocalVelocity(vel, rot, moveConstraints));
	}


	protected final void setGlobalVelocity(
			final IVector2 vel,
			final double rot,
			final IMoveConstraints moveConstraints
	)
	{
		IVector2 correctedVel = getWorldFrame().isInverted() ? vel.multiplyNew(-1) : vel;
		getMatchCtrl().setSkill(new BotSkillGlobalVelocity(correctedVel, rot, moveConstraints));
	}


	protected final void setMotorsOff()
	{
		getMatchCtrl().setSkill(new BotSkillMotorsOff());
	}


	protected final void setLocalForce(final IVector2 force, final double torque)
	{
		getMatchCtrl().setSkill(new BotSkillLocalForce(force, torque));
	}


	@Override
	public final void onSkillStarted()
	{
		super.onSkillStarted();
		doEntryActions();
	}


	@Override
	protected final void onSkillFinished()
	{
		super.onSkillFinished();
		doExitActions();
	}


	@Override
	protected final void doCalcActionsBeforeStateUpdate()
	{

		beforeStateUpdate();
	}


	@Override
	protected final void doCalcActionsAfterStateUpdate()
	{
		doUpdate();
		afterStateUpdate();
		handleVelocityLimitation();
		updateKickerDribbler(getMatchCtrl().getSkill().getKickerDribbler());
		drawSkillName();
	}


	private void handleVelocityLimitation()
	{
		getMatchCtrl().setStrictVelocityLimit(getGameState().isVelocityLimited());
		if (getGameState().isVelocityLimited())
		{
			switch (getMatchCtrl().getSkill().getType())
			{
				case GLOBAL_POSITION ->
				{
					BotSkillGlobalPosition botSkillGlobalPosition = (BotSkillGlobalPosition) getMatchCtrl().getSkill();
					botSkillGlobalPosition.setVelMax(Math.min(maxStopSpeed, botSkillGlobalPosition.getVelMax()));
				}
				case FAST_GLOBAL_POSITION ->
				{
					BotSkillFastGlobalPosition botSkillFastGlobalPosition = (BotSkillFastGlobalPosition) getMatchCtrl()
							.getSkill();
					botSkillFastGlobalPosition
							.setVelMax(Math.min(maxStopSpeed, botSkillFastGlobalPosition.getVelMax()));
				}
				default ->
				{
					// ignore other bot skills like MOTOR_OFF
				}
			}
		}
	}


	protected MoveConstraints defaultMoveConstraints()
	{
		return new MoveConstraints(getBot().getBotParams().getMovementLimits());
	}


	private void drawSkillName()
	{
		String botSkillName = getMatchCtrl().getSkill().getType().name();
		String text = getClass().getSimpleName() + "\n" +
				Optional.ofNullable(getCurrentState()).map(IState::toString).orElse("-") + "\n" +
				getSkillState().name() + "\n" +
				botSkillName;
		DrawableAnnotation dAnno = new DrawableAnnotation(getPos(), text);
		dAnno.setColor(getSkillState().getColor());
		dAnno.withFontHeight(50);
		dAnno.withCenterHorizontally(true);
		dAnno.withOffset(Vector2.fromY(250));

		getShapes().get(ESkillShapesLayer.SKILL_NAMES).add(dAnno);
	}


	protected void doEntryActions()
	{
	}


	protected void doUpdate()
	{
	}


	protected void doExitActions()
	{
	}


	protected void beforeStateUpdate()
	{
	}


	protected void afterStateUpdate()
	{
	}


	private void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		if (kickParams == null)
		{
			return;
		}
		double kickSpeed = kickParams.getKickSpeed();
		kickerDribblerOutput.setKick(kickSpeed, kickParams.getDevice(),
				kickSpeed > 0 ? EKickerMode.ARM : EKickerMode.DISARM);

		var dribblerSpecs = getBot().getBotParams().getDribblerSpecs();

		double dribbleSpeed;
		double maxDribbleCurrent;

		switch (kickParams.getDribblerMode())
		{
			case OFF ->
			{
				dribbleSpeed = 0;
				maxDribbleCurrent = 0;
			}
			case HIGH_POWER ->
			{
				dribbleSpeed = dribblerSpecs.getHighPowerSpeed();
				maxDribbleCurrent = dribblerSpecs.getHighPowerMaxCurrent();
			}
			case MANUAL ->
			{
				dribbleSpeed = kickParams.getDribbleSpeedRpm();
				maxDribbleCurrent = kickParams.getDribbleMaxCurrent();
			}
			default ->
			{
				dribbleSpeed = dribblerSpecs.getDefaultSpeed();
				maxDribbleCurrent = dribblerSpecs.getDefaultMaxCurrent();
			}
		}

		kickerDribblerOutput.setDribbler(dribbleSpeed, maxDribbleCurrent);
	}


	protected final double adaptKickSpeedToBotVel(IVector2 kickTarget, final double kickSpeed)
	{
		var dir = kickTarget.subtractNew(getTBot().getBotKickerPos());
		var targetVel = dir.scaleToNew(kickSpeed);
		var adaptedTargetVel = targetVel.subtractNew(getTBot().getVel());
		if (adaptedTargetVel.isZeroVector()
				|| AngleMath.diffAbs(adaptedTargetVel.getAngle(), dir.getAngle()) > AngleMath.DEG_090_IN_RAD)
		{
			// kick speed should be <= 0
			return 0;
		}
		return adaptedTargetVel.getLength2();
	}


	@Override
	public final void update(final WorldFrameWrapper wfw, final ABot bot, final ShapeMap shapeMap)
	{
		super.update(wfw, bot, shapeMap);
		Validate.notNull(wfw, "WorldFrameWrapper must be non-null for move-skills!");
		worldFrame = wfw.getWorldFrame(EAiTeam.primary(bot.getColor()));
		Validate.notNull(worldFrame, "WorldFrame must be non-null");
		gameState = wfw.getGameState().toBuilder().withOurTeam(bot.getColor()).build();
		if (worldFrame.getBots().containsKey(bot.getBotId()))
		{
			tBot = worldFrame.getBot(bot.getBotId());
		} else if (tBot == null)
		{
			tBot = TrackedBot.stub(bot.getBotId(), worldFrame.getTimestamp());
		}
	}


	@Override
	public BotAiInformation getBotAiInfo()
	{
		BotAiInformation aiInfo = super.getBotAiInfo();

		String ballContact = getTBot().getRobotInfo().isBarrierInterrupted() ? "BARRIER" : "NO BARRIER";
		ballContact = getTBot().getBallContact().isBallContactFromVision() ? "CONTACT|" + ballContact : ballContact;
		aiInfo.setBallContact(ballContact);

		double curVel = getVel().getLength2();
		aiInfo.setVelocityCurrent(curVel);
		aiInfo.setDribbleTraction(getTBot().getRobotInfo().getDribbleTraction());

		return aiInfo;
	}


	protected final IVector2 getPos()
	{
		return tBot.getPos();
	}


	protected final double getAngle()
	{
		return tBot.getOrientation();
	}


	protected final IVector2 getVel()
	{
		return tBot.getVel();
	}


	protected final ITrackedBall getBall()
	{
		return getWorldFrame().getBall();
	}
}
