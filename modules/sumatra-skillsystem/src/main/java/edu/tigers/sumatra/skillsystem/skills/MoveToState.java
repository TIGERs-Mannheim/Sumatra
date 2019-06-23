/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.pathfinder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToState extends AState
{
	private final AMoveSkill moveSkill;
	private TrajPathFinderV4 finder = new TrajPathFinderV4();
	private ObstacleGenerator obstacleGen = new ObstacleGenerator();
	private IPathFinderResult pathResult = null;
	
	private double targetAngle;
	
	
	protected MoveToState(final AMoveSkill moveSkill)
	{
		this.moveSkill = moveSkill;
	}
	
	
	@Override
	public void doEntryActions()
	{
		targetAngle = getTBot().getOrientation();
	}
	
	
	@Override
	public void doUpdate()
	{
		obstacleGen.setUseBall(getMoveCon().isBallObstacle());
		obstacleGen.setUseTheirBots(getMoveCon().isTheirBotsObstacle());
		obstacleGen.setUseOurBots(getMoveCon().isOurBotsObstacle());
		obstacleGen.setUsePenAreaOur(getMoveCon().isPenaltyAreaForbiddenOur());
		obstacleGen.setUsePenAreaTheir(getMoveCon().isPenaltyAreaForbiddenTheir());
		obstacleGen.setUseGoalPostsOur(getMoveCon().isGoalPostObstacle());
		obstacleGen.setIgnoredBots(getMoveCon().getIgnoredBots());
		getMoveCon().getMinDistToBall().ifPresent(obstacleGen::setSecDistBall);
		
		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(),
				getMoveCon().getPrioMap(), getGameState());
		obstacles.addAll(getMoveCon().getCustomObstacles());
		if (!getMoveCon().isIgnoreGameStateObstacles())
		{
			obstacles.addAll(
					obstacleGen.generateGameStateObstacles(getWorldFrame(), getGameState()));
		}
		getShapes().get(ESkillShapesLayer.TRAJ_PATH_OBSTACLES).addAll(obstacles);
		
		targetAngle = getMoveCon().getTargetAngle();
		
		TrajPathFinderInput finderInput = TrajPathFinderInput.fromBotOrTrajectory(getWorldFrame().getTimestamp(),
				getTBot());
		finderInput.setMoveConstraints(new MoveConstraints(getMoveCon().getMoveConstraints()));
		finderInput.setObstacles(obstacles);
		finderInput.setDest(getMoveCon().getDestination());
		finderInput.setTargetAngle(targetAngle);
		
		limitSpeedOnStoppedGame(finderInput.getMoveConstraints());
		
		pathResult = finder.calcPath(finderInput);
		
		getShapes().get(ESkillShapesLayer.PATH_FINDER_DEBUG).addAll(finder.getDebugShapes());
		
		Optional<IObstacle> collider = pathResult.getCollider();
		collider.ifPresent(obstacle -> obstacle.setColor(Color.RED.brighter()));
		
		if (criticalCollisionAhead())
		{
			// brake fast to avoid collision
			DrawableCircle dCircle = new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100), Color.pink);
			getShapes().get(ESkillShapesLayer.PATH).add(dCircle);
			
			MoveConstraints emergencyMoveConstraints = new MoveConstraints(finderInput.getMoveConstraints());
			emergencyMoveConstraints.setAccMax(6);
			emergencyMoveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
			emergencyMoveConstraints.setJerkMax(DriveLimits.MAX_JERK);
			emergencyMoveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);
			
			moveSkill.setLocalVelocity(Vector2.zero(), 0, emergencyMoveConstraints);
			getBot().setCurrentTrajectory(null);
		} else
		{
			executePath(pathResult.getTrajectory(), finderInput.getMoveConstraints());
		}
		drawPath(pathResult);
	}
	
	
	private boolean criticalCollisionAhead()
	{
		final double tCollision;
		if (pathResult.hasFrontCollision())
		{
			tCollision = 0;
		} else if (pathResult.hasIntermediateCollision())
		{
			tCollision = pathResult.getFirstCollisionTime();
		} else if (pathResult.hasBackCollision())
		{
			tCollision = pathResult.getTrajectory().getTotalTime() - pathResult.getCollisionDurationBack();
		} else
		{
			return false;
		}
		
		double reactionTime = 0.1;
		double amountOfBrake = getTBot().getMoveConstraints().getAccMax() * Math.max(0, tCollision - reactionTime);
		double currentVel = getTBot().getVel().getLength2();
		return currentVel - amountOfBrake > 0.5;
	}
	
	
	private void limitSpeedOnStoppedGame(MoveConstraints moveConstraints)
	{
		if (getGameState().isVelocityLimited())
		{
			moveConstraints.setVelMax(Math.min(moveConstraints.getVelMax(), RuleConstraints.getStopSpeed()));
		}
	}
	
	
	private void executePath(final ITrajectory<IVector2> trajectory, final MoveConstraints moveConstraints)
	{
		ITrajectory<IVector3> traj = new TrajectoryXyw(trajectory,
				TrajectoryGenerator.generateRotationTrajectoryStub(getMoveCon().getTargetAngle()));
		
		IVector2 dest = trajectory.getNextDestination(0);
		setTargetPose(dest, targetAngle, moveConstraints, traj);
	}
	
	
	private void drawPath(final IPathFinderResult pathResult)
	{
		List<IDrawableShape> shapes = new ArrayList<>(3);
		double t = 0;
		if (pathResult.getCollisionDurationFront() > 0)
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), 0,
					Math.min(pathResult.getCollisionDurationFront(), pathResult.getTrajectory().getTotalTime()));
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.red));
			t = pathResult.getCollisionDurationFront();
		}
		double tBeforeCollision = Math.min(pathResult.getCollisionLookahead(),
				pathResult.getTrajectory().getTotalTime() - pathResult.getCollisionDurationBack());
		if (t < tBeforeCollision)
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					tBeforeCollision);
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.green));
			t = tBeforeCollision;
		}
		
		if (t < pathResult.getCollisionLookahead())
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					pathResult.getCollisionLookahead());
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.red));
			t = pathResult.getCollisionLookahead();
		}
		
		if (t < pathResult.getTrajectory().getTotalTime())
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					pathResult.getTrajectory().getTotalTime());
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.orange));
		}
		
		shapes.add(new DrawableBot(getMoveCon().getDestination(), getMoveCon().getTargetAngle(),
				getTBot().getBotId().getTeamColor().getColor(),
				Geometry.getBotRadius() + 25,
				Geometry.getBotRadius() + 25));
		
		if (pathResult.hasIntermediateCollision())
		{
			IVector2 collisionPos = pathResult.getTrajectory().getPositionMM(pathResult.getFirstCollisionTime());
			DrawableCircle dCircleCollision = new DrawableCircle(Circle.createCircle(collisionPos, 10), Color.RED);
			shapes.add(dCircleCollision);
		}
		
		getShapes().get(ESkillShapesLayer.PATH).addAll(shapes);
	}
	
	
	protected ITrackedBot getTBot()
	{
		return moveSkill.getTBot();
	}
	
	
	protected GameState getGameState()
	{
		return moveSkill.getGameState();
	}
	
	
	protected ABot getBot()
	{
		return moveSkill.getBot();
	}
	
	
	protected WorldFrame getWorldFrame()
	{
		return moveSkill.getWorldFrame();
	}
	
	
	protected MovementCon getMoveCon()
	{
		return moveSkill.getMoveCon();
	}
	
	
	protected AMoveBotSkill setTargetPose(final IVector2 dest, final double targetAngle,
			final MoveConstraints moveConstraints, final ITrajectory<IVector3> trajectory)
	{
		return moveSkill.setTargetPose(dest, targetAngle, moveConstraints, trajectory);
	}
	
	
	protected ShapeMap getShapes()
	{
		return moveSkill.getShapes();
	}
}
