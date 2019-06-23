/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiverSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.BallWatcher;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.IBallWatcherObserver;


/**
 * Test role for redirects
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectRole extends ARole
{
	private IVector2			desiredDestination	= null;
	private float				desiredOrientation	= Float.MAX_VALUE;
	private DynamicPosition	target;
	private int					duration					= 0;
	
	private BallWatcher		exporter					= null;
	
	@Configurable(comment = "Export ball data")
	private static boolean	export					= false;
	
	private IVector2			initBallPos;
	private IVector2			lastBallPos;
	
	
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.BARRIER);
		features.add(EFeature.STRAIGHT_KICKER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
		private RedirectSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new RedirectSkill(target);
			skill.setDesiredDuration(duration);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
			initBallPos = getWFrame().getBall().getPos();
			if (export)
			{
				exporter = new BallWatcher("redirect/" + SumatraClock.currentTimeMillis());
				exporter.addObserver(new BallDataObserver());
				exporter.start();
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(getPos(), 120), Color.cyan));
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(getPos(), 150), Color.cyan));
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(target, 120), Color.magenta));
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(target, 150), Color.magenta));
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.REDIRECT;
		}
	}
	
	private class WaitState implements IRoleState
	{
		IMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (desiredDestination != null)
			{
				skill.getMoveCon().updateDestination(desiredDestination);
			}
			if (desiredOrientation == Float.MAX_VALUE)
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
			AMoveSkill skill = new KickSkill(target, EMoveMode.NORMAL, duration);
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.PASS);
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
			AMoveSkill skill = new ReceiverSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
	
	
	/**
	 * @return the duration
	 */
	public int getDuration()
	{
		return duration;
	}
	
	
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(final int duration)
	{
		this.duration = duration;
	}
	
	private class BallDataObserver implements IBallWatcherObserver
	{
		@Override
		public void onAddCustomData(final ExportDataContainer container, final MergedCamDetectionFrame frame)
		{
			container.getCustomNumberListable().put("redirectBot",
					ExportDataContainer.trackedBot2WpBot(getBot(), getWFrame().getId(), frame.getBall().getTimestamp()));
			container.getCustomNumberListable().put("target", getTarget());
			container.getCustomNumberListable().put("kickerPos", AiMath.getBotKickerPos(getBot()));
		}
		
		
		@Override
		public void postProcessing(final String fileName)
		{
		}
		
		
		@Override
		public void beforeExport(final Map<String, Object> jsonMapping)
		{
			jsonMapping.put("duration", duration);
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
	public final float getDesiredOrientation()
	{
		return desiredOrientation;
	}
	
	
	/**
	 * @param desiredOrientation the desiredOrientation to set
	 */
	public final void setDesiredOrientation(final float desiredOrientation)
	{
		this.desiredOrientation = desiredOrientation;
	}
}
