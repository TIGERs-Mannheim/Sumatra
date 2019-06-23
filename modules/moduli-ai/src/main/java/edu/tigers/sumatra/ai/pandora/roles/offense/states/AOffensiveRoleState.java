/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.AOffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	private final OffensiveRole	parent;
	protected static final Logger	log		= Logger.getLogger(AOffensiveRole.class.getName());
	protected int						animator	= 0;
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * @param role the Offensive Role
	 */
	public AOffensiveRoleState(final OffensiveRole role)
	{
		parent = role;
	}
	
	
	protected void setNewSkill(final ISkill newSkill)
	{
		parent.setNewSkill(newSkill);
	}
	
	
	protected void triggerEvent(final Enum<? extends Enum<?>> event)
	{
		parent.triggerEvent(event);
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return parent.getAiFrame();
	}
	
	
	protected WorldFrame getWFrame()
	{
		return parent.getWFrame();
	}
	
	
	protected IVector2 getPos()
	{
		return parent.getPos();
	}
	
	
	protected BotID getBotID()
	{
		return parent.getBotID();
	}
	
	
	protected ITrackedBot getBot()
	{
		return parent.getBot();
	}
	
	
	protected ISkill getCurrentSkill()
	{
		return parent.getCurrentSkill();
	}
	
	
	protected void visualizeTarget(final IVector2 target)
	{
		animator++;
		Circle targetCircle = new Circle(target, 150 + (int) (Math.sin(animator / 5.0) * 100));
		Color bigTargetColor = new Color(50, 10, 125, 60);
		DrawableCircle dtargetCircle = new DrawableCircle(targetCircle, bigTargetColor);
		dtargetCircle.setFill(true);
		
		Circle targetCircle2 = new Circle(target, 50 + (int) (Math.sin(animator / 5.0) * 30));
		DrawableCircle dtargetCircle2 = new DrawableCircle(targetCircle2, new Color(10, 10, 200, 60));
		dtargetCircle2.setFill(true);
		DrawableCircle dtargetCircle3 = new DrawableCircle(targetCircle, bigTargetColor);
		
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtargetCircle);
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtargetCircle2);
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtargetCircle3);
	}
	
	
	final protected boolean calcIsChip(final IVector2 actionTarget, final BotID passTargetID)
	{
		IVector2 ballToTarget = actionTarget.subtractNew(getWFrame().getBall().getPos());
		if (ballToTarget.getLength2() > OffensiveConstants.getChipKickCheckDistance())
		{
			ballToTarget = ballToTarget.normalizeNew().multiplyNew(OffensiveConstants.getChipKickCheckDistance());
		}
		
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		
		IVector2 triB1 = getWFrame().getBall().getPos()
				.addNew(normal.scaleToNew(25 + Geometry.getBotRadius()));
		IVector2 triB2 = getWFrame().getBall().getPos()
				.addNew(normal.scaleToNew(-25 - Geometry.getBotRadius()));
		IVector2 triT1 = triB1.addNew(ballToTarget).addNew(
				normal.scaleToNew(100 + Geometry.getBotRadius()));
		IVector2 triT2 = triB1.addNew(ballToTarget).addNew(
				normal.scaleToNew(-100 - Geometry.getBotRadius()));
		
		DrawableTriangle triangle1 = new DrawableTriangle(triB1, triT1, triT2);
		DrawableTriangle triangle2 = new DrawableTriangle(triB1, triB2, triT2);
		triangle2.setColor(new Color(25, 40, 40, 125));
		triangle1.setColor(new Color(25, 40, 40, 125));
		triangle1.setFill(true);
		triangle2.setFill(true);
		BotIDMap<ITrackedBot> bots = new BotIDMap<ITrackedBot>(getWFrame().getFoeBots());
		bots.putAll(getWFrame().getTigerBotsVisible());
		bots.remove(getBotID());
		if (passTargetID != null)
		{
			bots.remove(passTargetID);
		}
		
		// determine if offensive has to chip.
		boolean chip = false;
		for (ITrackedBot bot : bots.values())
		{
			Circle c1 = new Circle(bot.getPos(), Geometry.getBotRadius());
			DrawableCircle dc1 = new DrawableCircle(c1, Color.black);
			if (triangle1.isPointInShape(bot.getPos()) || triangle2.isPointInShape(bot.getPos()))
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL)
						.add(new DrawableText(getPos().addNew(new Vector2(0, -200)),
								"dist to Target: " + GeoMath.distancePP(getWFrame().getBall().getPos(), actionTarget),
								Color.orange));
				if (GeoMath.distancePP(getWFrame().getBall().getPos(), actionTarget) > OffensiveConstants
						.getChipKickMinDistToTarget())
				{
					chip = true;
					break;
				}
			}
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc1);
		}
		
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(triangle1);
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(triangle2);
		return chip;
	}
}
