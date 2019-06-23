/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.algorithms.ExtensiveFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.algorithms.interfaces.IFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * This class provides a calculator to calculate important points to defend versus the foe bots.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ZoneDefenseCalc extends ACalculator
{
	
	// private static final Logger log = Logger.getLogger(DefenseCalc.class.getName());
	
	// private final int pointDrawRadius = 50;
	
	private final IFoeBotCalc	foeBotCalc;
	
	@Configurable(comment = "negative xValue of the ball to set double defenders")
	private static double		doubleDefXValue						= -Geometry
			.getFieldLength() / 2.0 / 3.0;
	
	@Configurable(comment = "Range: (0, pi / 2.0): If the angle between ball and x axis is greater than this value only one defender will try to block the ball")
	private static double		blockAngleDoubleDefenders			= 0.7;
	
	@Configurable(comment = "Minimum distance of a defender to the ball")
	private static double		minDistanceDef2Ball					= 1.5
			* Geometry.getBotRadius();
	
	@Configurable(comment = "Minimum distance of a defender to a foe bot")
	private static double		minDistanceDef2Foe					= 2.1
			* Geometry.getBotRadius();
	
	@Configurable(comment = "maximum abs y position the defenders are allowed to drive to")
	private static double		yLimitDefenders						= (Geometry
			.getFieldWidth() / 2.)
			- Geometry.getBotRadius();
	
	@Configurable(comment = "Ball blocking defenders will not drive behind this x value")
	private static double		maxXDirectShotDefender				= -(Geometry
			.getCenterCircleRadius()
			+ (1.5f * Geometry.getBotRadius()));
	
	@Configurable(comment = "Bot blocking defenders will not drive behind this x value")
	private static double		maxXBotBlockingDefender				= -(1. / 3.) * Geometry.getFieldLength();
	
	@Configurable(comment = "Bot blocking defenders will not drive behind this x value during kickoff")
	private static double		maxXBotBlockingDefenderKickOff	= -(Geometry
			.getCenterCircleRadius()
			+ (1.5f * Geometry.getBotRadius()));
	
	
	/**
	 * @return the maxXBotBlockingDefenderKickOff
	 */
	public static double getMaxXBotBlockingDefenderKickOff()
	{
		return maxXBotBlockingDefenderKickOff;
	}
	
	
	@Configurable(comment = "If the angle between ball, foe and goal is larger the angle of the defender will be adapted to cover our goal")
	private static double	zoneDefenseAngleCorrection		= Math.PI / 2.;
	
	
	@Configurable(comment = "If a foe bot is nearer than this distance to the ball during a clearing, the clearing will be executed as fast as possible")
	private static double	foe2ballClearingPanicDistance	= 6 * Geometry.getBotRadius();
	
	
	/**
	 * 
	 */
	public ZoneDefenseCalc()
	{
		foeBotCalc = new ExtensiveFoeBotCalc();
	}
	
	
	/**
	 * @return the foe2ballClearingPanicDistance
	 */
	public static double getFoe2ballClearingPanicDistance()
	{
		return foe2ballClearingPanicDistance;
	}
	
	
	/**
	 * @return
	 */
	public static double getDoubleDefXValue()
	{
		
		return doubleDefXValue;
	}
	
	
	/**
	 * @return
	 */
	public static double getYLimitDefenders()
	{
		
		return yLimitDefenders;
	}
	
	
	/**
	 * @return
	 */
	public static double getBlockAngleDoubleDefenders()
	{
		
		return blockAngleDoubleDefenders;
	}
	
	
	/**
	 * @return
	 */
	public static double getMinDistanceDef2Ball()
	{
		
		return minDistanceDef2Ball;
	}
	
	
	/**
	 * @return
	 */
	public static double getMinDistanceDef2Foe()
	{
		
		return minDistanceDef2Foe;
	}
	
	
	/**
	 * @return
	 */
	public static double getMaxXDirectShotDefender()
	{
		
		return maxXDirectShotDefender;
	}
	
	
	/**
	 * @return
	 */
	public static double getMaxXBotBlockingDefender()
	{
		
		return maxXBotBlockingDefender;
	}
	
	
	/**
	 * @return
	 */
	public static double getZoneDefenseAngleCorrection()
	{
		
		return zoneDefenseAngleCorrection;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		double pointDrawRadius = 50;
		
		List<FoeBotData> foeBotDataList = foeBotCalc.getFoeBotData(newTacticalField, baseAiFrame);
		foeBotDataList.sort(FoeBotData.DANGER_COMPARATOR);
		
		newTacticalField.getDangerousFoeBots().addAll(foeBotDataList);
		
		// drawings
		for (FoeBotData curData : foeBotDataList)
		{
			if (null != curData.getBot2goal())
			{
				ILine line = new Line(curData.getFoeBot().getPos(), curData.getBot2goal());
				IDrawableShape drawableLine = new DrawableLine(line, Color.DARK_GRAY);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableLine);
			}
			
			IDrawableShape drawableCircle = new DrawableCircle(new Circle(curData.getBot2goalNearestToBot(),
					pointDrawRadius), Color.CYAN);
			IDrawableShape drawableCircle2 = new DrawableCircle(new Circle(curData.getBot2goalNearestToGoal(),
					pointDrawRadius), Color.BLUE);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle2);
			
			if (!curData.getBall2bot().isZeroVector())
			{
				ILine line2 = new Line(curData.getFoeBot().getPos(), curData.getBall2bot().multiplyNew(-1f));
				IDrawableShape drawableLine2 = new DrawableLine(line2, Color.GRAY);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableLine2);
			}
			IDrawableShape drawableCircle3 = new DrawableCircle(new Circle(curData.getBall2botNearestToBall(),
					pointDrawRadius), Color.PINK);
			IDrawableShape drawableCircle4 = new DrawableCircle(new Circle(curData.getBall2botNearestToBot(),
					pointDrawRadius), Color.MAGENTA);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle3);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle4);
			
			for (IVector2 point : curData.getBot2goalIntersecsBot2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.GREEN);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBot2goalIntersecsBall2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.LIGHT_GRAY);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBall2botIntersecsBot2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.MAGENTA);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBall2botIntersecsBot2goal())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.ORANGE);
				newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
		}
	}
}
