/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill.EReceiverMode;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.IBallWatcherObserver;
import edu.tigers.sumatra.wp.VisionWatcher;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * Test role for redirects
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectRole extends ARole
{
	private IVector2					desiredDestination	= null;
	private double						desiredOrientation	= Double.MAX_VALUE;
	private DynamicPosition			target;
	private double						kickSpeed				= 0;
	private final EReceiverMode	receiverMode			= EReceiverMode.STOP_DRIBBLER;
	
	private VisionWatcher			exporter					= null;
	
	@Configurable(comment = "Export ball data")
	private static boolean			export					= false;
	
	private IVector2					initBallPos;
	private IVector2					lastBallPos;
	
	
	private RedirectRole()
	{
		super(ERole.REDIRECT);
	}
	
	
	/**
	 * @param target
	 */
	public RedirectRole(final DynamicPosition target)
	{
		super(ERole.REDIRECT);
		this.target = target;
		
		IRoleState waitState = new WaitState();
		IRoleState passState = new PassState();
		IRoleState redirectState = new RedirectState();
		IRoleState receiveState = new ReceiveState();
		setInitialState(waitState);
		addTransition(EEvent.PASS, passState);
		addTransition(EEvent.REDIRECT, redirectState);
		addTransition(EEvent.WAIT, waitState);
		addTransition(EEvent.RECEIVE, receiveState);
	}
	
	private enum EStateId
	{
		REDIRECT,
		WAIT,
		PASS,
		RECEIVE;
	}
	
	private enum EEvent
	{
		REDIRECT,
		WAIT,
		PASS,
		RECEIVE
	}
	
	private class RedirectState implements IRoleState
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
		}
		
		
		@Override
		public void doUpdate()
		{
			List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
					.get(EShapesLayer.REDIRECT_ROLE);
			shapes.add(new DrawableCircle(new Circle(getPos(), 120), Color.cyan));
			shapes.add(new DrawableCircle(new Circle(getPos(), 150), Color.cyan));
			shapes.add(new DrawableCircle(new Circle(target, 120), Color.magenta));
			shapes.add(new DrawableCircle(new Circle(target, 150), Color.magenta));
		}
		
		
		@Override
		public void doExitActions()
		{
			if (exporter != null)
			{
				exporter.stopDelayed(500);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.REDIRECT;
		}
	}
	
	private class WaitState implements IRoleState
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
			if (desiredOrientation == Double.MAX_VALUE)
			{
				skill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			} else
			{
				skill.getMoveCon().updateTargetAngle(desiredOrientation);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAIT;
		}
	}
	
	private class PassState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			KickSkill skill = new KickSkill(target);
			skill.setKickMode(EKickMode.PASS);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (((getWFrame().getTimestamp() - getBot().getBot().getLastKickTime()) / 1e9) < 0.1)
			{
				triggerEvent(EEvent.PASS);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PASS;
		}
	}
	
	private class ReceiveState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			AMoveSkill skill = new ReceiverSkill(receiverMode);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.RECEIVE;
		}
	}
	
	
	/**
	 */
	public void changeToPass()
	{
		if (!isPassing())
		{
			triggerEvent(EEvent.PASS);
		}
	}
	
	
	/**
	 */
	public void changeToWait()
	{
		if (!isWaiting())
		{
			triggerEvent(EEvent.WAIT);
		}
	}
	
	
	/**
	 */
	public void changeToRedirect()
	{
		if (!isRedirecting())
		{
			triggerEvent(EEvent.REDIRECT);
		}
	}
	
	
	/**
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
		return getCurrentState() == EStateId.WAIT;
	}
	
	
	/**
	 * @return
	 */
	public boolean isReceiving()
	{
		return (getCurrentState() == EStateId.RECEIVE);
	}
	
	
	/**
	 * @return
	 */
	public boolean isRedirecting()
	{
		return (getCurrentState() == EStateId.REDIRECT);
	}
	
	
	/**
	 * @return
	 */
	public boolean isReceivingOrRedirecting()
	{
		return (getCurrentState() == EStateId.REDIRECT) || (getCurrentState() == EStateId.RECEIVE);
	}
	
	
	/**
	 * @return
	 */
	public boolean isPassing()
	{
		return getCurrentState() == EStateId.PASS;
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
		public void postProcessing(final String fileName)
		{
		}
		
		
		@Override
		public void beforeExport(final Map<String, Object> jsonMapping)
		{
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
	
	
	@Override
	protected void afterUpdate()
	{
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
