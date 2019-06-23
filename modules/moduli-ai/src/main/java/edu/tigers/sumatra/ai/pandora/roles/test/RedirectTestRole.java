/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.awt.*;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.util.ExportDataContainer;
import edu.tigers.sumatra.wp.util.IBallWatcherObserver;
import edu.tigers.sumatra.wp.util.VisionWatcher;


/**
 * Test role for redirects
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectTestRole extends ARole
{
	private IVector2 desiredDestination = null;
	private double desiredOrientation = Double.MAX_VALUE;
	private DynamicPosition target;
	
	private VisionWatcher exporter = null;
	
	@Configurable(comment = "Export ball data", defValue = "false")
	private static boolean export = false;
	
	@Configurable(comment = "If != 0 -> use this fixed kickSpeed for redirects and passes", defValue = "5.0")
	private static double desiredKickSpeed = 5;
	
	private IVector2 initBallPos;
	private IVector2 lastBallPos;
	
	private final IState waitState = new WaitState();
	private final IState passState = new PassState();
	private final IState redirectState = new RedirectState();
	private final IState receiveState = new ReceiveState();
	
	
	private RedirectTestRole()
	{
		super(ERole.REDIRECT);
	}
	
	
	/**
	 * @param target
	 */
	public RedirectTestRole(final DynamicPosition target)
	{
		super(ERole.REDIRECT);
		this.target = target;
		
		setInitialState(waitState);
		addTransition(EEvent.PASS, passState);
		addTransition(EEvent.REDIRECT, redirectState);
		addTransition(EEvent.WAIT, waitState);
		addTransition(EEvent.RECEIVE, receiveState);
	}
	
	private enum EEvent implements IEvent
	{
		REDIRECT,
		WAIT,
		PASS,
		RECEIVE
	}
	
	private class RedirectState implements IState
	{
		private RedirectSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new RedirectSkill(target);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
			initBallPos = getWFrame().getBall().getPos();
			if (export)
			{
				exporter = new VisionWatcher("redirect/" + System.currentTimeMillis());
				exporter.addObserver(new BallDataObserver());
				exporter.start();
			}
			if (desiredKickSpeed > 0)
			{
				skill.setFixedKickSpeed(desiredKickSpeed);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
					.get(EAiShapesLayer.REDIRECT_ROLE);
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 120), Color.cyan));
			shapes.add(new DrawableCircle(Circle.createCircle(getPos(), 150), Color.cyan));
			shapes.add(new DrawableCircle(Circle.createCircle(target, 120), Color.magenta));
			shapes.add(new DrawableCircle(Circle.createCircle(target, 150), Color.magenta));
		}
		
		
		@Override
		public void doExitActions()
		{
			if (exporter != null)
			{
				exporter.stopDelayed(500);
			}
		}
		
		
	}
	
	private class WaitState implements IState
	{
		AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (desiredDestination != null)
			{
				skill.getMoveCon().updateDestination(desiredDestination);
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
	
	private class PassState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			KickNormalSkill skill = new KickNormalSkill(target);
			
			if (desiredKickSpeed > 0)
			{
				skill.setKickMode(EKickMode.FIXED_SPEED);
				skill.setKickSpeed(desiredKickSpeed);
			} else
			{
				skill.setKickMode(EKickMode.PASS);
			}
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
	}
	
	private class ReceiveState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			AMoveSkill skill = new ReceiverSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
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
	public boolean isWaiting()
	{
		return getCurrentState() == waitState;
	}
	
	
	/**
	 * @return
	 */
	public boolean isReceiving()
	{
		return getCurrentState() == receiveState;
	}
	
	
	/**
	 * @return
	 */
	public boolean isRedirecting()
	{
		return getCurrentState() == redirectState;
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
	public boolean isPassing()
	{
		return getCurrentState() == passState;
	}
	
	
	/**
	 * @return the desiredDestination
	 */
	public IVector2 getDesiredDestination()
	{
		return desiredDestination;
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
	
	
	private class BallDataObserver implements IBallWatcherObserver
	{
		@Override
		public void onAddCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
		{
			container.getCustomNumberListable().put("redirectBot",
					ExportDataContainer.trackedBot2WpBot(getBot(), getWFrame().getId(), frame.getBall().getTimestamp()));
			container.getCustomNumberListable().put("target", getTarget());
			container.getCustomNumberListable().put("kickerPos", getBot().getBotKickerPos());
		}
		
		
		@Override
		public void beforeExport(final Map<String, Object> jsonMapping)
		{
			final double kickSpeed = getBot().getRobotInfo().getKickSpeed();
			jsonMapping.put("duration", kickSpeed);
			if (initBallPos != null)
			{
				jsonMapping.put("initBallPos", initBallPos.toJSON());
			}
			if (lastBallPos != null)
			{
				jsonMapping.put("lastBallPos", lastBallPos.toJSON());
			}
		}
	}
	
	
	/**
	 * @return the desiredOrientation
	 */
	public final double getDesiredOrientation()
	{
		return desiredOrientation;
	}
	
	
	/**
	 * @param desiredOrientation the desiredOrientation to set
	 */
	public final void setDesiredOrientation(final double desiredOrientation)
	{
		this.desiredOrientation = desiredOrientation;
	}
}
