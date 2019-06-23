/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12 Apr 2016
 * Author(s): croscher
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author croscher
 */
public class KeeperStateCalc extends ACalculator
{
	
	// Configurable Variables
	@Configurable(comment = "Distance from FoeBot to Ball Keeper beliefs he is in posession of the ball")
	private static double	foeBotBallPosessionDistance	= (Geometry.getBotRadius() * 3);
	
	@Configurable(comment = "Area additional around PE where ball and Foe are very Dangerous (GoOutState is triggered)")
	private static double	ballDangerZone						= Geometry.getBotRadius() * 4;
	
	@Configurable(comment = "Ball Speed where Keeper react on his direction")
	private static double	blockDecisionVelocity			= 0.1;
	
	@Configurable(comment = "offset to the Sides of the goalposts (BalVelIsDirToGoal State)")
	private static double	goalAreaOffset						= Geometry.getBotRadius() * 2;
	
	@Configurable(comment = "Additional Penaulty Area margin where Keeper goes into ChipKickState")
	private static double	chipKickDecisionDistance		= 100;
	
	@Configurable(comment = "Speedlimit of ball of ChipKickState")
	private static double	chipKickDecisionVelocity		= 0.8;
	
	/**
	 * All possible KeeperStates
	 * 
	 * @author ChrisC
	 */
	public enum EStateId
	{
		/** */
		NORMAL,
		/** */
		DEFEND_BALL_VEL_DIRECTED_TO_GOAL,
		/** */
		DEFEND_REDIRECT,
		/** */
		MOVE_TO_PENALTYAREA,
		/** */
		CHIP_FAST,
		/** */
		BALL_PLACEMENT,
		/** */
		STOPPED,
		/** */
		GO_OUT
		/** */
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		/*
		 * Prio of GameStates:
		 * - BalVelDirToGoal
		 * - Redirect
		 * - ChipAway
		 * - GoOut
		 * - MoveToPE
		 * - NormalBlockState
		 */
		
		// Is Keeper set?
		if ((baseAiFrame.getKeeperId() != null)
				&& baseAiFrame.getWorldFrame().getBots().containsKey(baseAiFrame.getKeeperId()))
		{
			// Some shortcuts for readable Code
			final TrackedBall ball = baseAiFrame.getWorldFrame().getBall();
			final IVector2 keeperPos = baseAiFrame.getWorldFrame().getBot(baseAiFrame.getKeeperId()).getPos();
			
			// Both GameStates handel the same Way
			// STATE: STOPPED
			if ((newTacticalField.getGameState() == EGameStateTeam.BALL_PLACEMENT_THEY)
					|| (newTacticalField.getGameState() == EGameStateTeam.BALL_PLACEMENT_WE)
					|| (newTacticalField.getGameState() == EGameStateTeam.STOPPED))
			{
				newTacticalField.setKeeperState(EStateId.STOPPED);
			} else
			{
				// Calculate ball velocity direction and BallPosession
				// STATE: BalVelDirToGoal
				BotDistance foeBot = newTacticalField.getEnemyClosestToBall();
				boolean isFOENearBall = false;
				// Is FOE near Ball?
				if (foeBot != BotDistance.NULL_BOT_DISTANCE)
				{
					isFOENearBall = GeoMath.distancePP(foeBot.getBot().getPos(),
							ball.getPos()) < foeBotBallPosessionDistance;
				}
				IVector2 intersect = null;
				try
				{
					intersect = GeoMath.intersectionPoint(ball.getPos(), ball.getVel(),
							Geometry.getGoalOur().getGoalCenter(), new Vector2(0, 1));
					
				} catch (MathException e)
				{
					// No intersection or ball has no velocity ->check next State
				}
				
				// Ball velocity in right direction?
				// Ball velocity big enough?
				// Intersection in Goal (with Offset)?
				if (((ball.getVel().x() < 0)
						&& (ball.getVel().getLength2() > blockDecisionVelocity))
						&& (intersect != null)
						&& (Math.abs(intersect.y()) < Math.abs(Geometry.getGoalOur().getGoalPostLeft().y() + goalAreaOffset))
						&& !isFOENearBall)
				{
					newTacticalField.setKeeperState(EStateId.DEFEND_BALL_VEL_DIRECTED_TO_GOAL);
				} else
				{
					// STATE: CatchRedirect
					// Is there a FOERedirectBot?
					BotID redirectFOEBotId = OffensiveMath.getBestRedirector(baseAiFrame.getWorldFrame(),
							baseAiFrame.getWorldFrame().getFoeBots());
					IVector2 redirectFOEBot = null;
					if (redirectFOEBotId != null)
					{
						redirectFOEBot = baseAiFrame.getWorldFrame().foeBots.get(redirectFOEBotId).getPos();
					}
					
					if ((redirectFOEBot != null)
							&& (AiMath.p2pVisibility(baseAiFrame.getWorldFrame(), redirectFOEBot,
									Geometry.getGoalOur().getGoalCenter(), baseAiFrame.getKeeperId()))
							&& (ball.getVel().getLength() > chipKickDecisionVelocity))
					{
						newTacticalField.setKeeperState(EStateId.DEFEND_REDIRECT);
					} else
					{
						// State: ChipAway
						if (Geometry.getPenaltyAreaOur().isPointInShape(ball.getPos(), chipKickDecisionDistance)
								&& (ball.getVel().getLength() < chipKickDecisionVelocity))
						{
							newTacticalField.setKeeperState(EStateId.CHIP_FAST);
						} else
						{
							// STATE: GoOut
							boolean isBallNearPE = Geometry.getPenaltyAreaOur().isPointInShape(ball.getPos(),
									ballDangerZone);
							if (isBallNearPE && isFOENearBall)
							{
								newTacticalField.setKeeperState(EStateId.GO_OUT);
							} else
							{
								// STATE: KeeperOutSidePE
								if (!Geometry.getPenaltyAreaOurExtended().isPointInShape(keeperPos))
								{
									newTacticalField.setKeeperState(EStateId.MOVE_TO_PENALTYAREA);
								} else
								{
									// STATE: NormalBlock
									newTacticalField.setKeeperState(EStateId.NORMAL);
								}
							}
						}
					}
				}
			}
		}
	}
}