/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveTestRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MoveTestRole.class.getName());
	
	private final List<MotionResult> results = new ArrayList<>();
	private final String logFileName;
	private TimeSeriesDataCollector dataCollector = null;
	private BotWatcher bw = null;
	private final EMoveMode mode;
	
	private enum EEvent implements IEvent
	{
		DONE,
	}
	
	
	/**
	 * Possible move modes
	 */
	public enum EMoveMode
	{
		/**  */
		TRAJ_WHEEL_VEL,
		/**  */
		TRAJ_VEL,
		/**  */
		TRAJ_POS,
		/** */
		TRAJ_GLOBAL_VEL
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
	 * @param rollOut
	 */
	public MoveTestRole(final EMoveMode mode, final IVector2 initPos, final double orientation, final double scale,
			final double startAngle, final double stopAngle, final double angleStepDeg, final double angleTurnDeg,
			final int iterations, final String logFileName, final boolean rollOut)
	{
		super(ERole.MOVE_TEST);
		this.mode = mode;
		this.logFileName = logFileName;
		
		List<double[]> relTargets = new ArrayList<>();
		for (double a = AngleMath.deg2rad(startAngle); a < (AngleMath.deg2rad(stopAngle) - 1e-4); a += AngleMath
				.deg2rad(angleStepDeg))
		{
			IVector2 dir = Vector2.fromAngle(orientation);
			relTargets.add(new double[] { dir.x(), dir.y(), a, a + AngleMath.deg2rad(angleTurnDeg) });
		}
		
		IState lastState = new InitState();
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (double[] target : relTargets)
			{
				IVector2 dest = initPos.addNew(Vector2.fromXY(target[0], target[1]).scaleToNew(scale));
				double initOrient = orientation + target[2];
				double finalOrient = orientation + target[3];
				IState waitState1 = new WaitState(0);
				IState prepareState = new PrepareState(initPos, initOrient);
				IState waitState2 = new WaitState(500);
				IState moveState = new MoveToState(dest, finalOrient, rollOut);
				IState waitState3 = new WaitState(0);
				IState prepare2State = new PrepareState(dest, finalOrient);
				IState waitState4 = new WaitState(500);
				IState moveBackState = new MoveToState(initPos, initOrient, rollOut);
				
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
			IState evalState = new EvaluationState();
			addTransition(lastState, EEvent.DONE, evalState);
			lastState = evalState;
		}
	}
	
	
	private class InitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			triggerEvent(EEvent.DONE);
		}
	}
	
	
	private class EvaluationState extends AState
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
			
			identifyFrictionModel(exp.getAbsoluteFileName());
		}
	}
	
	private class WaitState extends AState
	{
		private long tStart;
		private final long waitNs;
		
		
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
	}
	
	private class PrepareState extends AState
	{
		protected IVector2 dest;
		protected final double orientation;
		private long tLastStill = 0;
		
		protected IVector2 initPos;
		protected double initOrientation;
		protected double destOrientation;
		
		
		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}
		
		
		@Override
		public void doEntryActions()
		{
			initPos = getPos();
			initOrientation = getBot().getOrientation();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			destOrientation = orientation;
			tLastStill = 0;
			
			AMoveToSkill move = AMoveToSkill.createMoveToSkill();
			move.getMoveCon().updateDestination(dest);
			move.getMoveCon().updateTargetAngle(orientation);
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			double dist2Dest = VectorMath.distancePP(dest, getPos());
			
			if ((getBot().getVel().getLength2() < 0.2) && (Math.abs(getBot().getAngularVel()) < 0.5) && (dist2Dest < 2000))
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
		}
		
		
		protected void onDone()
		{
			// can be overwritten
		}
	}
	
	private class MoveToState extends PrepareState
	{
		MotionResult result;
		boolean rollOut;
		
		
		private MoveToState(final IVector2 dest, final double orientation, final boolean rollOut)
		{
			super(dest, orientation);
			
			this.rollOut = rollOut;
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			
			
			switch (mode)
			{
				case TRAJ_POS:
					PositionSkill posSkill = new PositionSkill();
					posSkill.getMoveCon().updateDestination(dest);
					posSkill.getMoveCon().updateTargetAngle(orientation);
					setNewSkill(posSkill);
					break;
				case TRAJ_VEL:
				{
					MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation, EBotSkill.LOCAL_VELOCITY,
							rollOut);
					setNewSkill(skill);
				}
					break;
				case TRAJ_GLOBAL_VEL:
				{
					MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation, EBotSkill.GLOBAL_VELOCITY,
							rollOut);
					setNewSkill(skill);
				}
					break;
				case TRAJ_WHEEL_VEL:
				{
					MoveBangBangSkill skill = new MoveBangBangSkill(dest, destOrientation, EBotSkill.WHEEL_VELOCITY,
							rollOut);
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
			double dist2Line = LineMath.distancePL(getPos(), Line.fromPoints(initPos, dest));
			result.dists2Line.add(dist2Line);
		}
		
		
		@Override
		protected void onDone()
		{
			result.initPos = initPos;
			result.initOrientation = initOrientation;
			result.finalPos = getPos();
			result.finalOrientation = getBot().getOrientation();
			result.dest = dest;
			result.destOrientation = destOrientation;
			results.add(result);
			log.info(result);
		}
	}
	
	
	private static class MotionResult
	{
		private IVector2 initPos;
		private double initOrientation;
		private IVector2 finalPos;
		private double finalOrientation;
		private IVector2 dest;
		private double destOrientation;
		private final List<Double> dists2Line = new ArrayList<>();
		
		
		public List<Number> getNumberList()
		{
			double offset = VectorMath.distancePP(finalPos, dest);
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
	
	
	private ABot getBotMgrBot()
	{
		ABot aBot = null;
		try
		{
			ABotManager botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			aBot = botManager.getBots().get(getBotID());
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find botManager module", e);
		}
		return aBot;
	}
	
	
	@Override
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		
		ABot aBot = getBotMgrBot();
		if (aBot != null)
		{
			bw = new BotWatcher(aBot, EDataAcquisitionMode.BOT_MODEL);
			bw.start();
			
			if (!logFileName.isEmpty())
			{
				aBot.execute(new TigerSystemConsoleCommand(ConsoleCommandTarget.MAIN, "logfile " + logFileName));
				
				dataCollector = TimeSeriesDataCollectorFactory.createFullCollector("moveTest/" + logFileName);
				dataCollector.setStopAutomatically(false);
				dataCollector.setTimeout(600);
				dataCollector.start();
			}
		}
	}
	
	
	@Override
	protected void onCompleted()
	{
		super.onCompleted();
		
		ABot aBot = getBotMgrBot();
		if (aBot == null)
		{
			return;
		}
		
		if (bw != null)
		{
			bw.stop();
			
			if (bw.isDataReceived())
			{
				switch (bw.getAcqMode())
				{
					case MOTOR_MODEL:
					case BOT_MODEL:
					case DELAYS:
						identifyBotModel();
						break;
					case NONE:
					default:
						break;
				}
			}
		}
		
		if (!logFileName.isEmpty())
		{
			aBot.execute(new TigerSystemConsoleCommand(ConsoleCommandTarget.MAIN, "stoplog"));
			if (dataCollector != null)
			{
				dataCollector.stopExport();
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	private void identifyBotModel()
	{
		MatlabProxy mp;
		try
		{
			mp = MatlabConnection.getMatlabProxy();
			mp.eval("addpath('identification')");
			Object[] values = mp.returningFeval("bot", 1, bw.getAbsoluteFileName());
			double[] params = (double[]) values[0];
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("Bot Model Identification complete.%n");
			sb.append(
					String.format(Locale.ENGLISH, "X   => K: %.4f, T: %.4f, Tv: %.4f%n", params[0], params[1], params[2]));
			sb.append(
					String.format(Locale.ENGLISH, "Y   => K: %.4f, T: %.4f, Tv: %.4f%n", params[3], params[4], params[5]));
			sb.append(
					String.format(Locale.ENGLISH, "Err => X: %.4f, Y: %.4f, abs: %.4f%n", params[6], params[7], params[8]));
			sb.append(String.format(Locale.ENGLISH, "Dataloss: %.2f%%%n", params[9] * 100));
			
			log.info(sb.toString());
		} catch (MatlabConnectionException err)
		{
			log.error(err.getMessage(), err);
		} catch (MatlabInvocationException err)
		{
			log.error("Error evaluating matlab function: " + err.getMessage(), err);
		} catch (Exception err)
		{
			log.error("An error occurred.", err);
		}
	}
	
	
	private void identifyFrictionModel(final String csvFile)
	{
		MatlabProxy mp;
		try
		{
			mp = MatlabConnection.getMatlabProxy();
			mp.eval("addpath('identification')");
			Object[] values = mp.returningFeval("fric", 1, csvFile);
			double[] params = (double[]) values[0];
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("Bot Friction Identification complete. %n");
			sb.append(
					String.format(Locale.ENGLISH, "x1: %.6f, x3: %.6f, x5: %.6f%n", params[0], params[1], params[2]));
			sb.append(
					String.format(Locale.ENGLISH, "y1: %.6f, y3: %.6f, y5: %.6f%n", params[3], params[4], params[5]));
			sb.append(
					String.format(Locale.ENGLISH, "Fit => X: %.2f%%, Y: %.2f%%%n", params[6], params[7]));
			
			log.info(sb.toString());
		} catch (MatlabConnectionException err)
		{
			log.error(err.getMessage(), err);
		} catch (MatlabInvocationException err)
		{
			log.error("Error evaluating matlab function: " + err.getMessage(), err);
		} catch (Exception err)
		{
			log.error("An error occurred.", err);
		}
	}
}
