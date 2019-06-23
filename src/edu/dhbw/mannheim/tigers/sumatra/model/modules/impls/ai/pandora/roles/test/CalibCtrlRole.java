/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3.ITigerBotV3Observer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


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
	public CalibCtrlRole(final IVector2 initPos, final float initOrientation, final float dist, final float acc,
			final float startAngleDeg,
			final float stopAngleDeg,
			final float stepDeg,
			final float iterations)
	{
		super(ERole.CALIB_CTRL);
		
		IRoleState lastState = new PrepareState(initPos, initOrientation + AngleMath.PI_HALF);
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (float a = startAngleDeg; a <= stopAngleDeg; a += stepDeg)
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
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
		
		
		private PrepareState(final IVector2 dest, final float orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}
		
		
		@Override
		public void doEntryActions()
		{
			TigerBotV3 bot = (TigerBotV3) getBot().getBot();
			bot.execute(new TigerCtrlSetControllerType(EControllerType.TIGGA));
			lastPos = getPos();
			lastOrientation = getBot().getAngle();
			initPos = getPos();
			move = AMoveSkill.createMoveToSkill();
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
			float dist = GeoMath.distancePP(lastPos, getPos());
			float aDiff = Math.abs(AngleMath.difference(lastOrientation, getBot().getAngle()));
			
			if ((dist < 10) && (aDiff < 0.05))
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
	
	private class CalibState implements IRoleState, ITigerBotV3Observer
	{
		
		private final float	angleDeg;
		private final float	dist;
		private final float	acc;
		
		
		/**
		 * @param angleDeg
		 * @param dist
		 * @param acc
		 */
		public CalibState(final float angleDeg, final float dist, final float acc)
		{
			this.angleDeg = angleDeg;
			this.dist = dist;
			this.acc = acc;
		}
		
		
		@Override
		public void doEntryActions()
		{
			if (getBotType() != EBotType.TIGER_V3)
			{
				throw new IllegalStateException("Invalid bot type.");
			}
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
			return EStateId.CALIB;
		}
		
		
		@Override
		public void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
		{
		}
		
	}
}
