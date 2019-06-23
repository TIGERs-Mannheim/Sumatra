/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.01.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ATwoPointMemory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePointPair;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Robot will position to receive a pass, just like the PassReceiver, but will
 * then try to shoot at its (goal-) target directly. Inherits behavior of both, the PassReceiver and
 * the Shooter.
 * 
 * @author GuntherB
 * 
 */
public class IndirectShooter extends ABaseRole
{
	/**  */
	private static final long	serialVersionUID	= -2364260410976638655L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static enum PositionLimitation
	{
		FREE,
		KICKOFF
	};
	
	private PositionLimitation				limitation;
	
	/** * has sender shot the ball already? */
	private boolean							senderHasShot;
	
	/** facing target for direct shot */
	private final LookAtCon					lookAt;
	/** which point shall be targeted */
	private final VisibleCon				targetVisible;
	/** sender visible */
	private final VisibleCon				senderVisible;
	
	private final FieldRasterGenerator	frg					= FieldRasterGenerator.getInstance();
	/** the rectangle the bot should be moving in */
	public AIRectangle						rect;
	
	private ShooterMemory					shooterMemory;
	
	private final int							MEMORYSIZE			= AIConfig.getRoles().getIndirectShooter().getMemorysize();
	private final int							TRIES_PER_CYCLE	= AIConfig.getRoles().getIndirectShooter().getTriesPerCycle();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public IndirectShooter(PositionLimitation limit)
	{
		this(limit, 2);
	}
	

	public IndirectShooter(PositionLimitation limit, int rectangleID)
	{
		super(ERole.INDIRECT_SHOOTER);
		
		this.limitation = limit;
		
		targetVisible = new VisibleCon();
		targetVisible.updateEnd(new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter()));
		addCondition(targetVisible);
		
		lookAt = new LookAtCon();
		lookAt.updateTarget(targetVisible.getEnd());
		addCondition(lookAt);
		
		senderVisible = new VisibleCon();
		senderVisible.updateEnd(new Vector2());
		addCondition(senderVisible);
		
		rect = frg.getAnalysingRectangle(rectangleID);
		
		senderHasShot = false;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void doFirstUpdate(AIInfoFrame curFrame)
	{
		shooterMemory = new ShooterMemory(MEMORYSIZE, TRIES_PER_CYCLE, curFrame);
	}

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		if (!senderHasShot)
		{
			findNewTarget(currentFrame);
		} else
		{
			// idle
		}
		
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// correct position?
		if (!senderHasShot)
		{
			if (!destCon.checkCondition(wFrame) || !lookAt.checkCondition(wFrame))
			{
				skills.moveTo(destCon.getDestination(), lookAt.getLookAtTarget());
				if(PositionLimitation.KICKOFF == this.limitation)
				{
					skills.setMoveConstrains(EGameSituation.KICK_OFF);
				}
			}
		} else
		{
			skills.kickArm();
		}
	}
	

	public void findNewTarget(AIInfoFrame currentFrame)
	{
		ValuePointPair newPair = shooterMemory.generateBestPoints(currentFrame);
		destCon.updateDestination(newPair.point1);
		targetVisible.updateEnd(newPair.point2);
		lookAt.updateTarget(newPair.point2);
		
	}
	

	/**
	 * Sets a new target, which can be shot at after the pass completes,
	 * will update internal conditions
	 * @param newTarget
	 */
	public void updateTarget(IVector2 newTarget)
	{
		targetVisible.updateEnd(newTarget);
	}
	

	public void updateSenderPos(IVector2 sender)
	{
		senderVisible.updateEnd(sender);
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
	 * to be called by corresponding play - is receiver looking at sender?
	 * 
	 * @param worldFrame
	 * @return
	 */
	public boolean checkLooksAtTarget(WorldFrame worldFrame)
	{
		return lookAt.checkCondition(worldFrame);
	}
	

	/**
	 * to be called by corresponding play - is Sender visible for the receiver right now?
	 * 
	 */
	public boolean checkSenderIsVisible(WorldFrame worldFrame)
	{
		return (senderVisible.checkCondition(worldFrame) && destCon.checkCondition(worldFrame));
	}
	

	public boolean isPrepared(WorldFrame worldFrame)
	{
		return checkTargetIsVisible(worldFrame) && checkLooksAtTarget(worldFrame) && checkSenderIsVisible(worldFrame);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void setSenderId(int botId)
	{
		senderVisible.resetIgnore();
		senderVisible.addToIgnore(botId);
	}
	

	public IVector2 getTarget()
	{
		return targetVisible.getEnd();
	}
	

	public void setRectangle(AIRectangle rectangle)
	{
		rect = rectangle;
	}
	

	public void setPositioningLimitation(PositionLimitation limit)
	{
		limitation = limit;
	}
	

	public PositionLimitation getPositioningLimitation()
	{
		return limitation;
	}
	

	public void forceDirectShot()
	{
		senderHasShot = true;
	}
	
	// --- inner class Shooter-Memory extends AMemory---
	
	private class ShooterMemory extends ATwoPointMemory
	{
		private final float	THEIR_GOAL_X	= AIConfig.getGeometry().getGoalTheir().getGoalCenter().x;
		/**
		 * little tolerance, so it won't always try to aim dierctly next to the goalpost, where it won't hit due to
		 * inaccuracies
		 */
		private final float	GOAL_WIDTH		= AIConfig.getGeometry().getGoalTheir().getSize() * 0.90f;
		
		
		public ShooterMemory(int memorysize, int triesPerCycle, AIInfoFrame curFrame)
		{
			super(memorysize, triesPerCycle, curFrame);
		}
		

		@Override
		public float evaluatePair(ValuePointPair valuePair, AIInfoFrame currentFrame)
		{
			if(!rect.isPointInShape(valuePair.point1)){
				return 0;
			}
			
			final float STARTING_DISTANCE = 10000000f;
			
			List<TrackedBot> botsToCheck = new ArrayList<TrackedBot>(10);
			
			for (Entry<Integer, TrackedBot> entry : currentFrame.worldFrame.foeBots.entrySet())
			{
				TrackedBot bot = entry.getValue();
				botsToCheck.add(bot);
			}
			
			float distanceStartEnd = AIMath.distancePP(valuePair.point1, valuePair.point2) + 150; // safety
			float minimumDistance = STARTING_DISTANCE;
			
			for (TrackedBot bot : botsToCheck)
			{
				float distanceBotStart = AIMath.distancePP(bot, valuePair.point1);
				float distanceBotEnd = AIMath.distancePP(bot, valuePair.point2);
				if (!(distanceStartEnd < distanceBotStart || distanceStartEnd < distanceBotEnd))
				{
					// only check those bots that possibly can be in between singlePoint and goalPoint
					float distanceBotLine = AIMath.distancePL(bot.pos, valuePair.point1, valuePair.point2);
					if (distanceBotLine < minimumDistance)
					{
						minimumDistance = distanceBotLine;
					}
				}
			}
			
			if (minimumDistance > STARTING_DISTANCE - 1f)
			{ // can't compare floats to equal, so this will evaluate whether it has changed
				return 0; // returns 0, because this can't be trusted
			}
			
			
			
			return minimumDistance;
			
		}
		

		/** How is a new Position found? */
		@Override
		public ValuePoint generateFirstPoint(AIInfoFrame currentFrame)
		{
			
			if (limitation == PositionLimitation.KICKOFF)
			{
				ValuePoint newPoint;
				
				int c = 0;
				do
				{
					c++;
					newPoint = new ValuePoint(-300, rect.getRandomPointInShape().y);
				} while (AIConfig.getGeometry().getCenterCircle().isPointInShape(newPoint) && c < 10);
				
				return newPoint;
				
			} else
			{
				ValuePoint newPoint;
				
				int c = 0;
				
				do
				{
					c++;
					newPoint = new ValuePoint(rect.getRandomPointInShape());
				} while (!AIMath.p2pVisibility(currentFrame.worldFrame, newPoint, currentFrame.worldFrame.ball.pos)
						&& c < 10);
				// only visible points allowed
				
				return newPoint;
			}
			

		}
		

		/** How is a new Target found? */
		@Override
		public ValuePoint generateSecondPoint(AIInfoFrame currentFrame)
		{
			return new ValuePoint(THEIR_GOAL_X, (float) Math.random() * GOAL_WIDTH - GOAL_WIDTH / 2.0f);
		}
		
	}
	

}
