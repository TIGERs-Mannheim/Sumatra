/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): GuntherB
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.APointMemory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Role positions itself near the ball and then proceeds to aim at the target location. If told so by the Play,
 * it shoots the ball at the target by slowly approaching it and then firing its kicking device
 * 
 * @author GuntherB
 * 
 */
public class Shooter extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -6520068570223942230L;
	private AimingCon				aiming;
	
	private boolean				shotForced;
	private boolean				desperateShot		= false;
	
	private EGameSituation		gameSit;
	
	private final float			AIMINGDISTANCE		= AIConfig.getTolerances().getPositioning()
																		+ AIConfig.getGeometry().getBallRadius()
																		+ AIConfig.getGeometry().getBotRadius() - 25f;
	private final float			NEAR_TOLERANCE		= AIConfig.getTolerances().getNearBall();
	
	private final float			GOALWIDTH			= AIConfig.getGeometry().getGoalTheir().getSize() * 0.85f;
	
	private ShooterMemory		myMemory;
	
	private final int				MEMORYSIZE			= AIConfig.getRoles().getIndirectShooter().getMemorysize();
	private final int				TRIES_PER_CYCLE	= AIConfig.getRoles().getIndirectShooter().getTriesPerCycle();
	
	public enum EShootingMode
	{
		GOAL,
		SAFESHOT;
	}
	
	private EShootingMode	shootingMode;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * @param type
	 */
	public Shooter(EGameSituation gameSituation)
	{
		super(ERole.SHOOTER);
		
		destCon.setTolerance(AIMINGDISTANCE + 50f);
		
		if (gameSituation == EGameSituation.KICK_OFF || gameSituation == EGameSituation.SET_PIECE)
		{
			destCon.setTolerance(230);
		}
		
		aiming = new AimingCon(AIConfig.getTolerances().getAiming());
		aiming.setNearTolerance(NEAR_TOLERANCE);
		addCondition(aiming);
		

		shotForced = false;
		
		gameSit = gameSituation;
		shootingMode = EShootingMode.GOAL;
		
		myMemory = new ShooterMemory(MEMORYSIZE, TRIES_PER_CYCLE);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		Vector2f dest = new Vector2f(currentFrame.worldFrame.ball.pos);
		if (AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(dest))
		{
			dest = new Vector2f(AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(dest));
		}
		destCon.updateDestination(dest);
		findNewTarget(currentFrame);
		currentFrame.addDebugPoint(aiming.getAimingTarget());
	}
	

	public Vector2 findNewTarget(AIInfoFrame currentFrame)
	{
		if (shootingMode == EShootingMode.GOAL)
		{
			aiming.updateAimingTarget(myMemory.generateBestPoint(currentFrame));
			
		} else
		{
			Vector2f safeTarget = new Vector2f(AIConfig.getGeometry().getFieldLength() / 4, AIConfig.getGeometry()
					.getFieldWidth() / 4);
			aiming.updateAimingTarget(safeTarget);
		}
		

		return aiming.getAimingTarget();
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// Shot IS forced - everything's fine
		if (shotForced)
		{
			if (shootingMode == EShootingMode.SAFESHOT)
			{
				System.out.println("Safe: Kick!!!!!!!");
				skills.kickArm(AIMath.distancePP(wFrame.ball.pos, aiming.getAimingTarget()));
				skills.kickAuto(AIMath.distancePP(wFrame.ball.pos, aiming.getAimingTarget()));
			} else
			{
				
				System.out.println("Kick!!!!!!!");
				skills.kickArm();
				skills.kickAuto();
			}
			
			skills.dribble(false);
			shotForced = false;
			return;
		}
		
		// Desperate shot, shoot if you can, but continue aiming
		
		if (desperateShot)
		{
			skills.aiming(aiming, gameSit);
			if (shootingMode == EShootingMode.SAFESHOT)
			{
				skills.kickArm(AIMath.distancePP(wFrame.ball.pos, aiming.getAimingTarget()));
			} else
			{
				skills.kickArm();
			}
			skills.dribble(true);
			return;
		}
		
		// Shot NOT forced - just work on your positioning
		if (!destCon.checkCondition(wFrame))
		{
			skills.moveTo(destCon.getDestination(), aiming.getAimingTarget());
		} else
		{
			if (!aiming.checkCondition(wFrame))
			{
				skills.aiming(aiming, gameSit);
				skills.disarm();
				skills.dribble(true);
			} else
			{
				System.out.println("Shooter says: Kick!!!");
			}
			// idle
		}
		
	}
	

	/**
	 * forces the Shooter to shoot ASAP
	 */
	public void forceShot()
	{
		shotForced = true;
	}
	

	/**
	 * shooter will kickarm in addition to all other actions, if it looks somewhere forward
	 */
	public void shootDesperately()
	{
		desperateShot = true;
	}
	

	/**
	 * optionally set by play in afterupdate to override role's own target
	 * @param newTarget
	 */
	public void updateTarget(Vector2 newTarget)
	{
		aiming.updateAimingTarget(newTarget);
	}
	

	@Deprecated
	public void updateDesignatedPos(Vector2 newPos)
	{
		destCon.updateDestination(newPos);
	}
	

	@Override
	public void initDestination(IVector2 destination)
	{
		destCon.getDestination();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public boolean isReadyToShoot(WorldFrame worldFrame)
	{
		return aiming.checkCondition(worldFrame); // (destCon.checkCondition(worldFrame)
	}
	

	@Override
	public IVector2 getDestination()
	{
		return destCon.getDestination();
	}
	

	public Vector2 getTarget()
	{
		return aiming.getAimingTarget();
	}
	

	public void incAimingTolerance()
	{
		aiming.setAimTolerance(aiming.getAimingTolerance() * 1.00005f);
		
		// float percentage = aiming.getAimingTolerance() / AIConfig.getTolerances().getAiming();
		// System.out.println("Shooter Aiming Tolerance Percentage " + percentage);
	}
	

	public void setMode(EShootingMode newMode)
	{
		this.shootingMode = newMode;
		if (newMode == EShootingMode.GOAL)
		{
			aiming.setAimTolerance(AIConfig.getTolerances().getAiming() * 1.2f);
		} else
		{
			aiming.setAimTolerance(AIConfig.getTolerances().getAiming() * 3f);
		}
	}
	
	
	// --- inner class Shooter-Memory extends AMemory---
	
	private class ShooterMemory extends APointMemory
	{
		
		public ShooterMemory(int memorysize, int triesPerCycle)
		{
			super(memorysize, triesPerCycle);
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
			
			float distanceStartEnd = AIMath.distancePP(start, end) + 150; // + 150 for safety, don't wanna forget one
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
