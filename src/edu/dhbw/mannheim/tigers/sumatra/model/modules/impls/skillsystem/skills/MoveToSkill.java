/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.IPathConsumer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.filter.HermiteSplinePathFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.filter.StubPathFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.iba.IBAFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This skills asks sysiphus to create a path, makes a spline out of it and then follows the spline.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToSkill extends AMoveSkill implements IPathConsumer
{
	private Sisyphus				sisyphus		= null;
	private IPath					latestPath	= null;
	private IPath					currentPath	= null;
	
	@Configurable(comment = "Path filter to use to filter new pathes: HERMITE_SPLINE, NONE")
	private static EPathFilter	pathFilter	= EPathFilter.HERMITE_SPLINE;
	
	private enum EPathFilter
	{
		HERMITE_SPLINE,
		NONE
	}
	
	@Configurable(comment = "Pathfinder to use to generate paths: ERRT, IBA")
	private static EPathFinder	pathFinder	= EPathFinder.ERRT;
	
	private enum EPathFinder
	{
		ERRT,
		IBA
	}
	
	
	/**
	 * Move to a target with an orientation as specified in the moveCon.
	 */
	protected MoveToSkill()
	{
		this(ESkillName.MOVE_TO);
	}
	
	
	/**
	 * Move to a target with an orientation as specified in the moveCon.
	 */
	protected MoveToSkill(final float forcedTimeToDestination)
	{
		super(ESkillName.MOVE_TO, forcedTimeToDestination);
	}
	
	
	protected MoveToSkill(final ESkillName skillName)
	{
		super(skillName);
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		sisyphus = new Sisyphus(getBot().getBotID(), getMoveCon());
		switch (pathFilter)
		{
			case HERMITE_SPLINE:
				getSisyphus().setPathFilter(new HermiteSplinePathFilter());
				break;
			case NONE:
				getSisyphus().setPathFilter(new StubPathFilter());
				break;
			default:
				throw new IllegalStateException();
		}
		switch (pathFinder)
		{
			case ERRT:
				getSisyphus().setPathFinder(new ERRTFinder());
				break;
			case IBA:
				getSisyphus().setPathFinder(new IBAFinder());
				break;
			default:
				throw new IllegalStateException();
		}
		getSisyphus().addObserver(this);
		getSkillSystem().getPathFinderScheduler().start(getSisyphus());
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
		getSisyphus().removeObserver(this);
		getSkillSystem().getPathFinderScheduler().stop(getSisyphus());
	}
	
	
	@Override
	public void onNewPath(final IPath path)
	{
		currentPath = path;
		setPathDriver(getDefaultPathDriver(path));
		setNewPathCounter(getNewPathCounter() + 1);
	}
	
	
	@Override
	public void onPotentialNewPath(final IPath path)
	{
		latestPath = path;
	}
	
	
	@Override
	public DrawablePath getDrawablePath()
	{
		DrawablePath dp = super.getDrawablePath();
		dp.setPath(currentPath);
		getSisyphus().getPathFilter().getDrawableShapes(dp.getPathShapes());
		return dp;
	}
	
	
	@Override
	public final DrawablePath getLatestDrawablePath()
	{
		DrawablePath dp = super.getLatestDrawablePath();
		dp.setPath(latestPath);
		return dp;
	}
	
	
	/**
	 * @return the sisyphus
	 */
	public final Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	
	
	/**
	 * @param timeToDestination
	 */
	public void setForcedTimeToDestination(final float timeToDestination)
	{
		forcedTimeToDestination = timeToDestination;
	}
}
