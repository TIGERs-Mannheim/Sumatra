/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.07.2010
 * Author(s):
 * FlorianS
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards;

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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.APointMemory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * The BallGetter goes behind the ball facing a certain point in our opponent's
 * goal and acquires ball possession. Assumes the ball just sits somewhere and
 * no-one possesses the ball. A viewPoint is the point the bot will look at when
 * it is behind the ball in order to shoot right after getting the ball. This
 * Role will be armed all the time!
 * 
 * @author FlorianS
 */
public class FreekickerRoleV2 extends ABaseRole
{
	/**  */
	private static final long	serialVersionUID			= -8374401797710010387L;
	
	private LookAtCon				lookAtCon;
	
	float								usedSpace;
	/** distance between the centers of bot and ball (when bot looks at the ball) */
	private final float			SPACE_MIN_ORIENTATION	= 70;																			// from
																																							// 0
																																							

	// private final float SPACE_MIN_KICK = 0;
	// private float current_space_min;
	
	/** distance between the centers of bot and ball (when bot doesn't look at the ball) */
	private final float			SPACE_MAX					= AIConfig.getRoles().getBallGetter().getSpaceMax();
	

	private ShooterMemory		myMemory;
	
	private final int				MEMORYSIZE					= AIConfig.getRoles().getIndirectShooter().getMemorysize();
	private final int				TRIES_PER_CYCLE			= AIConfig.getRoles().getIndirectShooter().getTriesPerCycle();
	
	private final float			GOALWIDTH					= AIConfig.getGeometry().getGoalTheir().getSize() * 0.90f;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public FreekickerRoleV2()
	{
		super(ERole.FREEKICKER_V2);
		
		myMemory = new ShooterMemory(MEMORYSIZE, TRIES_PER_CYCLE);
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
		
		// current_space_min = SPACE_MIN_ORIENTATION;
	}
	

	/**
	 * The idea of this BallGetter is the following:
	 * * We have a ball somewhere on the field
	 * * We want to "get" it, while looking at a given viewpoint (if the viewpoint is not defined, simply look at the
	 * ball)
	 */
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		Vector2 botPos = new Vector2(currentFrame.worldFrame.tigerBots.get(getBotID()).pos);
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		
		float currentAngleBetweenBallAndBot = AIMath.angleBetweenXAxisAndLine(botPos, ballPos);
		
		// angle between ball and bot (0-2PI);
		float angleBB = Math.abs(currentAngleBetweenBallAndBot - currentFrame.worldFrame.tigerBots.get(getBotID()).angle);
		
		// angle between ball and bot(0-PI)
		if (angleBB > Math.PI)
		{
			angleBB = (float) (Math.PI * 2 - angleBB);
		}
		
		// calculate desired distance between bot and ball. distance depends on the angle between bot and ball
		usedSpace = (float) (SPACE_MIN_ORIENTATION + (1 - 2 * (1 / (angleBB / Math.PI) + 2)))
				* (SPACE_MAX - SPACE_MIN_ORIENTATION);
		
		if (usedSpace < SPACE_MIN_ORIENTATION + 20)
		{
			usedSpace = 0;
		}
		
		destCon.updateDestination(calculateDestination(currentFrame));
		
		lookAtCon.updateTarget(myMemory.generateBestPoint(currentFrame));
		
		currentFrame.addDebugPoint(lookAtCon.getLookAtTarget());
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		boolean correctPosition = destCon.checkCondition(wFrame);
		boolean correctAngle = lookAtCon.checkCondition(wFrame);
		
		skills.setMoveFast(false);
		
		if (!correctPosition || !correctAngle)
		{
			skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
		}
		
		float angle = wFrame.tigerBots.get(getBotID()).angle;
		if (angle < AIMath.PI_QUART && angle > -AIMath.PI_QUART)
		{
			skills.kickArm();
		} else
		{
			skills.disarm();
		}
	}
	

	/**
	 * calculates the position, where our bot shall be send to. In later versions, you will differ between a rolling ball
	 * and a sitting ball here
	 */
	private IVector2 calculateDestination(AIInfoFrame frame)
	{
		WorldFrame worldFrame = frame.worldFrame;
		IVector2 ballPos = worldFrame.ball.pos;
		

		float FACTOR = 100;
		ballPos = ballPos.addNew(worldFrame.ball.vel.multiplyNew(FACTOR));
		return AIMath.stepAlongLine(ballPos, lookAtCon.getLookAtTarget(), -usedSpace);
		

	}
	

	public void setDestTolerance(float newTolerance)
	{
		destCon.setTolerance(newTolerance);
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
			// Vector2 start = new Vector2(worldFrame.ball.pos);
			
			IVector2 start = FreekickerRoleV2.this.destCon.getDestination();
			
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