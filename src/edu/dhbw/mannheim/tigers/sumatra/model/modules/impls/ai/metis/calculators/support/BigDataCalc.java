/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.LinkedList;
import java.util.List;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.KernelMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.KernelMath.EKernelFunction;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * Computes a heat map of goal score chances based on data collected during a match.
 * Highly experimental, maybe this is useful to obtain valuable knowledge, create an
 * efficient support position calculator or somehow enforce advantageous situations.
 * 
 * @author JulianT
 */
public class BigDataCalc extends ACalculator
{
	@Configurable(comment = "X axis resolution")
	private static int	numX					= 88;
	
	@Configurable(comment = "Y axis resolution")
	private static int	numY					= 66;
	
	private long			botPositions[]		= new long[numX * numY];
	private long			ballPositions[]	= new long[numX * numY];
	
	long						frameCount			= 0;
	long						totalScoreChances	= 0;
	long						maxScoreChance		= 0;
	
	Matrix					filter;
	
	
	/**
	 * Constructor. Initializes both grids with zero.
	 */
	public BigDataCalc()
	{
		for (int i = 0; i < (numX * numY); i++)
		{
			botPositions[i] = 0;
			ballPositions[i] = 0;
		}
		
		filter = KernelMath.createSquareKernel(EKernelFunction.GAUSS, 5, AIConfig.getGeometry().getFieldLength()
				/ (numX - 1), AIConfig.getGeometry().getFieldWidth() / (numY - 1));
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		frameCount++;
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			if (AIConfig.getGeometry().getField().isPointInShape(bot.getPos())
					&& goalChance(AiMath.getBotKickerPos(bot), baseAiFrame.getWorldFrame()))
			{
				IVector2 botPos = getDiscretePosition(bot.getPos());
				long scoreChance = ++botPositions[(int) (botPos.x() + (botPos.y() * numX))];
				totalScoreChances++;
				
				if (scoreChance > maxScoreChance)
				{
					maxScoreChance = scoreChance;
				}
			}
		}
		
		// if (AIConfig.getGeometry().getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos())
		// && goalChance(baseAiFrame.getWorldFrame().getBall().getPos(), baseAiFrame.getWorldFrame()))
		// {
		// IVector2 ballPos = getDiscretePosition(baseAiFrame.getWorldFrame().getBall().getPos());
		// long scoreChance = ballPositions[(int) (ballPos.x() + (ballPos.y() * numX))];
		// totalScoreChances++;
		//
		// if (scoreChance > maxScoreChance)
		// {
		// maxScoreChance = scoreChance;
		// }
		// }
		
		float normBotPositions[] = KernelMath.filter(filter, botPositions, numX, numY);
		for (int i = 0; i < (numX * numY); i++)
		{
			normBotPositions[i] = normBotPositions[i] / maxScoreChance;
		}
		
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.BIG_DATA)
				.add(new ValuedField(normBotPositions, numX, numY, 0));
		
	}
	
	
	private boolean goalChance(final IVector2 position, final WorldFrame worldFrame)
	{
		List<BotID> ignoreBots = new LinkedList<>(worldFrame.getTigerBotsAvailable().keySet());
		
		return AiMath.isGoalVisible(worldFrame, AIConfig.getGeometry().getGoalTheir(), position, ignoreBots);
	}
	
	
	private IVector2 getDiscretePosition(final IVector2 position)
	{
		float stepX = AIConfig.getGeometry().getFieldLength() / (numX - 1);
		float stepY = AIConfig.getGeometry().getFieldWidth() / (numY - 1);
		
		float x = Math.round((position.x() + (0.5f * AIConfig.getGeometry().getFieldLength())) / stepX);
		float y = Math.round((position.y() + (0.5f * AIConfig.getGeometry().getFieldWidth())) / stepY);
		
		return new Vector2(x, y);
	}
	
	
	/**
	 * Converts from grid index to field position
	 * 
	 * @param index Grid index
	 * @return Position on the field
	 */
	public static IVector2 getFieldPosition(final IVector2 index)
	{
		float stepX = AIConfig.getGeometry().getFieldLength() / (numX - 1);
		float stepY = AIConfig.getGeometry().getFieldWidth() / (numY - 1);
		
		float x = (index.x() * stepX) - (0.5f * AIConfig.getGeometry().getFieldLength());
		float y = (index.y() * stepY) - (0.5f * AIConfig.getGeometry().getFieldWidth());
		
		return new Vector2(x, y);
	}
}
