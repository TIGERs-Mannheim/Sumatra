/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 2, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveTestRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger		log		= Logger.getLogger(MoveTestRole.class.getName());
	
	private final List<MotionResult>	results	= new ArrayList<>();
	private final String					logFileName;
	
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
	public MoveTestRole(final IVector2 initPos, final float orientation, final float scale,
			final float startAngle, final float stopAngle, final float angleStepDeg, final float angleTurnDeg,
			final int iterations,
			final String logFileName)
	{
		super(ERole.MOVE_TEST);
		this.logFileName = logFileName;
		
		List<float[]> relTargets = new ArrayList<>();
		for (float a = AngleMath.deg2rad(startAngle); a < (AngleMath.deg2rad(stopAngle) - 1e-4); a += AngleMath
				.deg2rad(angleStepDeg))
		{
			relTargets.add(new float[] { 1, 0, a, a + AngleMath.deg2rad(angleTurnDeg) });
		}
		
		IRoleState lastState = new InitState();
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (float[] target : relTargets)
			{
				IVector2 dest = initPos.addNew(new Vector2(target[0], target[1]).scaleToNew(scale));
				float initOrient = orientation + target[2];
				float finalOrient = orientation + target[3];
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	private class InitState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			if (!logFileName.isEmpty())
			{
				getBot().getBot().execute(
						new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "logfile " + logFileName));
			}
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
			if (!logFileName.isEmpty())
			{
				getBot().getBot().execute(
						new TigerSystemConsoleCommand(ConsoleCommandTarget.MEDIA, "stoplog"));
			}
			
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
		private long	tStart;
		private long	waitNs;
		
		
		public WaitState(final long waitMs)
		{
			waitNs = (long) (waitMs * 1e6);
		}
		
		
		@Override
		public void doEntryActions()
		{
			tStart = System.nanoTime();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((System.nanoTime() - tStart) > waitNs)
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
		private IMoveToSkill		move;
		protected IVector2		dest;
		protected final float	orientation;
		private long				tLastStill	= 0;
		
		protected IVector2		lastPos;
		protected float			lastOrientation;
		protected IVector2		initPos;
		protected float			initOrientation;
		protected float			destOrientation;
		
		
		private PrepareState(final IVector2 dest, final float orientation)
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
			move = AMoveSkill.createMoveToSkill();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			destOrientation = orientation;
			move.getMoveCon().updateDestination(dest);
			move.getMoveCon().updateTargetAngle(orientation);
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			float dist = GeoMath.distancePP(lastPos, getPos());
			float aDiff = Math.abs(AngleMath.difference(lastOrientation, getBot().getAngle()));
			float dist2Dest = GeoMath.distancePP(dest, getPos());
			
			if ((dist < 10) && (aDiff < 0.05) && (dist2Dest < 1000))
			{
				if (tLastStill == 0)
				{
					tLastStill = System.nanoTime();
				}
				if ((System.nanoTime() - tLastStill) > 5e8)
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
		MotionResult	result;
		
		
		private MoveToState(final IVector2 dest, final float orientation)
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
			float dist2Line = GeoMath.distancePL(getPos(), initPos, dest);
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
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
		private IVector2		initPos;
		private float			initOrientation;
		private IVector2		finalPos;
		private float			finalOrientation;
		private IVector2		dest;
		private float			destOrientation;
		private List<Float>	dists2Line	= new ArrayList<>();
		
		
		public List<Number> getNumberList()
		{
			float offset = GeoMath.distancePP(finalPos, dest);
			IVector2 diff = finalPos.subtractNew(dest);
			float avgDist2Line = (float) dists2Line.stream().mapToDouble(a -> a).average().getAsDouble();
			float aDiff = AngleMath.difference(finalOrientation, destOrientation);
			
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
}
