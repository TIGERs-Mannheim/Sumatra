/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.errt.ERRTFinder;
import edu.tigers.sumatra.ai.sisyphus.filter.HermiteSplinePathFilter;
import edu.tigers.sumatra.ai.sisyphus.filter.IPathFilter;
import edu.tigers.sumatra.ai.sisyphus.filter.StubPathFilter;
import edu.tigers.sumatra.ai.sisyphus.finder.IPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.StubPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.iba.IBAFinder;
import edu.tigers.sumatra.drawable.DrawablePath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.EPathDriver;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToV2Skill extends AMoveToSkill
{
	private IPath					currentPath	= null;
														
	@Configurable(comment = "Path filter to use to filter new pathes: HERMITE_SPLINE, NONE")
	private static EPathFilter	pathFilter	= EPathFilter.HERMITE_SPLINE;
														
	private enum EPathFilter
	{
		HERMITE_SPLINE,
		NONE
	}
	
	@Configurable(comment = "Pathfinder to use to generate paths: ERRT, IBA")
	private static EPathFinder pathFinder = EPathFinder.ERRT;
	
	private enum EPathFinder
	{
		ERRT,
		IBA
	}
	
	@Configurable
	private static EPathDriver defaultPathDriver = EPathDriver.MIXED_SPLINE_POS;
	
	
	/**
	 */
	protected MoveToV2Skill()
	{
		super(ESkill.MOVE_TO_V2);
		setInitialState(new DefaultState());
	}
	
	
	private enum EStateId
	{
		DEFAULT
	}
	
	private class DefaultState implements IState
	{
		PathFinderInput	localPathFinderInput;
		IPathFilter			filter;
		IPathFinder			finder;
								
								
		@Override
		public void doEntryActions()
		{
			localPathFinderInput = new PathFinderInput(getBotId(), getMoveCon());
			switch (pathFilter)
			{
				case HERMITE_SPLINE:
					filter = new HermiteSplinePathFilter();
					break;
				case NONE:
					filter = new StubPathFilter();
					break;
				default:
					throw new IllegalStateException();
			}
			switch (pathFinder)
			{
				case ERRT:
					finder = new ERRTFinder();
					break;
				case IBA:
					finder = new IBAFinder();
					break;
				default:
					finder = new StubPathFinder();
					throw new IllegalStateException();
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			localPathFinderInput.getFieldInfo().updateWorldFrame(getWorldFrame());
			IPath newPath = finder.calcPath(localPathFinderInput);
			if ((currentPath == null)
					|| filter.accept(localPathFinderInput, newPath, currentPath))
			{
				currentPath = newPath;
				setPathDriver(getPathDriver(currentPath));
				List<IDrawableShape> shapes = new ArrayList<>(2);
				shapes.add(new DrawablePath(currentPath.getPathPoints()));
				shapes.add(new DrawablePath(currentPath.getUnsmoothedPathPoints()));
				getPathDriver().setShapes(EShapesLayer.PATH, shapes);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.DEFAULT;
		}
	}
}
