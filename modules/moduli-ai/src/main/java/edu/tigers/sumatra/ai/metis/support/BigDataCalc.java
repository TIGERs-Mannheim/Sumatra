/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.support;

import java.util.LinkedList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import Jama.Matrix;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.KernelMath;
import edu.tigers.sumatra.math.KernelMath.EKernelFunction;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


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
														
	private final long	botPositions[]		= new long[numX * numY];
	private final long	ballPositions[]	= new long[numX * numY];
														
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
		
		filter = KernelMath.createSquareKernel(EKernelFunction.GAUSS, 5, Geometry.getFieldLength()
				/ (numX - 1), Geometry.getFieldWidth() / (numY - 1));
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		frameCount++;
		
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			if (Geometry.getField().isPointInShape(bot.getPos())
					&& goalChance(bot.getBotKickerPos(), baseAiFrame.getWorldFrame()))
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
		
		// if (Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos())
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
		
		double normBotPositions[] = KernelMath.filter(filter, botPositions, numX, numY);
		for (int i = 0; i < (numX * numY); i++)
		{
			normBotPositions[i] = normBotPositions[i] / maxScoreChance;
		}
		
		newTacticalField.getDrawableShapes().get(EShapesLayer.BIG_DATA)
				.add(new ValuedField(normBotPositions, numX, numY, 0, Geometry.getFieldLength(), Geometry.getFieldWidth()));
				
	}
	
	
	private boolean goalChance(final IVector2 position, final WorldFrame worldFrame)
	{
		List<BotID> ignoreBots = new LinkedList<>(worldFrame.getTigerBotsAvailable().keySet());
		
		return AiMath.isGoalVisible(worldFrame, Geometry.getGoalTheir(), position, ignoreBots);
	}
	
	
	private IVector2 getDiscretePosition(final IVector2 position)
	{
		double stepX = Geometry.getFieldLength() / (numX - 1);
		double stepY = Geometry.getFieldWidth() / (numY - 1);
		
		double x = Math.round((position.x() + (0.5f * Geometry.getFieldLength())) / stepX);
		double y = Math.round((position.y() + (0.5f * Geometry.getFieldWidth())) / stepY);
		
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
		double stepX = Geometry.getFieldLength() / (numX - 1);
		double stepY = Geometry.getFieldWidth() / (numY - 1);
		
		double x = (index.x() * stepX) - (0.5f * Geometry.getFieldLength());
		double y = (index.y() * stepY) - (0.5f * Geometry.getFieldWidth());
		
		return new Vector2(x, y);
	}
}
