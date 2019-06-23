/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.TrajPathDriver;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveEvaluateSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger		log				= Logger.getLogger(MoveEvaluateSkill.class.getName());
	
	private final List<MotionResult>	results			= new ArrayList<>();
	private final String					logFileName;
	private final TrajPathDriver		trajPathDriver	= new TrajPathDriver();
	
	private enum EEvent
	{
		DONE,
	}
	
	private enum EStateId
	{
		WAIT,
		MOVE,
		PREPARE,
		EVALUATE,
		INIT
	}
	
	
	/**
	 * @param initPos
	 * @param orientation
	 * @param scale
	 * @param angleStepDeg
	 * @param startAngle
	 * @param stopAngle
	 * @param angleTurnDeg
	 * @param iterations
	 * @param logFileName
	 */
	public MoveEvaluateSkill(final IVector2 initPos, final double orientation, final double scale,
			final double startAngle, final double stopAngle, final double angleStepDeg, final double angleTurnDeg,
			final int iterations,
			final String logFileName)
	{
		super(ESkill.MOVE_EVALUATE);
		this.logFileName = logFileName;
		
		List<double[]> relTargets = new ArrayList<>();
		for (double a = AngleMath.deg2rad(startAngle); a < (AngleMath.deg2rad(stopAngle) - 1e-4); a += AngleMath
				.deg2rad(angleStepDeg))
		{
			relTargets.add(new double[] { 1, 0, a, a + AngleMath.deg2rad(angleTurnDeg) });
		}
		
		IRoleState lastState = new InitState();
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (double[] target : relTargets)
			{
				IVector2 dest = initPos.addNew(new Vector2(target[0], target[1]).scaleToNew(scale));
				double initOrient = orientation + target[2];
				double finalOrient = orientation + target[3];
				IRoleState waitState1 = new WaitState(0);
				IRoleState prepareState = new PrepareState(initPos, initOrient);
				IRoleState waitState2 = new WaitState(500);
				IRoleState moveState = new MoveToState(dest, finalOrient);
				IRoleState waitState3 = new WaitState(0);
				IRoleState prepare2State = new PrepareState(dest, finalOrient);
				IRoleState waitState4 = new WaitState(500);
				IRoleState moveBackState = new MoveToState(initPos, initOrient);
				
				addTransition(lastState, EEvent.DONE, waitState1);
				addTransition(waitState1, EEvent.DONE, prepareState);
				addTransition(prepareState, EEvent.DONE, waitState2);
				addTransition(waitState2, EEvent.DONE, moveState);
				addTransition(moveState, EEvent.DONE, waitState3);
				addTransition(waitState3, EEvent.DONE, prepare2State);
				addTransition(prepare2State, EEvent.DONE, waitState4);
				addTransition(waitState4, EEvent.DONE, moveBackState);
				lastState = moveBackState;
			}
			IRoleState evalState = new EvaluationState();
			addTransition(lastState, EEvent.DONE, evalState);
			lastState = evalState;
		}
		setPathDriver(trajPathDriver);
	}
	
	
	private class InitState implements IRoleState
	{
		@Override
		public void doEntryActions()
		{
			triggerEvent(EEvent.DONE);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.INIT;
		}
		
	}
	
	
	private class EvaluationState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			double avgDist2Line = results.stream().flatMap(r -> r.parts.stream()).mapToDouble(a -> a.dist2Line).average()
					.getAsDouble();
			double avgDist2Traj = results.stream().flatMap(r -> r.parts.stream()).mapToDouble(a -> a.dist2Traj).average()
					.getAsDouble();
			double avgOffset = results.stream().mapToDouble(r -> r.dest.subtractNew(r.finalPos).getLength2()).average()
					.getAsDouble();
			log.info(String.format("Overall: avgDist2Line=%f, avgDist2Traj=%f, avgOffset=%f", avgDist2Line, avgDist2Traj,
					avgOffset));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String folder = "data/movetest/" + sdf.format(new Date()) + "/";
			CSVExporter exp = new CSVExporter(folder + "summary", false);
			int i = 0;
			for (MotionResult r : results)
			{
				exp.addValues(r.getNumberList());
				CSVExporter exp2 = new CSVExporter(String.format("%spart%03d", folder, i), false);
				for (MotionResultPart p : r.parts)
				{
					exp2.addValues(p.getNumberList());
				}
				i++;
			}
			triggerEvent(EEvent.DONE);
			results.clear();
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
			return EStateId.EVALUATE;
		}
	}
	
	private class WaitState implements IRoleState
	{
		private long			tStart;
		private final long	waitNs;
		
		
		public WaitState(final long waitMs)
		{
			waitNs = (long) (waitMs * 1e6);
		}
		
		
		@Override
		public void doEntryActions()
		{
			tStart = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWorldFrame().getTimestamp() - tStart) > waitNs)
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAIT;
		}
	}
	
	private class PrepareState implements IRoleState
	{
		protected IVector2		dest;
		protected final double	orientation;
		private long				tLastStill	= 0;
		
		protected IVector2		lastPos;
		protected double			lastOrientation;
		protected IVector2		initPos;
		protected double			initOrientation;
		protected double			destOrientation;
		
		
		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}
		
		
		@Override
		public void doEntryActions()
		{
			lastPos = getPos();
			lastOrientation = getTBot().getAngle();
			initPos = getPos();
			initOrientation = getTBot().getAngle();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			destOrientation = orientation;
			
			TrajPathFinderV4 finder = new TrajPathFinderV4();
			TrajPathFinderInput input = new TrajPathFinderInput(getWorldFrame().getTimestamp());
			input.setDest(dest);
			input.setTargetAngle(destOrientation);
			input.setTrackedBot(getTBot());
			input.setMoveCon(getMoveCon());
			TrajectoryWithTime<IVector2> path = finder.calcPath(input).orElseGet(null);
			
			trajPathDriver.setPath(path, dest, destOrientation);
		}
		
		
		@Override
		public void doUpdate()
		{
			double dist = GeoMath.distancePP(lastPos, getPos());
			double aDiff = Math.abs(AngleMath.difference(lastOrientation, getTBot().getAngle()));
			double dist2Dest = GeoMath.distancePP(dest, getPos());
			double angleDiff2Dest = Math.abs(AngleMath.difference(destOrientation, getTBot().getAngle()));
			
			if ((dist < 10) && (aDiff < 0.1) && (dist2Dest < 1000) && (angleDiff2Dest < 0.2))
			{
				if (tLastStill == 0)
				{
					tLastStill = getWorldFrame().getTimestamp();
				}
				if ((getWorldFrame().getTimestamp() - tLastStill) > 5e8)
				{
					onDone();
					triggerEvent(EEvent.DONE);
				}
			} else
			{
				tLastStill = 0;
			}
			lastPos = getPos();
			lastOrientation = getTBot().getAngle();
		}
		
		
		protected void onDone()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class MoveToState extends PrepareState
	{
		MotionResult result;
		
		
		private MoveToState(final IVector2 dest, final double orientation)
		{
			super(dest, orientation);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			result = new MotionResult();
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			
			MotionResultPart part = new MotionResultPart();
			
			IVector3 dest3 = trajPathDriver.getNextDestination(getTBot(), getWorldFrame());
			IVector2 trajPos = dest3.getXYVector();
			double trajAngle = dest3.z();
			
			part.dist2Line = GeoMath.distancePL(getPos(), initPos, dest);
			part.dist2Traj = GeoMath.distancePP(trajPos, getPos());
			part.diffTraj = getPos().subtractNew(trajPos);
			part.diffAngle = AngleMath.getShortestRotation(trajAngle, getAngle());
			result.parts.add(part);
		}
		
		
		@Override
		protected void onDone()
		{
			result.initPos = initPos;
			result.initOrientation = initOrientation;
			result.finalPos = getPos();
			result.finalOrientation = getTBot().getAngle();
			result.dest = dest;
			result.destOrientation = destOrientation;
			results.add(result);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE;
		}
	}
	
	private static class MotionResultPart
	{
		private double		dist2Line;
		private double		dist2Traj;
		private double		diffAngle;
		private IVector2	diffTraj;
		
		
		public List<Number> getNumberList()
		{
			List<Number> nbrs = new ArrayList<>();
			nbrs.add(dist2Line);
			nbrs.add(dist2Traj);
			nbrs.add(diffAngle);
			nbrs.add(diffTraj.x());
			nbrs.add(diffTraj.y());
			return nbrs;
		}
	}
	
	private static class MotionResult
	{
		private IVector2								initPos;
		private double									initOrientation;
		private IVector2								finalPos;
		private double									finalOrientation;
		private IVector2								dest;
		private double									destOrientation;
		private final List<MotionResultPart>	parts	= new ArrayList<>();
		
		
		public List<Number> getNumberList()
		{
			double offset = GeoMath.distancePP(finalPos, dest);
			IVector2 diff = finalPos.subtractNew(dest);
			double avgDist2Line = parts.stream().mapToDouble(a -> a.dist2Line).average().getAsDouble();
			double avgDist2Traj = parts.stream().mapToDouble(a -> a.dist2Traj).average().getAsDouble();
			double aDiff = AngleMath.difference(finalOrientation, destOrientation);
			
			List<Number> nbrs = new ArrayList<>();
			nbrs.addAll(initPos.getNumberList());
			nbrs.add(initOrientation);
			nbrs.addAll(finalPos.getNumberList());
			nbrs.add(finalOrientation);
			nbrs.addAll(dest.getNumberList());
			nbrs.add(destOrientation);
			nbrs.addAll(diff.getNumberList());
			nbrs.add(offset);
			nbrs.add(aDiff);
			nbrs.add(avgDist2Line);
			nbrs.add(avgDist2Traj);
			return nbrs;
		}
		
		
		@Override
		public String toString()
		{
			List<Number> nbrs = getNumberList();
			StringBuilder sb = new StringBuilder();
			for (Number nbr : nbrs)
			{
				sb.append(nbr);
				sb.append(' ');
			}
			return sb.toString();
		}
	}
	
	
	@Override
	protected void onSkillStarted()
	{
		super.onSkillStarted();
		
		if (!logFileName.isEmpty())
		{
			getBot().execute(
					new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "logfile " + logFileName));
		}
	}
	
	
	@Override
	protected void onSkillFinished()
	{
		if (!logFileName.isEmpty())
		{
			getBot().execute(
					new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "stoplog"));
		}
	}
}
