/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Role will try to receive a pass from a corresponding PassSender
 * It will try to find a position, where a) the ball and b) the target is visible.
 * Also it will try to rotate to face the PassSender.
 * 
 * @author GuntherB
 * 
 */
public class PassReceiver extends ABaseRole
{
	/**  */
	private static final long					serialVersionUID	= 3222779676881933751L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Goal							goalTheir			= AIConfig.getGeometry().getGoalTheir();
	
	private boolean								forceReceive;
	
	/** where the sender will be */
	private final VisibleCon					senderVisible;
	/** also facing the sender */
	private final LookAtCon						lookAt;
	/** which point shall be targeted */
	private final VisibleCon					targetVisible;
	
	private int										senderId				= UNINITIALIZED_BOTID;
	
	private final ArrayList<AVisibleCon>	visibleCons;
	
	private final FieldRasterGenerator		frg					= FieldRasterGenerator.getInstance();
	/** the rectangle the bot should be moving in */
	private AIRectangle							rect;
	private boolean								forceNewPosition;
	
	private WorldFrame							worldFrame;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * creates a new Role, that will receive the ball from a sender-bot
	 */
	public PassReceiver()
	{
		super(ERole.PASS_RECEIVER);
		
		senderVisible = new VisibleCon();
		addCondition(senderVisible);
		
		targetVisible = new VisibleCon();
		targetVisible.updateEnd(goalTheir.getGoalCenter());
		addCondition(targetVisible);
		
		lookAt = new LookAtCon(); // will change to sender after first update
		addCondition(lookAt);
		
		visibleCons = new ArrayList<AVisibleCon>();
		visibleCons.add(senderVisible);
		visibleCons.add(targetVisible);
		
		rect = frg.getPositioningRectangle(1);
		
		forceReceive = false;
		forceNewPosition = false;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		worldFrame = currentFrame.worldFrame;
		
		// if position is bad, get new position
		
		// TODO GuntherB , switch this to point memory (by GuntherB)
		
		if (!(senderVisible.checkCondition(worldFrame) && targetVisible.checkCondition(worldFrame)) || forceNewPosition)
		{
			forceNewPosition = false;
			
			destCon.updateDestination(calcDestination(senderVisible.getEnd()));
			
			senderVisible.updateStart(destCon.getDestination());
			targetVisible.updateStart(destCon.getDestination());
		}
		

	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		worldFrame = wFrame;
		
		// if receive is forced by play, the skills are added and returned immediately
		if (forceReceive)
		{
			// skills.add(new TigerStraightMove(1000));
			// skills.moveStraight(1000);
			
			forceReceive = false;
			skills.dribble(true);
			return;
		}
		

		// correct position?
		if (!destCon.checkCondition(worldFrame) || !lookAt.checkCondition(wFrame))
		{
			// float distance = AIMath.distancePP(destCon.getDestination(), worldFrame.tigerBots.get(getBotID()).pos);
			// float tolerance = destCon.getTolerance();
			// System.out.println("PassReceiver tries to move: distancePP is " + distance + ", should be under " +
			// tolerance);
			skills.moveTo(destCon.getDestination(), lookAt.getLookAtTarget());
		}
	}
	

	/**
	 * calculate a new destination, from which the ball should be received
	 * 
	 * @param passSender
	 */
	public IVector2 calcDestination(IVector2 passSender)
	{
		return frg.getRandomPointWithConditions(rect, worldFrame, getBotID(), visibleCons);
	}
	

	/**
	 * forces the Role to Receive Pass ASAP
	 */
	public void forceReceive()
	{
		forceReceive = true;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Sets a new target, which can be shot at after the pass completes,
	 * will update internal conditions
	 * @param newTarget
	 */
	public void updateTarget(IVector2 newTarget)
	{
		targetVisible.updateEnd(newTarget);
	}
	

	/**
	 * Sets the new position where the sender is supposed to move to,
	 * will update internal conditions
	 * 
	 * @param newSenderPos
	 */
	public void updateSenderPos(IVector2 newSenderPos)
	{
		senderVisible.updateEnd(newSenderPos);
		lookAt.updateTarget(newSenderPos);
	}
	

	/**
	 * to be called by corresponding play - is receiver looking at sender?
	 * 
	 * @param worldFrame
	 * @return
	 */
	public boolean checkLooksAtSender(WorldFrame worldFrame)
	{
		return lookAt.checkCondition(worldFrame);
	}
	

	/**
	 * to be called by corresponding play - is the target visible for the receiver right now?
	 * 
	 */
	public boolean checkTargetIsVisible(WorldFrame worldFrame)
	{
		return (targetVisible.checkCondition(worldFrame) && destCon.checkCondition(worldFrame));
	}
	

	/**
	 * to be called by corresponding play - is Sender visible for the receiver right now?
	 * 
	 */
	public boolean checkSenderIsVisible(WorldFrame worldFrame)
	{
		return (senderVisible.checkCondition(worldFrame) && destCon.checkCondition(worldFrame));
	}
	

	public void updateRectangle(int i)
	{
		this.rect = frg.getPositioningRectangle(i);
		forceNewPosition = true;
	}
	

	public void updateSenderId(int botId)
	{
		if (this.senderId != botId && senderId != -1)
		{
			senderVisible.removeFromIgnore(senderId);
			senderVisible.addToIgnore(botId);
		}
		this.senderId = botId;
		
	}
	

	public boolean isWellAimed(WorldFrame worldFrame)
	{
		return lookAt.checkCondition(worldFrame) && destCon.checkCondition(worldFrame)
				&& senderVisible.checkCondition(worldFrame);
	}
	

	public IVector2 getTarget()
	{
		return targetVisible.getEnd();
	}
}
