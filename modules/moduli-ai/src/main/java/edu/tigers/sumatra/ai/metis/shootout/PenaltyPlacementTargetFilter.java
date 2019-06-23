/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.shootout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;


/**
 * Filters the generated groups to get the best one
 * 
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PenaltyPlacementTargetFilter extends ACalculator
{
	private Random rnd = null;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (rnd == null)
		{
			rnd = new Random(getAiFrame().getWorldFrame().getTimestamp());
		}
		
		return !tacticalField.getPenaltyPlacementTargetGroups().isEmpty();
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (tacticalField.getGameState().isStoppedGame())
		{
			tacticalField.setFilteredPenaltyPlacementTargetGroup(null);
			return;
		}
		
		PenaltyPlacementTargetGroup penaltyPlacementTargetGroup;
		if (aiFrame.getPrevFrame().getTacticalField().getFilteredPenaltyPlacementTargetGroup() != null)
		{
			penaltyPlacementTargetGroup = aiFrame.getPrevFrame().getTacticalField()
					.getFilteredPenaltyPlacementTargetGroup();
		} else
		{
			
			List<PenaltyPlacementTargetGroup> placementTargetGroups = new ArrayList<>(
					tacticalField.getPenaltyPlacementTargetGroups());
			Collections.shuffle(placementTargetGroups);
			List<PenaltyPlacementTargetGroup> sortedTargetGroups = placementTargetGroups.stream()
					.sorted(Comparator.comparing(PenaltyPlacementTargetGroup::calculateScore).reversed())
					.limit(1).collect(Collectors.toList());
			
			penaltyPlacementTargetGroup = sortedTargetGroups.get(rnd.nextInt(sortedTargetGroups.size()));
		}
		
		tacticalField.getDrawableShapes().get(EAiShapesLayer.PENALTY_PLACEMENT_GROUPS)
				.add(new DrawableCircle(penaltyPlacementTargetGroup, Color.CYAN));
		tacticalField.setFilteredPenaltyPlacementTargetGroup(penaltyPlacementTargetGroup);
	}
}
