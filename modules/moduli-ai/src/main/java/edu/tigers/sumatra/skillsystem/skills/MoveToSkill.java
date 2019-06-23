/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.sisyphus.IPathConsumer;
import edu.tigers.sumatra.ai.sisyphus.Sisyphus;
import edu.tigers.sumatra.ai.sisyphus.errt.ERRTFinder;
import edu.tigers.sumatra.ai.sisyphus.filter.HermiteSplinePathFilter;
import edu.tigers.sumatra.ai.sisyphus.filter.StubPathFilter;
import edu.tigers.sumatra.ai.sisyphus.finder.iba.IBAFinder;
import edu.tigers.sumatra.drawable.DrawablePath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.statemachine.IState;


/**
 * This skills asks sysiphus to create a path, makes a spline out of it and then follows the spline.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToSkill extends AMoveToSkill implements IPathConsumer
{
	private Sisyphus				sisyphus		= null;
														
														
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
	
	
	/**
	 * Move to a target with an orientation as specified in the moveCon.
	 */
	protected MoveToSkill()
	{
		this(ESkill.MOVE_TO);
	}
	
	
	protected MoveToSkill(final ESkill skillName)
	{
		super(skillName);
		setInitialState(new DefaultState());
	}
	
	
	@Override
	public void onNewPath(final IPath path)
	{
		setPathDriver(getPathDriver(path));
		List<IDrawableShape> shapes = new ArrayList<>(1);
		shapes.add(new DrawablePath(path.getPathPoints()));
		shapes.add(new DrawablePath(path.getUnsmoothedPathPoints()));
		getPathDriver().setShapes(EShapesLayer.PATH, shapes);
	}
	
	
	@Override
	public void onPotentialNewPath(final IPath path)
	{
		List<IDrawableShape> shapes = new ArrayList<>(1);
		shapes.add(new DrawablePath(path.getPathPoints()));
		shapes.add(new DrawablePath(path.getUnsmoothedPathPoints()));
		getPathDriver().setShapes(EShapesLayer.PATH_LATEST, shapes);
	}
	
	
	/**
	 * @return the sisyphus
	 */
	public final Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	
	
	private GenericSkillSystem getSkillSystem()
	{
		try
		{
			return (GenericSkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			// if this happens, a NPE will be raised later which should point out the problem clearly as well
		}
		return null;
	}
	
	private enum EStateId
	{
		DEFAULT
	}
	
	private class DefaultState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			sisyphus = new Sisyphus(getBot().getBotId(), getMoveCon());
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
			getSisyphus().addObserver(MoveToSkill.this);
			getSkillSystem().getPathFinderScheduler().start(getSisyphus());
		}
		
		
		@Override
		public void doExitActions()
		{
			getSisyphus().removeObserver(MoveToSkill.this);
			getSkillSystem().getPathFinderScheduler().stop(getSisyphus());
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.DEFAULT;
		}
		
	}
}
