/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.AOffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRoleState implements IState
{
	protected static final Logger log = Logger.getLogger(AOffensiveRole.class.getName());
	private final OffensiveRole parent;
	
	
	/**
	 * @param role the Offensive Role
	 */
	public AOffensiveRoleState(final OffensiveRole role)
	{
		parent = role;
	}
	
	
	public void setNewSkill(final ISkill newSkill)
	{
		parent.setNewSkill(newSkill);
	}
	
	
	protected void triggerEvent(final IEvent event)
	{
		parent.triggerEvent(event);
	}
	
	
	public AthenaAiFrame getAiFrame()
	{
		return parent.getAiFrame();
	}
	
	
	public WorldFrame getWFrame()
	{
		return parent.getWFrame();
	}
	
	
	public IVector2 getPos()
	{
		return parent.getPos();
	}
	
	
	public BotID getBotID()
	{
		return parent.getBotID();
	}
	
	
	public ITrackedBot getBot()
	{
		return parent.getBot();
	}
	
	
	public ISkill getCurrentSkill()
	{
		return parent.getCurrentSkill();
	}
	
	
	protected void visualizeTarget(final IVector2 target)
	{
		AnimatedCrosshair crosshair = AnimatedCrosshair.aCrazyCrosshair(target, 50, 100, 1.0f, Color.RED,
				new Color(255, 0, 0, 40), new Color(255, 0, 0, 140));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(crosshair);
	}
	
	
	/**
	 * @param actionTarget shoot target
	 * @param passTargetID this bot gets ignored by calculations.
	 * @return
	 */
	protected final boolean calcIsChip(final IVector2 actionTarget, final BotID passTargetID)
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
		BotIDMap<ITrackedBot> bots = new BotIDMap<>(getWFrame().getFoeBots());
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
			ICircle c1 = Circle.createCircle(bot.getPos(), Geometry.getBotRadius());
			DrawableCircle dc1 = new DrawableCircle(c1, Color.black);
			if (triangle1.getTriangle().isPointInShape(bot.getPos())
					|| triangle2.getTriangle().isPointInShape(bot.getPos()))
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ADDITIONAL)
						.add(new DrawableAnnotation(getPos().addNew(Vector2.fromXY(0, -200)),
								"dist to Target: " + VectorMath.distancePP(getWFrame().getBall().getPos(), actionTarget),
								Color.orange));
				if (VectorMath.distancePP(getWFrame().getBall().getPos(), actionTarget) > OffensiveConstants
						.getChipKickMinDistToTarget())
				{
					chip = true;
					break;
				}
			}
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dc1);
		}
		
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(triangle1);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(triangle2);
		return chip;
	}
	
	
	/**
	 * @return shootTarget of offensive role, is null when no target is set yet
	 */
	public IVector2 getTarget()
	{
		return null;
	}
	
	
	/**
	 * @return get Parent OffensiveRole of this state
	 */
	public OffensiveRole getParent()
	{
		return parent;
	}
	
	
	/**
	 * @return The current move Position of the offensiveRole
	 */
	public abstract IVector2 getMoveDest();
}
