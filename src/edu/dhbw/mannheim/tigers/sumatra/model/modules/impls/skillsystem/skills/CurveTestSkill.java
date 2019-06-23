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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Tests turning spline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class CurveTestSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float			size;
	private final float			angle;
	private static final int	PATH_POINTS	= 3;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param size
	 * @param angle
	 */
	public CurveTestSkill(float size, float angle)
	{
		super(ESkillName.TURN);
		this.size = size;
		this.angle = angle;
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
		List<IVector2> nodes = new ArrayList<IVector2>(PATH_POINTS);
		
		for (int i = 0; i < PATH_POINTS; i++)
		{
			nodes.add(GeoMath.stepAlongCircle(getPos(), getPos().addNew(new Vector2(size, 0)),
					(((angle) / (PATH_POINTS - 1)) * i)));
		}
		
		createSpline(nodes, getPos().addNew(new Vector2(size, 0)));
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
