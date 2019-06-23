/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.ITrajPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.ITrajPathDriver;
import edu.tigers.sumatra.skillsystem.driver.TrajPathDriverV2;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToTrajSkill extends AMoveToSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(MoveToTrajSkill.class.getName());
	
	private ITrajPathFinder			finder		= new TrajPathFinderV4();
	private TrajPathFinderInput	finderInput	= null;
	private ObstacleGenerator		obstacleGen	= new ObstacleGenerator();
	private ITrajPathDriver			driver		= new TrajPathDriverV2();
	
	
	@Configurable
	private static boolean			debug			= false;
	
	
	/**
	 * 
	 */
	public MoveToTrajSkill()
	{
		super(ESkill.MOVE_TO_TRAJ);
		setPathDriver(driver);
		setInitialState(new DefState());
	}
	
	
	/**
	 * @param skillname
	 */
	public MoveToTrajSkill(final ESkill skillname)
	{
		super(skillname);
		setPathDriver(driver);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		
		if (finderInput == null)
		{
			finderInput = new TrajPathFinderInput(getWorldFrame().getTimestamp());
		}
		finderInput.setDebug(debug);
		finderInput.setMoveCon(getMoveCon());
		
		if (getMoveCon().isArmChip())
		{
			getMatchCtrl().setKick(8, EKickerDevice.CHIP, EKickerMode.ARM);
		} else
		{
			getMatchCtrl().setKick(0, EKickerDevice.CHIP, EKickerMode.DISARM);
		}
		
		obstacleGen.setUseBall(getMoveCon().isBallObstacle());
		obstacleGen.setUseTheirBots(getMoveCon().isTheirBotsObstacle());
		obstacleGen.setUseOurBots(getMoveCon().isOurBotsObstacle());
		obstacleGen.setUsePenAreaOur(!getMoveCon().isPenaltyAreaAllowedOur());
		obstacleGen.setUsePenAreaTheir(!getMoveCon().isPenaltyAreaAllowedTheir());
		obstacleGen.setUseGoalPostsOur(getMoveCon().isGoalPostObstacle());
		
		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(),
				getMoveCon().getPrioMap());
		obstacles.addAll(
				obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(), getRefereeMsg(), getGameState()));
		List<IDrawableShape> shapes = new ArrayList<>(obstacles);
		
		finderInput.setObstacles(obstacles);
		finderInput.setTrackedBot(getTBot());
		finderInput.setDest(getMoveCon().getDestination());
		finderInput.setTargetAngle(getMoveCon().getTargetAngle());
		finderInput.getMoveCon().getMoveConstraints().setDefaultAccLimit();
		finderInput.getMoveCon().getMoveConstraints().setDefaultVelLimit();
		final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput, getWorldFrame().getTimestamp());
		Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localInput);
		
		driver.setPath(path.orElse(null), getMoveCon().getDestination(), getMoveCon().getTargetAngle());
		List<IDrawableShape> debugShapes = new ArrayList<>(localInput.getDebugShapes());
		driver.setShapes(EShapesLayer.TRAJ_PATH_DEBUG, debugShapes);
		
		if (path.isPresent())
		{
			ITrajectory<IVector3> traj = new TrajectoryXyw(path.get().getTrajectory(),
					TrajectoryGenerator.generateRotationTrajectoryStub(getMoveCon().getTargetAngle(), null));
			TrajectoryWithTime<IVector3> twt = new TrajectoryWithTime<>(traj, path.get().gettStart());
			getBot().setCurrentTrajectory(Optional.ofNullable(twt));
		} else
		{
			DrawableCircle dc = new DrawableCircle(getPos(), 100, Color.pink);
			dc.setFill(true);
			shapes.add(dc);
		}
		
		driver.setShapes(EShapesLayer.TRAJ_PATH_OBSTACLES, shapes);
	}
	
	enum EState
	{
		DEF
	}
	
	
	private class DefState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EState.DEF;
		}
	}
	
	
	@Override
	protected void onSkillFinished()
	{
		super.onSkillFinished();
		
		getBot().setCurrentTrajectory(Optional.empty());
	}
	
	
}
