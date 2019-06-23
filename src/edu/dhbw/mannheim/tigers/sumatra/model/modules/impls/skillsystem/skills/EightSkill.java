/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 30, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * drive an eight
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class EightSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float	size;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param size
	 */
	public EightSkill(float size)
	{
		super(ESkillName.EIGHT);
		this.size = size;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void periodicProcess(List<ACommand> cmds)
	{
		
	}
	
	
	@Override
	protected List<ACommand> doCalcEntryActions(List<ACommand> cmds)
	{
		List<IVector2> nodes = new ArrayList<IVector2>(10);
		nodes.add(getPos());
		nodes.add(getPos().addNew(new Vector2(size / 2, size / 2)));
		nodes.add(getPos().addNew(new Vector2(size, 0)));
		nodes.add(getPos().addNew(new Vector2(size / 2, -size / 2)));
		nodes.add(getPos());
		nodes.add(getPos().addNew(new Vector2(-size / 2, size / 2)));
		nodes.add(getPos().addNew(new Vector2(-size, 0)));
		nodes.add(getPos().addNew(new Vector2(-size / 2, -size / 2)));
		nodes.add(getPos());
		createSpline(nodes, 0);
		
		return cmds;
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
