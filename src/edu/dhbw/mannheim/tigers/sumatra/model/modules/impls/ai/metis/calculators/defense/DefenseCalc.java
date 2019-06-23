/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.ExtensiveFoeBotCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.interfaces.IFoeBotCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * This class provides a calculator to calculate important points to defend versus the foe bots.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DefenseCalc extends ACalculator
{
	// private static final Logger log = Logger.getLogger(DefenseCalc.class.getName());
	
	private int						pointDrawRadius						= 50;
	
	private final IFoeBotCalc	foeBotCalc;
	
	/**  */
	@Configurable(comment = "Double defender at kickoff")
	public static boolean		doubleDefKickoffOn					= false;
	
	/**  */
	@Configurable(comment = "Force the double block to retreat to the penalty area at kickoff")
	public static boolean		forceDoubleBlockAtPenAreaKickoff	= false;
	
	@Configurable(comment = "Distance to marker")
	private static float			distance2Marker						= 200;
	@Configurable
	private static float			distance2MarkerOffenseNear			= 500;
	
	@Configurable(comment = "Additional offset to penaltyArea margin")
	private static float			penaltyMarginOffset					= 70;
	
	/**  */
	@Configurable(comment = "Time [s] - the position of the future ball will be used to determine the danger from the ball position")
	public static float			ballLookaheadOfDefenders			= 0.5f;
	
	@Configurable(comment = "negative xValue of the ball to set double defenders")
	private static float			doubleDefXValue						= -AIConfig.getGeometry().getFieldLength() / 2 / 3;
	
	@Configurable(comment = "Range: (0, pi / 2): If the angle between ball and x axis is greater than this value only one defender will try to block the ball")
	private static float			blockAngle								= 0.7f;
	
	
	/**
	 * 
	 */
	public DefenseCalc()
	{
		foeBotCalc = new ExtensiveFoeBotCalc();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<FoeBotData> foeBotDataList = foeBotCalc.getFoeBotData(newTacticalField, baseAiFrame);
		
		newTacticalField.setDangerousFoeBots(foeBotDataList);
		
		if (((newTacticalField.getGameState().equals(EGameState.PREPARE_KICKOFF_THEY) && (doubleDefKickoffOn || forceDoubleBlockAtPenAreaKickoff)))
				|| ((baseAiFrame.getWorldFrame().getBall().getPosByTime(ballLookaheadOfDefenders).x() < doubleDefXValue)
				&& !(Math.abs(GeoMath.angleBetweenXAxisAndLine(AIConfig.getGeometry().getGoalOur().getGoalCenter(),
						baseAiFrame.getWorldFrame().getBall().getPosByTime(ballLookaheadOfDefenders))) > blockAngle)))
		{
			newTacticalField.setNeedTwoForBallBlock(true);
		} else
		{
			newTacticalField.setNeedTwoForBallBlock(false);
		}
		
		// drawings
		for (FoeBotData curData : foeBotDataList)
		{
			if (null != curData.getBot2goal())
			{
				ILine line = new Line(curData.getFoeBot().getPos(), curData.getBot2goal());
				IDrawableShape drawableLine = new DrawableLine(line, Color.DARK_GRAY);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableLine);
			}
			
			IDrawableShape drawableCircle = new DrawableCircle(new Circle(curData.getBot2goalNearestToBot(),
					pointDrawRadius), Color.CYAN);
			IDrawableShape drawableCircle2 = new DrawableCircle(new Circle(curData.getBot2goalNearestToGoal(),
					pointDrawRadius), Color.BLUE);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle2);
			
			if (!curData.getBall2bot().isZeroVector())
			{
				ILine line2 = new Line(curData.getFoeBot().getPos(), curData.getBall2bot().multiplyNew(-1f));
				IDrawableShape drawableLine2 = new DrawableLine(line2, Color.GRAY);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableLine2);
			}
			IDrawableShape drawableCircle3 = new DrawableCircle(new Circle(curData.getBall2botNearestToBall(),
					pointDrawRadius), Color.PINK);
			IDrawableShape drawableCircle4 = new DrawableCircle(new Circle(curData.getBall2botNearestToBot(),
					pointDrawRadius), Color.MAGENTA);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle3);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(drawableCircle4);
			
			for (IVector2 point : curData.getBot2goalIntersecsBot2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.GREEN);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBot2goalIntersecsBall2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.LIGHT_GRAY);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBall2botIntersecsBot2bot())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.MAGENTA);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
			
			for (IVector2 point : curData.getBall2botIntersecsBot2goal())
			{
				IDrawableShape circle = new DrawableCircle(new Circle(point, pointDrawRadius), Color.ORANGE);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE_ADDITIONAL).add(circle);
			}
		}
		
		
		// draw a line from the ball to the center of the goal and to each goal post
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		IVector2 leftPost = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
		IVector2 rightPost = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
		IVector2 bisectorBall2Posts = GeoMath.calculateBisector(ballPos, leftPost, rightPost);
		
		DrawableLine ballToGoalCenter = new DrawableLine(Line.newLine(ballPos, bisectorBall2Posts), Color.RED);
		DrawableLine ballToLeftPost = new DrawableLine(Line.newLine(ballPos, AIConfig.getGeometry().getGoalOur()
				.getGoalPostLeft()), Color.RED);
		DrawableLine ballToRightPost = new DrawableLine(Line.newLine(ballPos, AIConfig.getGeometry().getGoalOur()
				.getGoalPostRight()), Color.RED);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE).add(ballToGoalCenter);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE).add(ballToLeftPost);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.DEFENSE).add(ballToRightPost);
	}
	
	
	/**
	 * @return
	 */
	public static float getPenaltyAreaMargin()
	{
		return Geometry.getPenaltyAreaMargin() + penaltyMarginOffset;
	}
	
}
