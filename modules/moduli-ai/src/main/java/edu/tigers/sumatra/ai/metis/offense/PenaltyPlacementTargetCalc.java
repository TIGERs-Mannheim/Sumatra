/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.PenaltyPlacementTargetGroup;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Calculates a set of positions where to place the ball during PenaltyShootout.
 * Does nothing in other GameStates.
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PenaltyPlacementTargetCalc extends ACalculator
{
	
	@Configurable(comment = "x-pos to align new groups to", defValue = "3000")
	private static double placementBaseX = 3000.0;
	
	@Configurable(comment = "Number of groups to generate in the first row", defValue = "4")
	private static int groups = 4;
	
	@Configurable(comment = "Number of rows to generate", defValue = "2")
	private static int groupRows = 2;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return tacticalField.getGameState().isPenaltyShootout();
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		List<PenaltyPlacementTargetGroup> placementTargetGroups = calculateTargetGroups();
		List<PenaltyPlacementTargetGroup> prevTargetGroups = aiFrame.getPrevFrame().getTacticalField()
				.getPenaltyPlacementTargetGroups();
		
		double groupDistance = Geometry.getFieldWidth() / groups;
		
		for (PenaltyPlacementTargetGroup prevGroup : prevTargetGroups)
		{
			for (int i = 0; i < placementTargetGroups.size(); i++)
			{
				if (placementTargetGroups.get(i).center().isCloseTo(prevGroup.center(), groupDistance / 2))
				{
					placementTargetGroups.set(i, prevGroup);
					break;
				}
			}
		}
		
		tacticalField.setPenaltyPlacementTargetGroups(placementTargetGroups);
		drawGroups(tacticalField, placementTargetGroups);
	}
	
	
	private List<PenaltyPlacementTargetGroup> calculateTargetGroups()
	{
		
		List<PenaltyPlacementTargetGroup> placementTargetGroups = new ArrayList<>();
		double groupDistance = Geometry.getFieldWidth() / groups;
		
		int tempGroups = groups;
		for (int g = 0; g < groupRows; g++)
		{
			for (int i = 0; i < tempGroups; i++)
			{
				
				double gDiff = g % 2 != 0 ? (groupDistance / 2) : 0;
				
				IVector2 center = Vector2.fromXY(placementBaseX - (g * groupDistance * 0.75),
						-(Geometry.getFieldWidth() / 2) + (groupDistance / 2) + i * groupDistance + gDiff);
				placementTargetGroups.add(new PenaltyPlacementTargetGroup(center, groupDistance / 2));
			}
			
			tempGroups = g % 2 == 0 ? tempGroups - 1 : tempGroups + 1;
		}
		
		return placementTargetGroups;
	}
	
	
	private void drawGroups(TacticalField tacticalField, List<PenaltyPlacementTargetGroup> groups)
	{
		final List<IDrawableShape> shapes = tacticalField.getDrawableShapes()
				.get(EAiShapesLayer.PENALTY_PLACEMENT_GROUPS);
		for (PenaltyPlacementTargetGroup group : groups)
		{
			shapes.add(new DrawableCircle(group, Color.ORANGE));
			shapes.add(new DrawableAnnotation(group.center(),
					String.format("A: %1$d | S: %2$d", group.getAttempts(), group.getSuccessfulAttempts()), true));
		}
	}
}
