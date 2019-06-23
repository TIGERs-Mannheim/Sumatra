/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.PassReceiver;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class rates the prior generated SupportPositions
 */
public class SupportPositionRatingCalc extends ACalculator
{
	@Configurable(defValue = "0.5", comment = "Max time of interception")
	private static double maxTimeOfInterception = 0.5;
	
	@Configurable(defValue = "false", comment = "Fill the whole field with pass score")
	private static boolean drawWholeField = false;
	
	private List<IDrawableShape> fieldRatingShapes;
	private BotID passPlayerID;
	private AngleRangeRater targetRater;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		fieldRatingShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_FIELD_RATING);
		passPlayerID = newTacticalField.getOffensiveStrategy().getAttackerBot().orElse(getAiFrame().getKeeperId());
		
		targetRater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		targetRater.setObstacles(getWFrame().getFoeBots().values());
		targetRater.setStraightBallConsultant(getBall().getStraightConsultant());
		targetRater.setTimeToKick(0);
		
		IVector2 alternativeBallPos = getBall().getPos();
		rateSupportPositions(newTacticalField.getGlobalSupportPositions(), alternativeBallPos);
		drawWholeField();
	}
	
	
	@Override
	public boolean isCalculationNecessary(TacticalField tacticalField, BaseAiFrame aiFrame)
	{
		return PassReceiver.isActive();
	}
	
	
	private void rateSupportPositions(final List<SupportPosition> positions, final IVector2 passOrigin)
	{
		positions.forEach(pos -> pos.setShootScore(shootScore(pos)));
		// prefer shoot score over pass score by reducing pass score to at most 0.5
		positions.forEach(pos -> pos.setPassScore(0.5 * passScore(passOrigin, pos.getPos())));
	}
	
	
	private double shootScore(final SupportPosition supportPosition)
	{
		double tDeflect = DefenseMath.calculateTDeflect(supportPosition.getPos(), getBall().getPos(),
				DefenseMath.getBisectionGoal(getBall().getPos()));
		
		targetRater.setTimeToKick(tDeflect);
		return targetRater.rate(supportPosition.getPos()).map(IRatedTarget::getScore).orElse(0.0);
	}
	
	
	private double passScore(final IVector2 passOrigin, final IVector2 passTarget)
	{
		double passDistance = getWFrame().getBall().getPos().distanceTo(passTarget);
		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(getWFrame().getBots());
		obstacles.remove(passPlayerID);
		ChipKickReasonableDecider chipDecider = new ChipKickReasonableDecider(
				getWFrame().getBall().getPos(),
				passTarget,
				obstacles.values(),
				OffensiveMath.passSpeedChip(passDistance));
		
		List<ITrackedBot> consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperFoeId())
				.collect(Collectors.toList());
		
		if (chipDecider.isChipKickReasonable())
		{
			return PassInterceptionRater.rateChippedPass(passOrigin, passTarget, consideredBots);
		}
		return PassInterceptionRater.rateStraightPass(passOrigin, passTarget, consideredBots);
	}
	
	
	private void drawWholeField()
	{
		if (drawWholeField)
		{
			double width = Geometry.getFieldWidth();
			double height = Geometry.getFieldLength();
			int numX = 200;
			int numY = 150;
			List<SupportPosition> visPositions = new ArrayList<>();
			for (int iy = 0; iy < numY; iy++)
			{
				for (int ix = 0; ix < numX; ix++)
				{
					double x = (-height / 2) + (ix * (height / (numX - 1)));
					double y = (-width / 2) + (iy * (width / (numY - 1)));
					SupportPosition passTarget = new SupportPosition(Vector2.fromXY(x, y), getWFrame().getTimestamp());
					visPositions.add(passTarget);
				}
			}
			
			rateSupportPositions(visPositions, getBall().getPos());
			
			
			double[] data = visPositions.stream()
					.mapToDouble(p -> SumatraMath.relative(p.getPassScore(), -2, maxTimeOfInterception)).toArray();
			ValuedField field = new ValuedField(data, numX, numY, 0);
			fieldRatingShapes.add(field);
		}
	}
}
