/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.kick;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.sampler.ParameterPermutator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.test.AutoKickSampleSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Test role for redirects
 */
@Log4j2
public class RedirectTestRole extends ARole
{
	private static final String DESIRED_KICK_SPEED = "desiredKickSpeed";
	private static final String DESIRED_REDIRECT_ANGLE = "desiredRedirectAngle";

	@Configurable(comment = "Export ball data", defValue = "false")
	private static boolean collectData = false;

	@Configurable(comment = "If != 0 use this fixed kickSpeed for passes", defValue = "0.0")
	private static double desiredPassKickSpeedDefault = 0;
	@Configurable(comment = "If != 0 use this fixed kickSpeed for redirects", defValue = "0.0")
	private static double desiredRedirectKickSpeedDefault = 0;
	@Configurable(comment = "Target ball velocity when ball hits receiver. Not used if kickSpeed is fixed!", defValue = "1.5")
	private static double passEndVelDefault = 1.5;

	@Configurable(defValue = "false")
	private static boolean sampleRedirectSpeed = false;
	@Configurable(defValue = "2.5")
	private static double desiredRedirectSpeedMin = 2.5;
	@Configurable(defValue = "6.5")
	private static double desiredRedirectSpeedMax = 6.5;
	@Configurable(defValue = "0.5")
	private static double desiredRedirectSpeedStep = 0.5;

	@Configurable(defValue = "false")
	private static boolean sampleRedirectAngle = false;
	@Configurable(defValue = "0.0")
	private static double desiredRedirectAngleMin = 0.0;
	@Configurable(defValue = "1.5")
	private static double desiredRedirectAngleMax = 1.5;
	@Configurable(defValue = "0.3")
	private static double desiredRedirectAngleStep = 0.3;

	private final ParameterPermutator parameterPermutator = new ParameterPermutator();

	private final IState receiveState = new ReceiveState();
	private final IState waitState = new WaitState();
	private final IState passState = new PassState();
	private final IState redirectState = new RedirectState();

	@Setter
	private double desiredPassKickSpeed = desiredPassKickSpeedDefault;
	@Setter
	private double desiredRedirectKickSpeed = desiredRedirectKickSpeedDefault;
	@Setter
	private double passEndVel = passEndVelDefault;
	@Setter
	private EKickerDevice kickerDevice = EKickerDevice.STRAIGHT;
	@Setter
	private EKickMode kickMode = EKickMode.NORMAL;
	@Setter
	private IVector2 desiredBallPosition = null;
	@Setter
	private Double desiredOrientation = null;
	@Setter
	private IVector2 target = Vector2f.ZERO_VECTOR;


	public RedirectTestRole()
	{
		super(ERole.REDIRECT_TEST);

		setInitialState(waitState);
		addTransition(EEvent.PASS, passState);
		addTransition(EEvent.REDIRECT, redirectState);
		addTransition(EEvent.WAIT, waitState);
		addTransition(EEvent.RECEIVE, receiveState);

		if (sampleRedirectSpeed)
		{
			parameterPermutator.add(DESIRED_KICK_SPEED, desiredRedirectSpeedMin, desiredRedirectSpeedMax,
					desiredRedirectSpeedStep);
		}
		if (sampleRedirectAngle)
		{
			parameterPermutator.add(DESIRED_REDIRECT_ANGLE, desiredRedirectAngleMin, desiredRedirectAngleMax,
					desiredRedirectAngleStep);
		}
	}


	private enum EEvent implements IEvent
	{
		REDIRECT,
		WAIT,
		PASS,
		STOP_BALL,
		RECEIVE,
	}

	public enum EKickMode
	{
		NORMAL,
		CHILL,
		SAMPLE,
	}


	/**
	 * pass
	 */
	public void changeToPass()
	{
		changeState(passState);
	}


	/**
	 * wait
	 */
	public void changeToWait()
	{
		changeState(waitState);
	}


	/**
	 * redirect
	 */
	public void changeToRedirect()
	{
		changeState(redirectState);
	}


	/**
	 * receive
	 */
	public void changeToReceive()
	{
		changeState(receiveState);
	}


	public boolean isReceivingOrRedirecting()
	{
		return (getCurrentState() == redirectState) || (getCurrentState() == receiveState);
	}


	public boolean isDrivingToDesiredDest()
	{
		return getCurrentState() == waitState
				&& desiredBallPosition != null
				&& !desiredBallPosition.isCloseTo(getBot().getBotKickerPos(), 100);
	}


	public boolean readyToPass()
	{
		return desiredBallPosition == null || getBall().getPos().distanceTo(desiredBallPosition) < 500;
	}


	public KickParams getKickParams(final double distance, final double desiredKickSpeed)
	{
		if (desiredKickSpeed > 0)
		{
			return KickParams.of(kickerDevice, desiredKickSpeed);
		}
		if (kickerDevice == EKickerDevice.STRAIGHT)
		{
			double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, passEndVel);
			return KickParams.straight(kickSpeed);
		}
		double kickSpeed = getBall().getChipConsultant().getInitVelForDistAtTouchdown(distance, 3);
		return KickParams.chip(kickSpeed);
	}


	private KickParams getKickParams(final IVector2 currentTarget, final double desiredKickSpeed)
	{
		return getKickParams(currentTarget.distanceTo(getBot().getBotKickerPos()), desiredKickSpeed);
	}


	private class RedirectState extends RoleState<RedirectBallSkill>
	{
		TimeSeriesDataCollector dataCollector;
		Map<String, Double> parameters;


		RedirectState()
		{
			super(RedirectBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			parameters = parameterPermutator.next();
			if (!parameters.isEmpty())
			{
				log.info("Next parameters: " + parameters);
			}

			if (desiredBallPosition == null)
			{
				desiredBallPosition = getBot().getBotKickerPos();
			}
			skill = new RedirectBallSkill();
			skill.setBallReceivingPosition(desiredBallPosition);
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			setNewSkill(skill);

			if (collectData)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
				String botIdShort = String.format("%d%s", getBotID().getNumber(),
						getBotID().getTeamColor().name().charAt(0));
				String fileName = "redirect/" + sdf.format(new Date()) + "_" + botIdShort;
				dataCollector = TimeSeriesDataCollectorFactory
						.createFullCollector(fileName);
				dataCollector.addObserver(new BallDataObserver(getBall().getPos(), target));
				dataCollector.start();
			}
		}


		@Override
		protected void onUpdate()
		{
			double currentDesiredRedirectKickSpeed = parameters.getOrDefault(DESIRED_KICK_SPEED, desiredRedirectKickSpeed);
			KickParams kickParams = getKickParams(target, currentDesiredRedirectKickSpeed);
			IVector2 currentTarget = getCurrentTarget();

			skill.setDesiredKickParams(kickParams);
			skill.setTarget(currentTarget);

			List<IDrawableShape> shapes = getAiFrame().getShapeMap()
					.get(EAiShapesLayer.TEST_KICK);
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 120), Color.cyan));
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 150), Color.cyan));
			shapes.add(new DrawableCircle(Circle.createCircle(currentTarget, 120), Color.magenta));
			shapes.add(new DrawableCircle(Circle.createCircle(currentTarget, 150), Color.magenta));
		}


		private IVector2 getCurrentTarget()
		{
			Double desiredRedirectAngle = parameters.get(DESIRED_REDIRECT_ANGLE);
			if (desiredRedirectAngle != null)
			{
				return getBall().getPos().subtractNew(desiredBallPosition)
						.turn(-desiredRedirectAngle)
						.scaleTo(desiredBallPosition.distanceTo(target))
						.add(desiredBallPosition);
			}
			return target;
		}


		@Override
		protected void onExit()
		{
			if (dataCollector != null)
			{
				dataCollector.stopDelayed(500);
			}
		}
	}

	private class WaitState extends AState
	{
		MoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			skill.getMoveCon().setBallObstacle(false);
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if (desiredBallPosition != null)
			{
				IVector2 botDest = BotShape.getCenterFromKickerPos(desiredBallPosition, getBot().getOrientation(),
						getBot().getCenter2DribblerDist() + Geometry.getBallRadius());
				skill.updateDestination(botDest);
			}
			if (desiredOrientation == null)
			{
				skill.updateLookAtTarget(getWFrame().getBall().getPos());
			} else
			{
				skill.updateTargetAngle(desiredOrientation);
			}
		}
	}

	private class PassState extends AState
	{
		private IVector2 initBallPos;


		@Override
		public void doEntryActions()
		{
			initBallPos = getBall().getPos();

			KickParams kickParams = getKickParams(target, desiredPassKickSpeed);

			var skill = getSkill(kickParams);
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			setNewSkill(skill);
		}


		private AMoveToSkill getSkill(final KickParams kickParams)
		{
			switch (kickMode)
			{
				case NORMAL:
					return new TouchKickSkill(target, kickParams);
				case CHILL:
					return new SingleTouchKickSkill(target, kickParams);
				case SAMPLE:
					return new AutoKickSampleSkill(target, kickParams.getDevice(), kickParams.getKickSpeed());
				default:
					throw new IllegalArgumentException();
			}
		}


		@Override
		public void doUpdate()
		{
			if (initBallPos.distanceTo(getBall().getPos()) > 200
					&& getBall().getVel().getLength2() > 0.5)
			{
				triggerEvent(EEvent.WAIT);
			}
		}
	}

	private class ReceiveState extends AState
	{
		@Override
		public void doEntryActions()
		{
			if (desiredBallPosition == null)
			{
				desiredBallPosition = getBot().getBotKickerPos();
			}
			ReceiveBallSkill skill = new ReceiveBallSkill();
			skill.setBallReceivingPosition(desiredBallPosition);
			skill.setConsideredPenAreas(ETeam.UNKNOWN);
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			setNewSkill(skill);
		}
	}

	private class BallDataObserver implements ITimeSeriesDataCollectorObserver
	{
		final Map<String, Object> parameters = new HashMap<>();


		public BallDataObserver(IVector2 initBallPos, IVector2 target)
		{
			parameters.put("initBallPos", initBallPos.toJSON());
			parameters.put("redirectBotId", getBotID().getNumber());
			parameters.put("redirectBotColor", getBotID().getTeamColor().getId());
			parameters.put("target", target.toJSON());
			parameters.put("desiredDestination", desiredBallPosition.toJSON());
			parameterPermutator.current().forEach(parameters::put);
		}


		@Override
		public void onAddMetadata(final Map<String, Object> jsonMapping)
		{
			jsonMapping.putAll(parameters);
		}
	}
}
