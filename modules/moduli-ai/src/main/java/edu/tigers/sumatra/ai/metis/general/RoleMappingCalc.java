/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Find number of roles and preferred bots for all roles
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RoleMappingCalc extends ACalculator
{
	private EnumMap<EPlay, Color> colorMap;
	
	
	/**
	 * Default
	 */
	public RoleMappingCalc()
	{
		int alpha = 200;
		colorMap = new EnumMap<>(EPlay.class);
		colorMap.put(EPlay.DEFENSIVE, new Color(108, 209, 255, alpha));
		colorMap.put(EPlay.KEEPER, new Color(255, 0, 218, alpha));
		colorMap.put(EPlay.OFFENSIVE, new Color(255, 50, 8, alpha));
		colorMap.put(EPlay.SUPPORT, new Color(80, 80, 80, alpha));
		colorMap.put(EPlay.INTERCHANGE, new Color(255, 150, 0, alpha));
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame aiFrame)
	{
		Map<EPlay, RoleMapping> roleFinderInfoMap = new EnumMap<>(EPlay.class);
		Map<EPlay, Set<BotID>> desiredRoles = newTacticalField.getDesiredBotMap();
		
		for (Map.Entry<EPlay, Set<BotID>> entry : desiredRoles.entrySet())
		{
			RoleMapping info = mapToRoleFinderInfo(entry.getKey(), newTacticalField);
			roleFinderInfoMap.put(entry.getKey(), info);
		}
		
		newTacticalField.setRoleMapping(roleFinderInfoMap);
		
		generateRoleShapes(newTacticalField);
	}
	
	
	private RoleMapping mapToRoleFinderInfo(final EPlay play, final TacticalField tacticalField)
	{
		RoleMapping roleMapping = new RoleMapping();
		roleMapping.getDesiredBots()
				.addAll(tacticalField.getDesiredBotMap().getOrDefault(play, Collections.emptySet()));
		
		return roleMapping;
	}
	
	
	private void generateRoleShapes(final TacticalField newTacticalField)
	{
		List<IDrawableShape> roleColorShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_ROLE_COLOR);
		for (EPlay play : newTacticalField.getDesiredBotMap().keySet())
		{
			Set<BotID> botIDS = newTacticalField.getDesiredBotMap().get(play);
			if (!colorMap.containsKey(play))
			{
				continue;
			}
			for (BotID bot : botIDS)
			{
				ITrackedBot tBot = getWFrame().getBot(bot);
				if (tBot != null)
				{
					DrawableCircle botShape = new DrawableCircle(
							Circle.createCircle(tBot.getPos(), Geometry.getBotRadius() + 50), colorMap.get(play));
					botShape.setFill(true);
					roleColorShapes.add(botShape);
				}
			}
		}
	}
}
