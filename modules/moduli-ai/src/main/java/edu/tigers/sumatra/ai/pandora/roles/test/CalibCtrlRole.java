/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3.ITigerBotV3Observer;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CalibCtrlRole extends ARole
{
	
	private enum EStateId
	{
		PREPARE,
		CALIB
	}
	
	private enum EEvent
	{
		DONE
	}
	
	
	/**
	 * @param initPos
	 * @param initOrientation
	 * @param dist
	 * @param acc
	 * @param startAngleDeg
	 * @param stopAngleDeg
	 * @param stepDeg
	 * @param iterations
	 */
	public CalibCtrlRole(final IVector2 initPos, final double initOrientation, final double dist, final double acc,
			final double startAngleDeg,
			final double stopAngleDeg,
			final double stepDeg,
			final double iterations)
	{
		super(ERole.CALIB_CTRL);
		
		IRoleState lastState = new PrepareState(initPos, initOrientation + AngleMath.PI_HALF);
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (double a = startAngleDeg; a <= stopAngleDeg; a += stepDeg)
			{
				IRoleState calibState = new CalibState(a, dist, acc);
				IRoleState prepareState = new PrepareState(initPos, (initOrientation - AngleMath.deg2rad(a))
						+ AngleMath.PI_HALF);
				addTransition(lastState, EEvent.DONE, prepareState);
				addTransition(prepareState, EEvent.DONE, calibState);
				lastState = calibState;
			}
		}
		addEndTransition(lastState, EEvent.DONE);
	}
	
	
	private class PrepareState implements IRoleState
	{
		private AMoveToSkill		move;
		protected IVector2		dest;
		protected final double	orientation;
		private long				tLastStill	= 0;
														
		protected IVector2		lastPos;
		protected double			lastOrientation;
		protected IVector2		initPos;
										
										
		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}
		
		
		@Override
		public void doEntryActions()
		{
			// TigerBotV3 bot = (TigerBotV3) getBot().getBot();
			// bot.execute(new TigerCtrlSetControllerType(EControllerType.TIGGA));
			lastPos = getPos();
			lastOrientation = getBot().getAngle();
			initPos = getPos();
			move = AMoveToSkill.createMoveToSkill();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			move.getMoveCon().updateDestination(dest);
			move.getMoveCon().updateTargetAngle(orientation);
			move.getMoveCon().setPenaltyAreaAllowedOur(true);
			move.getMoveCon().setPenaltyAreaAllowedTheir(true);
			
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			double dist = GeoMath.distancePP(lastPos, getPos());
			double aDiff = Math.abs(AngleMath.difference(lastOrientation, getBot().getAngle()));
			
			if ((dist < 10) && (aDiff < 0.05))
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
	
	private class CalibState implements IRoleState, ITigerBotV3Observer
	{
		
		private final double	angleDeg;
		private final double	dist;
		private final double	acc;
									
									
		/**
		 * @param angleDeg
		 * @param dist
		 * @param acc
		 */
		public CalibState(final double angleDeg, final double dist, final double acc)
		{
			this.angleDeg = angleDeg;
			this.dist = dist;
			this.acc = acc;
		}
		
		
		@Override
		public void doEntryActions()
		{
			TigerBotV3 bot = (TigerBotV3) getBot().getBot();
			bot.execute(new TigerCtrlSetControllerType(EControllerType.CALIBRATE));
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(ConsoleCommandTarget.MAIN);
			consoleCmd.setText(String.format("calib %f %f %f", angleDeg, dist, acc));
			bot.execute(consoleCmd);
			bot.addObserver(this);
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void onConsolePrint(final TigerSystemConsolePrint cmd)
		{
			triggerEvent(EEvent.DONE);
		}
		
		
		@Override
		public void doExitActions()
		{
			TigerBotV3 bot = (TigerBotV3) getBot().getBot();
			bot.execute(new TigerCtrlSetControllerType(EControllerType.TIGGA));
			bot.removeObserver(this);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CALIB;
		}
		
		
		@Override
		public void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
		{
		}
		
	}
}
