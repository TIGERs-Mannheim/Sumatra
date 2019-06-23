/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.offense.AOffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRoleKickStateState implements IState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	protected static final Logger				log							= Logger.getLogger(AOffensiveRole.class.getName());
	private static final double				ANTI_TOGGLE_BONUS			= 0.12;
	private static final double				PROTECTION_MODE_SWITCH	= 0.35;
	private final OffensiveRoleKickState	parent;

	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * @param role the Offensive Role
	 */
	public AOffensiveRoleKickStateState(final OffensiveRoleKickState role)
	{
		parent = role;
	}
	
	
	/**
	 * @return get the current movePosition
	 */
	public abstract IVector2 getDestination();
	
	
	protected void setNewSkill(final ISkill newSkill)
	{
		parent.setNewSkill(newSkill);
	}
	
	
	protected void triggerInnerEvent(final EKickStateEvent event)
	{
		parent.triggerInnerEvent(event);
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
		AnimatedCrosshair crosshair = AnimatedCrosshair.aCrazyCrosshair(target, 50, 100, 1.0f, Color.RED,
				new Color(255, 0, 0, 40), new Color(255, 0, 0, 140));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(crosshair);
		
		DrawableLine line = new DrawableLine(Line.fromPoints(getWFrame().getBall().getPos(), target),
				getBotID().getTeamColor().getColor());
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING).add(line);
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
	public OffensiveRoleKickState getParent()
	{
		return parent;
	}
	
	
	protected boolean isProtectionRequired(final OffensiveAction.EOffensiveAction currentAction)
	{
		long dif = getWFrame().getTimestamp() - getAiFrame().getAICom().getProtectionInitTime();
		if ((dif * 1e-9) > 10)
		{
			// if protectionInit time is older than xx seconds. reset it.
			getAiFrame().getAICom().setProtectionPenalty(0);
			getAiFrame().getAICom().setProtectionInitTime(0);
		}
		BotDistance closestEnemy = getAiFrame().getTacticalField().getEnemyClosestToBall();
		boolean isRequired = isProtectionRequired(getAiFrame().getPrevFrame().getAICom().getProtectionPenalty(), 0);
		boolean extraConditon = (closestEnemy.getDist() > (Geometry.getBotRadius() * 4))
				&& (closestEnemy.getDist() < 2500);
		
		String text = "";
		boolean failed = false;
		if (getAiFrame().getGamestate().isStandardSituationForUs())
		{
			failed = true;
			text += "GameState | ";
		}
		if (currentAction == OffensiveAction.EOffensiveAction.GOAL_SHOT)
		{
			failed = true;
			text += "Goal_Shot | ";
		}
		if (getWFrame().getBall().getVel().getLength2() > 0.5)
		{
			failed = true;
			text += "Vel | ";
		}
		if (getPos().distanceTo(getWFrame().getBall().getPos()) > 400)
		{
			failed = true;
			text += "distToBall | ";
		}
		if (Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos(), 500))
		{
			failed = true;
			text += "PenArea | ";
		}
		if (!Geometry.getField().isPointInShape(getWFrame().getBall().getPos(), -300))
		{
			failed = true;
			text += "FieldBorder | ";
		}
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 70), text,
				Color.red);
		dt.setFontSize(12);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PROTECTION).add(dt);
		
		return !failed && extraConditon && isRequired;
	}
	
	
	/**
	 * Protection is required when.... some general conditions... and
	 *
	 * @param penalty
	 * @param antiTogglingBonus
	 * @return
	 */
	private boolean isProtectionRequired(final double penalty, final double antiTogglingBonus)
	{
		double via = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID()).getViability();
		String text = String.format("%.2f", via) + " - " + antiTogglingBonus + " < " + PROTECTION_MODE_SWITCH + " - "
				+ String.format("%.2f", penalty);
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 50), "ProtectionMode stats: " + text,
				Color.red);
		dt.setFontSize(12);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PROTECTION).add(dt);
		return (via - antiTogglingBonus) < (PROTECTION_MODE_SWITCH - penalty);
	}
	
	
	protected boolean isProtecionNotRequiredAnymore()
	{
		// time gets set to zero on new initialization
		// this happens when another bot
		if (getAiFrame().getAICom().getProtectionInitTime() == 0)
		{
			getAiFrame().getAICom().setProtectionInitTime(getWFrame().getTimestamp());
		}
		long dif = getWFrame().getTimestamp() - getAiFrame().getAICom().getProtectionInitTime();
		double activeSeconds = (dif) * 1e-9;
		double penalty = 0.15 * activeSeconds; // every second 0.15 penalty !
		getAiFrame().getAICom().setProtectionPenalty(penalty);
		return !isProtectionRequired(getAiFrame().getAICom().getProtectionPenalty(), ANTI_TOGGLE_BONUS);
	}
}

