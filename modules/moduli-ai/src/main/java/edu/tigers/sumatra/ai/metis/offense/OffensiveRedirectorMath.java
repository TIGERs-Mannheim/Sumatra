/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.PassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This class is used to move logic out of the RedirectKickState
 */
public class OffensiveRedirectorMath
{
	@Configurable(defValue = "1.4")
	private static double currenBotPassHysteresis = 1.4;
	
	@Configurable(defValue = "1.2")
	private static double currentBotGoalShotHysteresis = 1.2;
	
	@Configurable(defValue = "400")
	private static double currentBotDistHysteresis = 400;
	
	@Configurable(defValue = "1.2")
	private static double doublePassBonusRating = 1.2;
	
	@Configurable(defValue = "2500")
	private static double minDistForValidPass = 2500;
	
	@Configurable(defValue = "-800")
	private static double minXLine = -800;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", OffensiveRedirectorMath.class);
	}
	
	
	/**
	 * @param wf
	 * @param botMap
	 * @param passSenderBot
	 * @param newTacticalField
	 * @param currentPassReceiver
	 * @param baseAiFrame
	 * @return
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	public Optional<IPassTarget> calcBestRedirectPassTarget(WorldFrame wf, Map<BotID, ITrackedBot> botMap,
			ITrackedBot passSenderBot, ITacticalField newTacticalField,
			IPassTarget currentPassReceiver, final BaseAiFrame baseAiFrame)
	{
		double bestPassScore = 0;
		PassTarget passTarget = null;
		for (Map.Entry<BotID, ITrackedBot> passTargetBot : botMap.entrySet())
		{
			double hysteresisGoalScore = 1.0;
			double distance = passSenderBot.getBotKickerPos().distanceTo(passTargetBot.getValue().getBotKickerPos());
			boolean isPrimary = isPrimary(passSenderBot, baseAiFrame);
			
			Optional<IPassTarget> target = newTacticalField.getPassTargetsRanked().stream()
					.filter(e -> e.getBotId().equals(passTargetBot.getValue().getBotId()))
					.min(Comparator
							.comparingDouble(e -> e.getBotPos().distanceTo(passTargetBot.getValue().getBotKickerPos())));
			
			if (!target.isPresent())
			{
				continue;
			}
			
			IVector2 targetPos = target.get().getKickerPos();
			double targetScore = target.get().getScore();
			double passScore = calculateRedirectScoringChance(newTacticalField, wf.getBall().getPos(),
					passSenderBot.getBotKickerPos(), targetPos, isPrimary, targetScore);
			
			if (currentPassReceiver != null && passTargetBot.getKey().equals(currentPassReceiver.getBotId()))
			{
				passScore = passScore * currenBotPassHysteresis;
				distance += currentBotDistHysteresis; // anti toggle for current receiver
				hysteresisGoalScore = currentBotGoalShotHysteresis;
			}
			
			Optional<IRatedTarget> myGoalShotTarget = newTacticalField.getBestGoalKickTargetForBot()
					.get(passSenderBot.getBotId());
			Optional<IRatedTarget> receiverGoalShotTarget = newTacticalField.getBestGoalKickTargetForBot()
					.get(passSenderBot.getBotId());
			
			boolean badShootScoringOfReceiver = myGoalShotTarget.isPresent() && receiverGoalShotTarget.isPresent()
					&& myGoalShotTarget.get().getScore() > receiverGoalShotTarget.get().getScore() * hysteresisGoalScore;
			boolean isNotVisible = !isPassLineFree(passSenderBot.getPos(), targetPos,
					wf.getFoeBots().values());
			boolean isPassDistNotValid = distance < minDistForValidPass;
			boolean isDangerousBackPass = passTargetBot.getValue().getPos().x() < minXLine;
			boolean isValid = !isPassDistNotValid && !isNotVisible && !isDangerousBackPass;
			
			if (isValid && !badShootScoringOfReceiver && passScore > bestPassScore)
			{
				bestPassScore = passScore;
				passTarget = new PassTarget(targetPos, passTargetBot.getKey());
				
				// give PassScore to Action to make it comparable to a StandardPass, however. A Redirect Pass should
				// have a bit higher rating
				passTarget.setGoalKickScore(Math.min(1, targetScore * doublePassBonusRating));
			}
			drawShape(newTacticalField, passTargetBot, passScore);
		}
		return Optional.ofNullable(passTarget);
	}
	
	
	private void drawShape(final ITacticalField newTacticalField, final Map.Entry<BotID, ITrackedBot> passTargetBot,
			final double passScore)
	{
		DrawableAnnotation da = new DrawableAnnotation(passTargetBot.getValue().getPos(),
				String.format("valid: %.2f", passScore));
		da.withOffset(Vector2.fromY(-200));
		da.setColor(Color.MAGENTA);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_DOUBLE_PASS).add(da);
	}
	
	
	private boolean isPrimary(final ITrackedBot passSenderBot, final BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getPrevFrame() != null
				&& !baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getDesiredBots().isEmpty()
				&& baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getDesiredBots().iterator().next()
						.equals(passSenderBot.getBotId());
	}
	
	
	private boolean isPassLineFree(final IVector2 passSenderPos, final IVector2 target,
			final Collection<ITrackedBot> foeBots)
	{
		// better use some triangle here
		return AiMath.p2pVisibility(foeBots, passSenderPos, target, 500.0);
	}
	
	
	/**
	 * Calculate a redirect score combining the angle (chance of a successful redirect)
	 * with a scoring chance (how many defenders are in my way to score)
	 */
	private double calculateRedirectScoringChance(final ITacticalField newTacticalField, IVector2 ballPos,
			final IVector2 source, IVector2 redirectorKickerPos, final boolean isPrimary,
			double targetScore)
	{
		double redirectorAngle = OffensiveMath.getRedirectAngle(ballPos, source, redirectorKickerPos);
		double redirectAngleScore = 1 - SumatraMath.relative(redirectorAngle, AngleMath.deg2rad(40),
				OffensiveConstants.getMaximumReasonableRedirectAngle());
		
		double rating = targetScore * redirectAngleScore;
		if (isPrimary)
		{
			Color color = new Color((int) ((1 - rating) * 255), (int) (rating * 255), 0, 20);
			DrawableTriangle passTriangle = new DrawableTriangle(source, redirectorKickerPos, ballPos);
			passTriangle.setColor(color);
			passTriangle.setFill(true);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_DOUBLE_PASS).add(passTriangle);
		}
		return rating;
	}
	
	
	public static BotID getBestRedirector(
			final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots)
	{
		IVector2 endPos = wFrame.getBall().getTrajectory().getPosByVel(0).getXYVector();
		IVector2 ballPos = wFrame.getBall().getPos();
		
		BotID minID = null;
		double minDist = Double.MAX_VALUE;
		
		List<BotID> filteredBots = getPotentialRedirectors(wFrame, bots, endPos);
		for (BotID key : filteredBots)
		{
			IVector2 pos = bots.getWithNull(key).getPos();
			if (VectorMath.distancePP(pos, ballPos) < minDist)
			{
				minDist = VectorMath.distancePP(pos, ballPos);
				minID = key;
			}
		}
		if (minID != null)
		{
			return minID;
		}
		return BotID.noBot();
	}
	
	
	private static List<BotID> getPotentialRedirectors(
			final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots,
			final IVector2 endPos)
	{
		List<BotID> filteredBots = new ArrayList<>();
		final double redirectTol = 350;
		IVector2 ballPos = wFrame.getBall().getPos();
		
		// input: endpoint, ballVel.vel = endpoint - curPos.getAngle().
		IVector2 ballVel = endPos.subtractNew(ballPos);
		
		if (ballVel.getLength() < 0.4)
		{
			// no potential redirector
			return filteredBots;
		}
		
		IVector2 left = Vector2.fromAngle(ballVel.getAngle() - 0.2).normalizeNew();
		IVector2 right = Vector2.fromAngle(ballVel.getAngle() + 0.2).normalizeNew();
		
		double dist = Math.max(VectorMath.distancePP(ballPos, endPos) - redirectTol, 10);
		
		IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
		IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
		
		DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 20));
		dtri.setFill(true);
		IVector2 normal = ballVel.getNormalVector().normalizeNew();
		IVector2 tleft = ballPos.addNew(normal.scaleToNew(160));
		IVector2 tright = ballPos.addNew(normal.scaleToNew(-160));
		IVector2 uleft = tleft.addNew(left.scaleToNew(dist)).addNew(normal.scaleToNew(100));
		IVector2 uright = tright.addNew(right.scaleToNew(dist)).addNew(normal.scaleToNew(-100));
		
		DrawableTriangle dtri3 = new DrawableTriangle(tleft, uleft, uright, new Color(255, 0, 0, 20));
		dtri3.setFill(true);
		
		DrawableTriangle dtri4 = new DrawableTriangle(tleft, tright, uright, new Color(255, 0, 0, 20));
		dtri4.setFill(true);
		
		for (Map.Entry<BotID, ITrackedBot> entry : bots.entrySet())
		{
			BotID botID = entry.getKey();
			ITrackedBot tBot = entry.getValue();
			if (tBot == null)
			{
				continue;
			}
			IVector2 pos = tBot.getPos();
			if (dtri3.getTriangle().isPointInShape(pos) || dtri4.getTriangle().isPointInShape(pos))
			{
				filteredBots.add(botID);
			}
		}
		return filteredBots;
	}
}
