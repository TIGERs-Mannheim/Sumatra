/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.EPathDriver;
import edu.tigers.sumatra.skillsystem.driver.HermiteSplinePathDriver;
import edu.tigers.sumatra.skillsystem.driver.IPathDriver;
import edu.tigers.sumatra.skillsystem.driver.LongPathDriver;
import edu.tigers.sumatra.skillsystem.driver.MixedPathDriver;
import edu.tigers.sumatra.skillsystem.driver.PathPointDriver;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AMoveToSkill extends AMoveSkill
{
	@Configurable
	private static EPathDriver	defaultPathDriver	= EPathDriver.MIXED_SPLINE_POS;
																
	@Configurable
	private static EMoveToType	moveToType			= EMoveToType.DEFAULT;
																
	private enum EMoveToType
	{
		DEFAULT,
		TRAJECTORY,
		V2
	}
	
	
	/**
	 * 
	 */
	protected AMoveToSkill(final ESkill skill)
	{
		super(skill);
	}
	
	
	/**
	 * Create the configured default MoveToSkill
	 * 
	 * @return
	 */
	public static AMoveToSkill createMoveToSkill()
	{
		switch (moveToType)
		{
			case DEFAULT:
				return new MoveToSkill();
			case TRAJECTORY:
				return new MoveToTrajSkill();
			case V2:
				return new MoveToV2Skill();
			default:
				throw new IllegalStateException();
		}
	}
	
	
	protected final IPathDriver getPathDriver(final IPath path)
	{
		switch (defaultPathDriver)
		{
			case HERMITE_SPLINE:
				return (new HermiteSplinePathDriver(getTBot(), path));
			case MIXED_SPLINE_POS:
				return (new MixedPathDriver(new HermiteSplinePathDriver(getTBot(), path),
						new PathPointDriver(path, getMoveCon()), getMoveCon().getDestination()));
			case MIXED_LONG_POS:
				return (new MixedPathDriver(new LongPathDriver(getTBot(), path),
						new PathPointDriver(path, getMoveCon()), getMoveCon().getDestination()));
			case PATH_POINT:
				return (new PathPointDriver(path, getMoveCon()));
			case LONG:
				return (new LongPathDriver(getTBot(), path));
			default:
				throw new IllegalStateException();
		}
	}
}
