/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;


/**
 * Simple straight move that times out after a specified time.
 * Mainly for testing purposes.
 * 
 * @author AndreR
 */
public class StraightMoveSkill extends PositionSkill
{
	/** mm */
	private final int		distance;
	/** rad */
	private final float	angle;
	
	
	/**
	 * @param distance [mm]
	 * @param angle [rad]
	 */
	public StraightMoveSkill(final int distance, final float angle)
	{
		super(ESkillName.STRAIGHT_MOVE);
		
		this.distance = distance;
		this.angle = angle;
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		setDestination(getPos().addNew(new Vector2(getAngle() + angle).multiply(distance)));
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
}
