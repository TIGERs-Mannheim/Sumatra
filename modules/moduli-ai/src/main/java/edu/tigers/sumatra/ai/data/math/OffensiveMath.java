/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.04.2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.driver.KickSkillCalc;
import edu.tigers.sumatra.skillsystem.driver.KickSkillCalc.EKickSkillState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Math and methods for the Offensive
 * (There is just too much chaos in AiMath)
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveMath
{
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	public static BotIDMap<ITrackedBot> getPotentialOffensiveBotMap(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		BotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			if (newTacticalField.getOffensiveActions().containsKey(key))
			{
				botMap.put(key, baseAiFrame.getWorldFrame().getTigerBotsVisible().get(key));
			}
		}
		if (baseAiFrame.getKeeperId() != null)
		{
			if (botMap.containsKey(baseAiFrame.getKeeperId()))
			{
				botMap.remove(baseAiFrame.getKeeperId());
			}
		}
		for (BotID key : newTacticalField.getCrucialDefenders())
		{
			if (botMap.containsKey(key))
			{
				// botMap.remove(key);
			}
		}
		return botMap;
	}
	
	
	/**
	 * @param wFrame
	 * @param ourPenAreaMargin
	 * @param theirPenAreaMargin
	 * @return
	 */
	public static boolean isBallNearPenaltyAreaOrOutsideField(final WorldFrame wFrame, final double ourPenAreaMargin,
			final double theirPenAreaMargin)
	{
		PenaltyArea ourPenArea = Geometry.getPenaltyAreaOur();
		PenaltyArea theirPenArea = Geometry.getPenaltyAreaTheir();
		IVector2 ballPos = wFrame.getBall().getPos();
		
		if (ourPenArea.isPointInShape(ballPos, ourPenAreaMargin))
		{
			return true;
		}
		if (theirPenArea.isPointInShape(ballPos, theirPenAreaMargin))
		{
			return true;
		}
		if (!Geometry.getField().isPointInShape(ballPos))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param wFrame
	 * @param botPos
	 * @param target
	 * @return
	 */
	public static boolean isBallRedirectPossible(final WorldFrame wFrame,
			final IVector2 botPos, final IVector2 target)
	{
		IVector2 ballToBot = botPos.subtractNew(wFrame.getBall().getPos()).normalizeNew();
		IVector2 botToTarget = target.subtractNew(botPos).normalizeNew();
		
		double product = ballToBot.scalarProduct(botToTarget);
		if (product < 0)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param wf
	 * @param kickerPos
	 * @param target
	 * @return
	 */
	public static boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 kickerPos, final IVector2 target)
	{
		IVector2 botToBall = wf.getBall().getPos().subtractNew(kickerPos);
		IVector2 botToTarget = target.subtractNew(kickerPos);
		double redirectAngle = GeoMath.angleBetweenVectorAndVector(botToBall, botToTarget);
		
		if (ProbabilityMath.getDirectShootScoreChance(wf, kickerPos, false) < 0.1)
		{
			return false;
		}
		
		if (redirectAngle <= OffensiveConstants.getMaximumReasonableRedirectAngle())
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots)
	{
		return getBestRedirector(wFrame, bots, wFrame.getBall().getVel(), null);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param fakeBallVel
	 * @param tacticalField
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 fakeBallVel, final ITacticalField tacticalField)
	{
		if (fakeBallVel.getLength2() > 0.1)
		{
			IVector2 ballPos = wFrame.getBall().getPos();
			
			BotID minID = null;
			double minDist = Double.MAX_VALUE;
			
			List<BotID> filteredBots = getPotentialRedirectors(wFrame, bots, fakeBallVel, tacticalField);
			for (BotID key : filteredBots)
			{
				IVector2 pos = bots.get(key).getPos();
				if (GeoMath.distancePP(pos, ballPos) < minDist)
				{
					minDist = GeoMath.distancePP(pos, ballPos);
					minID = key;
				}
			}
			if (minID != null)
			{
				return minID;
			}
		}
		return null;
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param ballVel
	 * @return
	 */
	public static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 ballVel)
	{
		return getPotentialRedirectors(wFrame, bots, ballVel, null);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param ballVel
	 * @param tacticalField
	 * @return
	 */
	public static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 ballVel, final ITacticalField tacticalField)
	{
		List<BotID> filteredBots = new ArrayList<BotID>();
		if (ballVel.getLength2() > 0.1)
		{
			final double REDIRECT_TOLERANCE = 350;
			IVector2 ballPos = wFrame.getBall().getPos();
			
			IVector2 left = new Vector2(ballVel.getAngle() - 0.2).normalizeNew();
			IVector2 right = new Vector2(ballVel.getAngle() + 0.2).normalizeNew();
			
			TrackedBall ball = new TrackedBall(wFrame.getBall().getPos(), 10.0, ballVel, 0.0, null);
			IVector2 futureBall = ball.getPosByVel(0f);
			double dist = Math.max((GeoMath.distancePP(ballPos, futureBall) - REDIRECT_TOLERANCE), 10);
			
			IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
			IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
			
			DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 20));
			dtri.setFill(true);
			// DrawableCircle dc = new DrawableCircle(
			// new Circle(ballPos.addNew(ballVel.normalizeNew().multiplyNew(150f)), 200), new Color(255, 0, 0, 20));
			// dc.setFill(true);
			IVector2 normal = ballVel.getNormalVector().normalizeNew();
			IVector2 tleft = ballPos.addNew(normal.scaleToNew(160));
			IVector2 tright = ballPos.addNew(normal.scaleToNew(-160));
			IVector2 uleft = tleft.addNew(left.scaleToNew(dist)).addNew(normal.scaleToNew(100));
			IVector2 uright = tright.addNew(right.scaleToNew(dist)).addNew(normal.scaleToNew(-100));
			
			DrawableTriangle dtri3 = new DrawableTriangle(tleft, uleft, uright, new Color(255, 0, 0, 20));
			dtri3.setFill(true);
			
			DrawableTriangle dtri4 = new DrawableTriangle(tleft, tright, uright, new Color(255, 0, 0, 20));
			dtri4.setFill(true);
			
			for (BotID key : bots.keySet())
			{
				IVector2 pos = bots.get(key).getPos();
				// IVector2 kpos = bots.get(key).getBotKickerPos();
				if (dtri3.isPointInShape(pos) || dtri4.isPointInShape(pos) // || dc.isPointInShape(pos)
				/* || dtri.isPointInShape(kpos) || dc.isPointInShape(kpos) */)
				{
					filteredBots.add(key);
				}
				if (tacticalField != null)
				{
					DrawableCircle dcp = new DrawableCircle(pos, 150, Color.cyan);
					// tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc);
					if (filteredBots.contains(key))
					{
						tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dcp);
					}
					
					tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtri);
					tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtri3);
					tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtri4);
				}
			}
			if (tacticalField != null)
			{
				tacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE_FINDER).add(dtri);
			}
		}
		return filteredBots;
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @return
	 */
	public static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots)
	{
		return getPotentialRedirectors(wFrame, bots, wFrame.getBall().getVel());
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final TacticalField newTacticalField)
	{
		BotIDMap<ITrackedBot> potentialOffensiveBots = getPotentialOffensiveBotMap(newTacticalField, baseAiFrame);
		return getBestGetter(baseAiFrame, potentialOffensiveBots, newTacticalField);
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param bots
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final IBotIDMap<ITrackedBot> bots,
			final TacticalField newTacticalField)
	{
		KickSkillCalc calc = new KickSkillCalc(new DynamicPosition(Geometry.getGoalTheir().getGoalCenter()));
		IBotIDMap<ITrackedBot> potentialOffensiveBots = bots;
		double minScore = Double.MAX_VALUE;
		BotID bestBot = null;
		
		int numOfRedirectors = 0;
		double smallestDistRedirectorToBall = Double.MAX_VALUE;
		BotID bestRedirectorBot = null;
		
		for (BotID id : potentialOffensiveBots.keySet())
		{
			ITrackedBot bot = potentialOffensiveBots.get(id);
			IVector2 botPos = bot.getPos();
			double score = 0;
			double distToBall = 0;
			boolean isRedirectOrCatch = false;
			
			
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			
			String modifierInformation = "";
			if (baseAiFrame.getPrevFrame() != null)
			{
				if (baseAiFrame.getPrevFrame().getPlayStrategy() != null)
				{
					if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE) != null)
					{
						boolean idFoundInOffensive = false;
						for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE))
						{
							if ((role.getBotID() == id)
									&& (role.getCurrentState() != EOffensiveStrategy.SUPPORTIVE_ATTACKER))
							{
								// primary bot here
								score = newTacticalField.getKickSkillTimes().get(role.getBotID());
								modifierInformation += "isPrimary: -0.5;";
								score -= 0.5;
								isRedirectOrCatch = calc.getKickState().equals(EKickSkillState.CATCH);
								idFoundInOffensive = true;
							} else if (role.getBotID() == id)
							{
								// secondary bot here
								score = newTacticalField.getKickSkillTimes().get(role.getBotID());
								score -= 0.15;
								modifierInformation += "isSecondary: -0.5;";
								isRedirectOrCatch = calc.getKickState().equals(EKickSkillState.CATCH);
								idFoundInOffensive = true;
							}
						}
						if (!idFoundInOffensive)
						{
							// generate trajectory here
							score = newTacticalField.getKickSkillTimes().get(id);
							isRedirectOrCatch = calc.getKickState().equals(EKickSkillState.CATCH);
							idFoundInOffensive = true;
						}
						
						// this is a really ugly quick fix
						if (newTacticalField.getGameState().equals(EGameStateTeam.PREPARE_KICKOFF_WE)
								&& (GeoMath.distancePP(botPos, baseAiFrame.getWorldFrame().getBall().getPos()) < 300))
						{
							score -= 5;
							modifierInformation += "kickoff: -5.0;";
						}
					}
				}
			}
			
			double rawscore = newTacticalField.getKickSkillTimes().get(id);
			
			DrawableText dt = new DrawableText(botPos.addNew(new Vector2(-30, -25)),
					"Score -> " + df.format(rawscore) + " " + modifierInformation + " = " + df.format(score),
					Color.black);
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE_FINDER).add(dt);
			
			if (isRedirectOrCatch && (distToBall < smallestDistRedirectorToBall))
			{
				if ((smallestDistRedirectorToBall - distToBall) < 50)
				{
					if (score < minScore)
					{
						bestRedirectorBot = id;
						smallestDistRedirectorToBall = distToBall;
					} else
					{
						bestRedirectorBot = bestBot;
						smallestDistRedirectorToBall = distToBall;
					}
				} else
				{
					smallestDistRedirectorToBall = distToBall;
					bestRedirectorBot = id;
					numOfRedirectors++;
				}
			}
			
			if (score < minScore)
			{
				minScore = score;
				bestBot = id;
			}
		}
		
		// choose if bestRedirector is better, then bestGetter
		// bestRedirector is only reasonable when more then 3 bots want to redirect the ball.
		
		if ((numOfRedirectors >= 2) && (bestRedirectorBot != null)
				&& (baseAiFrame.getWorldFrame().getBall().getVel().getLength() > OffensiveConstants
						.getAcceptBestCatcherBallSpeedTreshold()))
		{
			return bestRedirectorBot;
		}
		
		return bestBot;
	}
	
	
	/**
	 * @param passSenderPos (most of the time, this will be the ballPos)
	 * @param passReceiverPos receiving robot
	 * @param target redirecting target of the receiving robot.
	 * @return
	 */
	public static double calcPassSpeedForReceivers(final IVector2 passSenderPos, final IVector2 passReceiverPos,
			final IVector2 target)
	{
		double defaultEndVel = OffensiveConstants.getDefaultPassEndVel();
		IVector2 targetToReceiver = passReceiverPos.subtractNew(target);
		IVector2 senderToReceiver = passReceiverPos.subtractNew(passSenderPos);
		double distance = senderToReceiver.getLength();
		
		double add = 0;
		if (distance > OffensiveConstants.getMinDistanceForSpeedAddition())
		{
			double rate = (Math.min(OffensiveConstants.getMaxDistanceForSpeedAddition(), distance)
					- OffensiveConstants.getMinDistanceForSpeedAddition())
					/ (OffensiveConstants.getMaxDistanceForSpeedAddition()
							- OffensiveConstants.getMinDistanceForSpeedAddition());
			add += rate * OffensiveConstants.getDistanceSpeedAddition();
		}
		
		
		double angle = GeoMath.angleBetweenVectorAndVector(targetToReceiver, senderToReceiver) * AngleMath.RAD_TO_DEG;
		
		if (angle > (AngleMath.RAD_TO_DEG * OffensiveConstants.getMaximumReasonableRedirectAngle()))
		{
			return OffensiveConstants.getDefaultPassEndVelReceive() + add;
		}
		
		double mFull = OffensiveConstants.getMaxAngleforPassMaxSpeed();
		double mRed = OffensiveConstants.getMaxAngleForReducedSpeed();
		if (angle < mFull)
		{
			return defaultEndVel + add;
		} else if (angle < 100)
		{
			return Math.max(
					defaultEndVel
							- (OffensiveConstants.getPassSpeedReductionForBadAngles() *
									((angle - mFull) / (mRed - mFull))),
					0.5) + add;
		}
		// Receiver will probably catch the ball, not redirect
		return defaultEndVel + add;
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param newTacticalField
	 * @return
	 */
	public static boolean isKeeperInsane(final BaseAiFrame baseAiFrame, final ITacticalField newTacticalField)
	{
		switch (newTacticalField.getGameState())
		{
			case THROW_IN_WE:
			case CORNER_KICK_WE:
			case DIRECT_KICK_WE:
				
				if (OffensiveConstants.isEnableInsanityMode()
						&& (baseAiFrame.getWorldFrame().getBall().getPos().x() > ((Geometry.getFieldLength() / 2) - 250)))
				{
					return true;
				}
				return false;
			
			default:
				return false;
			
		}
	}
}
