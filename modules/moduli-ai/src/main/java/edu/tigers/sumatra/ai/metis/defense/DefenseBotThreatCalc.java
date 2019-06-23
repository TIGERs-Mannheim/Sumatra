/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.BetaDistribution;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Rate opponent bot threats based on shooting angle and time to goal.
 * Calculations taken from: http://wiki.robocup.org/File:Small_Size_League_-_RoboCup_2016_-_TDP_CMDragons.pdf
 */
public class DefenseBotThreatCalc extends ACalculator
{
	@Configurable(comment = "maximum x value for a foe bot to 'be in our half' (mm)", defValue = "50.0")
	private static double minXValueOpp = 50.;
	
	@Configurable(comment = "Assumed vPass of the opponent (m/s)", defValue = "6.0")
	private static double vPassOpponent = 6.;
	
	@Configurable(comment = "Assumed vShot of the opponent (allowed are 10 m/s) (m/s)", defValue = "8.0")
	private static double vShotOpponent = 8.;
	
	@Configurable(comment = "If the goal angle is greater than this, use shot times instead of the angle to compare (rad)", defValue = "0.5")
	private static double maxAngleCompareAngle = 0.5;
	
	@Configurable(comment = "Use ER-Force 2017 ETDP threat rating.", defValue = "true")
	private static boolean useERForceRating = true;
	
	@Configurable(comment = "Activate the bot threat grid", defValue = "false")
	private static boolean isERForceBotThreatGridActivated = false;
	
	@Configurable(comment = "ER-Force volley shot threat weight", defValue = "5.0")
	private static double erVolleyAngleWeight = 5.0;
	
	@Configurable(comment = "ER-Force travel angle threat weight", defValue = "1.0")
	private static double erTravelAngleWeight = 1.0;
	
	@Configurable(comment = "ER-Force distance to goal weight", defValue = "1.0")
	private static double erDistToGoalWeight = 1.0;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<DefenseBotThreat> angleComparedThreats = new ArrayList<>();
		final List<DefenseBotThreat> timeComparedThreats = new ArrayList<>();
		
		final IVector2 ballPos = getBall().getPos();
		final IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		
		for (ITrackedBot bot : getWFrame().getFoeBots().values())
		{
			IVector2 botPos = bot.getPosByTime(DefenseConstants.getLookaheadBotThreats(bot.getVel().getLength()));
			
			if (botPos.x() > minXValueOpp)
			{
				continue;
			}
			
			if (useERForceRating)
			{
				double threatRating = getThreatRating(ballPos, goalCenter, botPos);
				
				DefenseBotThreat curThreat = new DefenseBotThreat(bot, threatRating, 0);
				angleComparedThreats.add(curThreat);
			} else
			{
				final double shootingAngle = calculateShootingAngle(botPos);
				final double tPass = calculateTPass(botPos, ballPos);
				final double tDeflect = calculateTDeflect(botPos, ballPos);
				final double tKick = calculateTKick(botPos);
				
				DefenseBotThreat curThreat = new DefenseBotThreat(bot, shootingAngle,
						tPass + tDeflect + tKick);
				
				if (shootingAngle > maxAngleCompareAngle)
				{
					timeComparedThreats.add(curThreat);
				} else
				{
					angleComparedThreats.add(curThreat);
				}
			}
		}
		
		angleComparedThreats.sort(Comparator.comparingDouble(DefenseBotThreat::getShootingAngle).reversed());
		timeComparedThreats.sort(Comparator.comparingDouble(DefenseBotThreat::getTGoal));
		
		timeComparedThreats.addAll(angleComparedThreats);
		
		newTacticalField.setDefenseBotThreats(timeComparedThreats);
		
		if (isERForceBotThreatGridActivated)
		{
			drawBotThreatGrid(newTacticalField, ballPos, goalCenter);
		}
		
		final List<IDrawableShape> defenseShapes = newTacticalField.getDrawableShapes()
				.get(EAiShapesLayer.DEFENSE_BOT_THREATS);
		
		drawBotThreads(timeComparedThreats, defenseShapes);
	}
	
	
	private void drawBotThreatGrid(final TacticalField newTacticalField, final IVector2 ballPos,
			final IVector2 goalCenter)
	{
		double width = Geometry.getFieldWidth();
		double height = Geometry.getFieldLength();
		
		int numX = 400;
		int numY = 200;
		
		List<Double> ratings = new ArrayList<>();
		
		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));
				
				ratings.add(getThreatRating(ballPos, goalCenter, Vector2.fromXY(x, y)));
			}
		}
		
		double maxValue = ratings.stream().sorted((a, b) -> (int) Math.signum(b - a)).findFirst()
				.orElseThrow(() -> new RuntimeException("Never!"));
		ratings = ratings.stream().map(r -> SumatraMath.relative(r, 0, maxValue)).collect(Collectors.toList());
		
		double[] ratingsArray = ratings.stream().mapToDouble(Double::doubleValue).toArray();
		
		ValuedField field = new ValuedField(ratingsArray, numX, numY, 0, height, width);
		
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.BOT_THREADS_GRIT).add(field);
	}
	
	
	private double getThreatRating(final IVector2 ballPos, final IVector2 target, final IVector2 passTarget)
	{
		BetaDistribution beta = new BetaDistribution(4, 6);
		
		final IVector2 botPos = passTarget;
		
		double distGoal = minXValueOpp - Geometry.getGoalOur().getCenter().x();
		double distanceToGoal = 1.0 - (Math.min(botPos.distanceTo(target), distGoal) / distGoal);
		
		IVector2 ballOpponent = Vector2.fromPoints(botPos, ballPos);
		IVector2 opponentGoal = Vector2.fromPoints(botPos, target);
		IVector2 ballGoal = Vector2.fromPoints(ballPos, target);
		IVector2 goalOpponent = Vector2.fromPoints(target, opponentGoal);
		
		double volleyAngle = ballOpponent.angleToAbs(opponentGoal).orElse(0.0) / Math.PI;
		volleyAngle = beta.density(volleyAngle) / 2.5; // scale to 1.0
		double travelAngle = ballGoal.angleToAbs(goalOpponent).orElse(0.0) / Math.PI;
		
		return ((volleyAngle * erVolleyAngleWeight) + (travelAngle * erTravelAngleWeight)
				+ (distanceToGoal * erDistToGoalWeight)) /
				(erVolleyAngleWeight + erTravelAngleWeight + erDistToGoalWeight);
	}
	
	
	private void drawBotThreads(final List<DefenseBotThreat> timeComparedThreats,
			final List<IDrawableShape> defenseShapes)
	{
		int threatId = 0;
		for (DefenseBotThreat threat : timeComparedThreats)
		{
			DrawableAnnotation angle = new DrawableAnnotation(threat.getPos(),
					String.format("-> %d <-%nAngle: %.2f%nTime: %.2f", threatId, threat.getShootingAngle(),
							threat.getTGoal()),
					Vector2.fromY(200));
			angle.setCenterHorizontally(true);
			defenseShapes.add(angle);
			
			int col = 255 - Math.min(255, threatId * 40);
			DrawableLine threatLine = new DrawableLine(threat.getThreatLine(), new Color(col, col, col));
			defenseShapes.add(threatLine);
			
			++threatId;
		}
	}
	
	
	/**
	 * @return shooting angle of the bot to our goal [0, PI]
	 */
	private double calculateShootingAngle(final IVector2 botPos)
	{
		final IVector2 postLeft = Geometry.getGoalOur().getLeftPost();
		final IVector2 postRight = Geometry.getGoalOur().getRightPost();
		
		final IVector2 bot2postLeft = postLeft.subtractNew(botPos);
		final IVector2 bot2postRight = postRight.subtractNew(botPos);
		
		return bot2postLeft.angleToAbs(bot2postRight).orElse(0.0);
	}
	
	
	private double calculateTPass(final IVector2 botPos, final IVector2 ballPos)
	{
		return ballPos.subtractNew(botPos).getLength2() / (vPassOpponent * 1000);
	}
	
	
	private double calculateTDeflect(final IVector2 botPos, final IVector2 ballPos)
	{
		return DefenseMath.calculateTDeflect(botPos, ballPos);
	}
	
	
	private double calculateTKick(final IVector2 botPos)
	{
		final IVector2 goalOurCenter = Geometry.getGoalOur().getCenter();
		
		return goalOurCenter.subtractNew(botPos).getLength2() / (vShotOpponent * 1000);
	}
}
