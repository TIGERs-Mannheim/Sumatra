/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards.penalty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.APointMemory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyUsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Penalty Shooter! Used by the {@link PenaltyUsPlay}
 * 
 * @author Malte
 * 
 */
public class PenaltyUsShooterRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID								= 9129645740870778582L;
	
	private boolean				ready												= false;
	
	private AimingCon				aimingCon;
	private boolean				desperateShot									= false;
	
	private ShooterMemory		memory;
	private static final int	MEMORY_SIZE										= 2;
	private static final int	TRIES_PER_CYCLE								= 10;
	private final float			GOALWIDTH										= AIConfig.getGeometry().getGoalTheir().getSize() * 0.85f;
	
	private final float			AIMING_TOLERANCE_MULTIPLIER_PER_FRAME	= 1.0006f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PenaltyUsShooterRole()
	{
		super(ERole.PENALTY_US_SHOOTER);
		
		aimingCon = new AimingCon();
		addCondition(aimingCon);
		
		memory = new ShooterMemory(MEMORY_SIZE, TRIES_PER_CYCLE);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		
		if (!ready)
		{
			Vector2 destination = new Vector2(AIConfig.getGeometry().getPenaltyMarkTheir());
			destination.add(new Vector2f(-200, 0));
			destCon.updateDestination(destination);
			
			Vector2 target = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
			memory.generateBestPoint(currentFrame);
			aimingCon.updateAimingTarget(target);
		} else
		{
			Vector2 destination = new Vector2(currentFrame.worldFrame.ball.pos);
			destination.add(new Vector2(-100, 80));
			destCon.updateDestination(destination);
			aimingCon.updateAimingTarget(memory.generateBestPoint(currentFrame));
			
			// slowly increasing tolerance, so it will shoot eventually
			// increasing value is best guess from sim testing
			aimingCon.setAimTolerance(aimingCon.getAimingTolerance() * AIMING_TOLERANCE_MULTIPLIER_PER_FRAME);
		}
		
		// float percentage = aimingCon.getAimingTolerance() / AIConfig.getTolerances().getAiming();
		// System.out.println("Shooter Aiming Tolerance Percentage " + percentage);
		
		currentFrame.addDebugPoint(aimingCon.getAimingTarget());
		
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (!ready)
		{
			if (!aimingCon.checkCondition(wFrame) || !destCon.checkCondition(wFrame))
			{
				skills.moveTo(destCon.getDestination(), aimingCon.getAimingTarget());
			}
		} else
		{
			if (desperateShot)
			{
				// disclaimer: might miss the ball due to wrong aiming..
				skills.kickArm();
				skills.kickAuto();
				skills.dribble(false);
			}
			if (!aimingCon.checkCondition(wFrame))
			{
				skills.aiming(aimingCon, EGameSituation.SET_PIECE);
			} else
			{
				skills.kickArm();
				skills.kickAuto();
				skills.dribble(false);
			}
			
		}
		

	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setReady(boolean ready)
	{
		this.ready = ready;
	}
	

	public void setDesperateShot(boolean despShot)
	{
		desperateShot = despShot;
	}
	
	
	// --------------------------------------------------------------------------
	// --- INNER CLASS --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public class ShooterMemory extends APointMemory
	{
		
		/**
		 * @param memorySize
		 * @param triesPerCycle
		 */
		public ShooterMemory(int memorySize, int triesPerCycle)
		{
			super(memorySize, triesPerCycle);
		}
		

		@Override
		public float evaluatePoint(ValuePoint valuePoint, AIInfoFrame currentFrame)
		{
			// speedhack :P
			
			// vs enemy team
			WorldFrame worldFrame = currentFrame.worldFrame;
			Vector2 end = valuePoint;
			Vector2 start = new Vector2(worldFrame.ball.pos);
			
			List<TrackedBot> botsToCheck = new ArrayList<TrackedBot>(5);
			
			for (Entry<Integer, TrackedBot> entry : worldFrame.foeBots.entrySet())
			{
				TrackedBot bot = entry.getValue();
				botsToCheck.add(bot);
				
			}
			
			float distanceStartEnd = AIMath.distancePP(start, end) + 150; // for safety
			float minimumDistance = 1000000f;
			
			for (TrackedBot bot : botsToCheck)
			{
				float distanceBotStart = AIMath.distancePP(bot, start);
				float distanceBotEnd = AIMath.distancePP(bot, end);
				if (!(distanceStartEnd < distanceBotStart || distanceStartEnd < distanceBotEnd))
				{
					// only check those bots that possibly can be in between start and end
					float distanceBotLine = AIMath.distancePL(bot.pos, start, end);
					if (distanceBotLine < minimumDistance)
					{
						minimumDistance = distanceBotLine;
					}
				}
			}
			
			return minimumDistance;
			
		}
		

		@Override
		public ValuePoint generateNewPoint(AIInfoFrame currentFrame)
		{
			return new ValuePoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter().x, (float) Math.random()
					* GOALWIDTH - GOALWIDTH / 2.0f);
		}
		
	}
}
