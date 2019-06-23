/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 2, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveBangBangSkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.VisionWatcher;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveTestRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger		log		= Logger.getLogger(MoveTestRole.class.getName());
	
	private final List<MotionResult>	results	= new ArrayList<>();
	private final String					logFileName;
	private VisionWatcher				vw			= null;
	private final EMoveMode				mode;
	
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
	 */
	public enum EMoveMode
	{
		/**  */
		TRAJ_WHEEL_VEL,
		/**  */
		TRAJ_VEL,
		/**  */
		TRAJ_POS
	}
	
	
	/**
	 * @param mode
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
	public MoveTestRole(final EMoveMode mode, final IVector2 initPos, final double orientation, final double scale,
			final double startAngle, final double stopAngle, final double angleStepDeg, final double angleTurnDeg,
			final int iterations,
			final String logFileName)
	{
		super(ERole.MOVE_TEST);
		this.mode = mode;
		this.logFileName = logFileName;
		
		List<double[]> relTargets = new ArrayList<>();
		for (double a = AngleMath.deg2rad(startAngle); a < (AngleMath.deg2rad(stopAngle) - 1e-4); a += AngleMath
				.deg2rad(angleStepDeg))
		{
			IVector2 dir = new Vector2(orientation);
			relTargets.add(new double[] { dir.x(), dir.y(), a, a + AngleMath.deg2rad(angleTurnDeg) });
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
		addEndTransition(lastState, EEvent.DONE);
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
			double avgDist2Line = results.stream().flatMap(r -> r.dists2Line.stream()).mapToDouble(a -> a).average()
					.getAsDouble();
			double avgOffset = results.stream().mapToDouble(r -> r.dest.subtractNew(r.finalPos).getLength2()).average()
					.getAsDouble();
			log.info(String.format("Overall: avgDist2Line=%f, avgOffset=%f", avgDist2Line, avgOffset));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			CSVExporter exp = new CSVExporter("data/movetest/" + sdf.format(new Date()), false);
			for (MotionResult r : results)
			{
				exp.addValues(r.getNumberList());
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
			tStart = getWFrame().getTimestamp();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getTimestamp() - tStart) > waitNs)
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
			lastOrientation = getBot().getAngle();
			initPos = getPos();
			initOrientation = getBot().getAngle();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			destOrientation = orientation;
			tLastStill = 0;
			
			
			// AMoveToSkill move = AMoveToSkill.createMoveToSkill();
			// move.getMoveCon().updateDestination(dest);
			// move.getMoveCon().updateTargetAngle(orientation);
			// setNewSkill(move);
			
			PositionSkill posSkill = new PositionSkill(dest, orientation);
			setNewSkill(posSkill);
			
			// ABotSkill botSkill;
			// if (getWFrame().isInverted())
			// {
			// botSkill = new BotSkillPositionPid(dest.multiplyNew(-1), destOrientation);
			// } else
			// {
			// botSkill = new BotSkillPositionPid(dest, destOrientation);
			// }
			// BotSkillWrapperSkill skill = new BotSkillWrapperSkill(botSkill);
			
			// MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation);
			// setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			double dist = GeoMath.distancePP(lastPos, getPos());
			double aDiff = Math.abs(AngleMath.difference(lastOrientation, getBot().getAngle()));
			double dist2Dest = GeoMath.distancePP(dest, getPos());
			
			if ((dist < 15) && (aDiff < 0.1) && (dist2Dest < 2000))
			{
				if (tLastStill == 0)
				{
					tLastStill = getWFrame().getTimestamp();
				}
				if ((getWFrame().getTimestamp() - tLastStill) > 5e8)
				{
					onDone();
					triggerEvent(EEvent.DONE);
				}
			} else
			{
				tLastStill = 0;
			}
			lastPos = getPos();
			lastOrientation = getBot().getAngle();
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
			
			
			switch (mode)
			{
				case TRAJ_POS:
					PositionSkill posSkill = new PositionSkill(dest, orientation);
					setNewSkill(posSkill);
					break;
				case TRAJ_VEL:
				{
					MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation);
					setNewSkill(skill);
				}
					break;
				case TRAJ_WHEEL_VEL:
				{
					MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation, EBotSkill.WHEEL_VELOCITY);
					setNewSkill(skill);
				}
					break;
				default:
					break;
			}
			
			result = new MotionResult();
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			double dist2Line = GeoMath.distancePL(getPos(), initPos, dest);
			result.dists2Line.add(dist2Line);
		}
		
		
		@Override
		protected void onDone()
		{
			result.initPos = initPos;
			result.initOrientation = initOrientation;
			result.finalPos = getPos();
			result.finalOrientation = getBot().getAngle();
			result.dest = dest;
			result.destOrientation = destOrientation;
			results.add(result);
			log.info(result);
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
	
	
	private static class MotionResult
	{
		private IVector2				initPos;
		private double					initOrientation;
		private IVector2				finalPos;
		private double					finalOrientation;
		private IVector2				dest;
		private double					destOrientation;
		private final List<Double>	dists2Line	= new ArrayList<>();
		
		
		public List<Number> getNumberList()
		{
			double offset = GeoMath.distancePP(finalPos, dest);
			IVector2 diff = finalPos.subtractNew(dest);
			double avgDist2Line = dists2Line.stream().mapToDouble(a -> a).average().getAsDouble();
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
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		
		if (!logFileName.isEmpty() && (getBot().getBot() instanceof ABot))
		{
			((ABot) getBot().getBot()).execute(
					new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "logfile " + logFileName));
			
			vw = new VisionWatcher("moveTest/" + logFileName);
			vw.setStopAutomatically(false);
			vw.setTimeout(600);
			vw.start();
		}
	}
	
	
	@Override
	protected void onCompleted()
	{
		super.onCompleted();
		
		if (!logFileName.isEmpty() && (getBot().getBot() instanceof ABot))
		{
			((ABot) getBot().getBot()).execute(
					new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "stoplog"));
			if (vw != null)
			{
				vw.stopExport();
			}
		}
	}
}
