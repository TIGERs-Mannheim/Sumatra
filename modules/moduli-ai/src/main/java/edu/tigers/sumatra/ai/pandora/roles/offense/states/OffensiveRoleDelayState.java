/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import java.awt.Color;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleDelayState extends AOffensiveRoleState
{
	private long initialTime = 0;
	private AMoveToSkill move = null;
	
	
	/**
	 * @param role
	 */
	public OffensiveRoleDelayState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		return move.getMoveCon().getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.DELAY.name();
	}
	
	
	@Override
	public void doEntryActions()
	{
		move = AMoveToSkill.createMoveToSkill();
		setNewSkill(move);
		initialTime = getWFrame().getTimestamp();
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 dir = Geometry.getGoalTheir().getCenter().subtractNew(getWFrame().getBall().getPos()).scaleToNew(-300);
		if (dir.isZeroVector())
		{
			dir = Vector2.fromXY(-300, 0);
		}
		IVector2 movePos = getWFrame().getBall().getPos().addNew(
				dir);
		
		move.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
		move.getMoveCon().updateDestination(movePos);
		double delayTime = OffensiveConstants.getDelayWaitTime();
		if (OffensiveMath.isKeeperInsane(getAiFrame(), getAiFrame().getTacticalField()))
		{
			delayTime *= 2.5;
		}
		
		if ((getWFrame().getTimestamp() - initialTime) > delayTime || endDelayEarly())
		{
			getAiFrame().getAICom().setResponded(true);
		}
		DrawableAnnotation dt = new DrawableAnnotation(getWFrame().getBall().getPos().addNew(Vector2.fromXY(-500, 0)),
				"delay: " + ((getWFrame().getTimestamp() - initialTime) / (1000 * 1000)), Color.orange);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINDER).add(dt);
	}
	
	
	private boolean endDelayEarly()
	{
		return getAiFrame().getGamestate().isDirectFreeForUs()
				&& getPos().distanceTo(getWFrame().getBall().getPos()) < 400
				&& OffensiveMath.willBotShoot(getWFrame());
	}
}
