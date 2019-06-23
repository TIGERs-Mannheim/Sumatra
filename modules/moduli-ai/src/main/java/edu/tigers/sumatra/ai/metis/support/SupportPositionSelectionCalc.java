/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.ai.metis.support.SupportPositionGenerationCalc.getMinSupporterDistance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * This class selects the support positions with respect to the pass and goal score value
 */
public class SupportPositionSelectionCalc extends ACalculator
{
	
	@Configurable(defValue = "2", comment = "Number of offensive Positions")
	private static int numberOfOffensivePositions = 2;
	
	@Configurable(defValue = "3", comment = "Number of defensive Positions")
	private static int numberOfPassPositions = 3;
	
	@Configurable(defValue = "-0.1", comment = "Min interception time for offensive Positons")
	private static double minOffensiveInterceptionTime = -0.1;
	
	private List<IDrawableShape> shapes;
	private List<IDrawableShape> debugShapes;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		
		shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_SELECTION);
		debugShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_SELECTION_DEBUG);
		
		List<SupportPosition> positions = new ArrayList<>();
		for (SupportPosition pos : newTacticalField.getGlobalSupportPositions())
		{
			SupportPosition newPos = new SupportPosition(pos.getPos(), pos.getBirth());
			newPos.setPassScore(pos.getPassScore());
			newPos.setShootScore(pos.getShootScore());
			positions.add(newPos);
		}
		
		List<SupportPosition> nonOffensivePositions = positions.stream()
				.filter(p -> p.getPassScore() < minOffensiveInterceptionTime).collect(Collectors.toList());
		positions.removeAll(nonOffensivePositions);
		positions.sort(SupportPosition::compareShootScoreWith);
		positions.addAll(nonOffensivePositions);
		List<SupportPosition> selectedPositions = grepGoodPositions(positions, numberOfOffensivePositions);
		selectedPositions.forEach(p -> p.setShootPosition(true));
		
		positions.sort(SupportPosition::comparePassScoreWith);
		selectedPositions.addAll(grepGoodPositions(positions, numberOfPassPositions));
		
		drawGlobalPositions(selectedPositions);
		newTacticalField.setSelectedSupportPositions(selectedPositions);
	}
	
	
	/**
	 * greps good positions out of sorted list without violation of distance
	 *
	 * @param sortedPositions first element is best
	 * @param numberOfPositions
	 */
	private List<SupportPosition> grepGoodPositions(List<SupportPosition> sortedPositions, int numberOfPositions)
	{
		List<SupportPosition> chosenPositions = new ArrayList<>();
		int numberAvailablePositions = sortedPositions.size();
		while (numberAvailablePositions > 0)
		{
			SupportPosition currentPosition = sortedPositions.get(0);
			chosenPositions.add(currentPosition);
			Iterator<SupportPosition> iterator = sortedPositions.iterator();
			while (iterator.hasNext())
			{
				if (iterator.next().isNearTo(currentPosition, getMinSupporterDistance()))
				{
					iterator.remove();
					numberAvailablePositions--;
				}
			}
			if (chosenPositions.size() >= numberOfPositions)
			{
				break;
			}
		}
		return chosenPositions;
	}
	
	
	private void drawGlobalPositions(List<SupportPosition> positions)
	{
		Color passColor = Color.GREEN;
		Color shootColor = Color.RED;
		positions.forEach(pos -> shapes.add(
				new DrawableCircle(pos.getPos(), Geometry.getBotRadius(), pos.isShootPosition() ? shootColor : passColor)));
		
		for (SupportPosition pos : positions)
		{
			String passScore = "Pass:" + String.format("%.2f", pos.getPassScore());
			String shootScore = "Shoot:" + String.format("%.2f", pos.getShootScore());
			
			if (pos.isShootPosition())
			{
				shootScore += "<-";
			} else
			{
				passScore += "<-";
			}
			
			String score = passScore + "\n" + shootScore;
			
			DrawableAnnotation dTxtScores = new DrawableAnnotation(pos.getPos(),
					score,
					Color.black);
			dTxtScores.setFontHeight(50);
			dTxtScores.setCenterHorizontally(true);
			dTxtScores.setOffset(Vector2.fromXY(0, 35));
			debugShapes.add(dTxtScores);
		}
	}
	
	
	public static int getNumberOfOffensivePositions()
	{
		return numberOfOffensivePositions;
	}
	
	
	public static int getNumberOfPassPositions()
	{
		return numberOfPassPositions;
	}
}
