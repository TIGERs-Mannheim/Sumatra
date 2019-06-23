/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.sampler.ParameterPermutator;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ATouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;


/**
 * Test role for redirects
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectTestRole extends ARole
{
	private static final Logger log = Logger.getLogger(RedirectTestRole.class.getName());
	private static final String DESIRED_KICK_SPEED = "desiredKickSpeed";
	private static final String DESIRED_REDIRECT_ANGLE = "desiredRedirectAngle";
	
	private IVector2 desiredDestination = null;
	private double desiredOrientation = Double.MAX_VALUE;
	private DynamicPosition target;
	
	private TimeSeriesDataCollector dataCollector = null;
	
	@Configurable(comment = "Export ball data", defValue = "false")
	private static boolean collectData = false;
	
	@Configurable(comment = "If != 0 use this fixed kickSpeed for passes", defValue = "0.0")
	private static double desiredPassKickSpeed = 0;
	
	@Configurable(comment = "If != 0 use this fixed kickSpeed for redirects", defValue = "0.0")
	private static double desiredRedirectKickSpeed = 0;
	
	@Configurable(comment = "Target ball velocity when ball hits receiver. Not used if kickSpeed is fixed!", defValue = "1.5")
	private static double passEndVel = 1.5;
	
	private IVector2 initBallPos;
	private final ReceiveState receiveState = new ReceiveState();
	
	private final IState waitState = new WaitState();
	private final IState passState = new PassState();
	private final IState redirectState = new RedirectState();
	private boolean kickWithChill = false;
	
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
	private DynamicPosition currentTarget;
	
	
	public RedirectTestRole()
	{
		this(new DynamicPosition(Vector2f.ZERO_VECTOR));
	}
	
	
	/**
	 * @param target
	 */
	public RedirectTestRole(final DynamicPosition target)
	{
		super(ERole.REDIRECT_TEST);
		this.target = target;
		
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
		RECEIVE
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();
		target.update(getWFrame());
	}
	
	
	/**
	 * @param desiredOrientation the desiredOrientation to set
	 */
	public final void setDesiredOrientation(final double desiredOrientation)
	{
		this.desiredOrientation = desiredOrientation;
	}
	
	
	/**
	 * pass
	 */
	public void changeToPass()
	{
		if (!isPassing())
		{
			triggerEvent(EEvent.PASS);
		}
	}
	
	
	/**
	 * wait
	 */
	public void changeToWait()
	{
		if (!isWaiting())
		{
			triggerEvent(EEvent.WAIT);
		}
	}
	
	
	/**
	 * redirect
	 */
	public void changeToRedirect()
	{
		if (!isRedirecting())
		{
			triggerEvent(EEvent.REDIRECT);
		}
	}
	
	
	/**
	 * receive
	 */
	public void changeToReceive()
	{
		if (!isReceiving())
		{
			triggerEvent(EEvent.RECEIVE);
		}
	}
	
	
	/**
	 * @return
	 */
	private boolean isWaiting()
	{
		return getCurrentState() == waitState;
	}
	
	
	/**
	 * @return
	 */
	private boolean isReceiving()
	{
		return getCurrentState() == receiveState;
	}
	
	
	/**
	 * @return
	 */
	private boolean isRedirecting()
	{
		return getCurrentState() == redirectState;
	}
	
	
	private KickParams getKickParams(final double desiredRedirectKickSpeed)
	{
		KickParams kickParams;
		if (desiredRedirectKickSpeed > 0)
		{
			kickParams = KickParams.straight(desiredRedirectKickSpeed);
		} else
		{
			double distance = target.distanceTo(getBall().getPos());
			double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, passEndVel);
			kickParams = KickParams.straight(kickSpeed);
		}
		return kickParams;
	}
	
	
	/**
	 * @return
	 */
	public boolean isReceivingOrRedirecting()
	{
		return (getCurrentState() == redirectState) || (getCurrentState() == receiveState);
	}
	
	
	/**
	 * @return
	 */
	private boolean isPassing()
	{
		return getCurrentState() == passState;
	}
	
	
	/**
	 * @param desiredDestination the desiredDestination to set
	 */
	public void setDesiredDestination(final IVector2 desiredDestination)
	{
		this.desiredDestination = desiredDestination;
	}
	
	
	/**
	 * @return the target
	 */
	public DynamicPosition getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(final DynamicPosition target)
	{
		this.target = target;
	}
	
	
	public void setKickWithChill(final boolean kickWithChill)
	{
		this.kickWithChill = kickWithChill;
	}
	
	
	public boolean isDrivingToDesiredDest()
	{
		return isWaiting() && !desiredDestination.isCloseTo(getPos(), 100);
	}
	
	
	private class RedirectState extends AState
	{
		@Override
		public void doEntryActions()
		{
			Map<String, Double> parameters = parameterPermutator.next();
			if (!parameters.isEmpty())
			{
				log.info("Next parameters: " + parameters);
			}
			double currentDesiredRedirectKickSpeed = parameters.getOrDefault(DESIRED_KICK_SPEED, desiredRedirectKickSpeed);
			Double desiredRedirectAngle = parameters.get(DESIRED_REDIRECT_ANGLE);
			if (desiredRedirectAngle != null)
			{
				IVector2 nextTarget = getBall().getPos().subtractNew(desiredDestination)
						.turn(-desiredRedirectAngle)
						.scaleTo(desiredDestination.distanceTo(target))
						.add(desiredDestination);
				currentTarget = new DynamicPosition(nextTarget);
			} else
			{
				currentTarget = target;
			}
			
			KickParams kickParams = getKickParams(currentDesiredRedirectKickSpeed);
			
			if (desiredDestination == null)
			{
				desiredDestination = getPos();
			}
			AMoveSkill skill = new RedirectBallSkill(desiredDestination, currentTarget, kickParams);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
			initBallPos = getWFrame().getBall().getPos();
			if (collectData)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
				String botIdShort = String.format("%d%s", getBotID().getNumber(),
						getBotID().getTeamColor().name().substring(0, 1));
				String fileName = "redirect/" + sdf.format(new Date()) + "_" + botIdShort;
				dataCollector = TimeSeriesDataCollectorFactory
						.createFullCollector(fileName);
				dataCollector.addObserver(new BallDataObserver());
				dataCollector.start();
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
					.get(EAiShapesLayer.TEST_REDIRECT);
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 120), Color.cyan));
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 150), Color.cyan));
			if (currentTarget != null)
			{
				shapes.add(new DrawableCircle(Circle.createCircle(currentTarget, 120), Color.magenta));
				shapes.add(new DrawableCircle(Circle.createCircle(currentTarget, 150), Color.magenta));
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			if (dataCollector != null)
			{
				dataCollector.stopDelayed(500);
			}
		}
	}
	
	private class WaitState extends AState
	{
		AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setBallObstacle(false);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (desiredDestination != null)
			{
				IVector2 botDest = BotShape.getCenterFromKickerPos(desiredDestination, getBot().getOrientation(),
						getBot().getCenter2DribblerDist() + Geometry.getBallRadius());
				skill.getMoveCon().updateDestination(botDest);
			}
			if (Double.compare(desiredOrientation, Double.MAX_VALUE) == 0)
			{
				skill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			} else
			{
				skill.getMoveCon().updateTargetAngle(desiredOrientation);
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
			
			KickParams kickParams = getKickParams(desiredPassKickSpeed);
			
			ATouchKickSkill skill;
			if (kickWithChill)
			{
				skill = new SingleTouchKickSkill(target, kickParams);
			} else
			{
				skill = new TouchKickSkill(target, kickParams);
			}
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
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
		private ReceiveBallSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			if (desiredDestination == null)
			{
				desiredDestination = getPos();
			}
			skill = new ReceiveBallSkill(desiredDestination);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
	}
	
	private class BallDataObserver implements ITimeSeriesDataCollectorObserver
	{
		final Map<String, Object> parameters = new HashMap<>();
		
		
		public BallDataObserver()
		{
			parameters.put("initBallPos", initBallPos.toJSON());
			parameters.put("redirectBotId", getBotID().getNumber());
			parameters.put("redirectBotColor", getBotID().getTeamColor().getId());
			parameters.put("target", currentTarget.toJSON());
			parameters.put("desiredDestination", desiredDestination.toJSON());
			parameterPermutator.current().forEach(parameters::put);
		}
		
		
		@Override
		public void onAddMetadata(final Map<String, Object> jsonMapping)
		{
			jsonMapping.putAll(parameters);
		}
	}
}
