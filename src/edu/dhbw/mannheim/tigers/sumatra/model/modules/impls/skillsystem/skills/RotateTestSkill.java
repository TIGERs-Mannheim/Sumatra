/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Rotate with angle. Multiples of 2*PI are possible.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RotateTestSkill extends AMoveSkill
{
	/** rad */
	private final float	angle;
	
	
	/**
	 * @param angle [rad]
	 */
	public RotateTestSkill(final float angle)
	{
		super(ESkillName.ROTATE);
		
		this.angle = angle;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(final List<ACommand> cmds)
	{
		List<IVector2> path = new LinkedList<IVector2>();
		path.add(getPos());
		
		SplineTrajectoryGenerator gen = createDefaultGenerator(getBotType());
		SplinePair3D result = createSplineWithoutDrivingIt(path, getAngle(), gen);
		
		HermiteSpline rotateSpline = gen.generateSpline(getAngle(), getAngle() + angle, 0, 0, 0.1f, false);
		List<HermiteSpline> rotateParts = new ArrayList<HermiteSpline>();
		rotateParts.add(rotateSpline);
		
		result.setRotationTrajectory(new HermiteSplineTrajectory1D(rotateParts));
		setNewTrajectory(result, path);
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(final List<ACommand> cmds)
	{
	}
	
	
	@Override
	protected boolean isMoveComplete()
	{
		return super.isMoveComplete();
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
}
