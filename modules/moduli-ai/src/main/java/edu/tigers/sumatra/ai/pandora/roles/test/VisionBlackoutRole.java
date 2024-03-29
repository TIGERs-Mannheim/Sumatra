/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGetBall;
import edu.tigers.sumatra.botmanager.botskills.BotSkillInterceptBall;
import edu.tigers.sumatra.botmanager.botskills.BotSkillTechChallenge2022Stage1;
import edu.tigers.sumatra.botmanager.botskills.BotSkillTechChallenge2022Stage2;
import edu.tigers.sumatra.botmanager.botskills.BotSkillTechChallenge2022Stage3;
import edu.tigers.sumatra.botmanager.botskills.BotSkillTechChallenge2022Stage4;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;


public class VisionBlackoutRole extends ARole
{
	@Configurable(defValue = "2.0", comment = "[1/s]")
	private static double getRotationSpeed = 2.0;

	@Configurable(defValue = "1.0", comment = "[m/s]")
	private static double getMaxSpeed = 1.0;

	@Configurable(defValue = "0.1", comment = "[m/s]")
	private static double getDockingSpeed = 0.1;

	@Configurable(defValue = "2.0", comment = "[m/s²]")
	private static double getAcceleration = 2.0;

	@Configurable(defValue = "2.0", comment = "[m/s]")
	private static double interceptMaxSpeed = 2.0;

	@Configurable(defValue = "2.0", comment = "[m/s²]")
	private static double interceptAcceleration = 2.0;


	public VisionBlackoutRole(final EChallengeType type)
	{
		super(ERole.VISION_BLACKOUT_ROLE);

		IState readyState = new ReadyState();
		IState activeState = new ActiveState(type);

		setInitialState(readyState);
		addTransition(EEvent.GO, activeState);
		addTransition(EEvent.STOPPED, readyState);
	}


	public enum EChallengeType
	{
		STATIC_BALL,
		MOVING_BALL,

		TECH_CHALLENGE_STAGE1,
		TECH_CHALLENGE_STAGE2,
		TECH_CHALLENGE_STAGE3,
		TECH_CHALLENGE_STAGE4,
	}

	private enum EEvent implements IEvent
	{
		GO,
		STOPPED
	}

	private class ReadyState extends AState
	{
		@Override
		public void doUpdate()
		{
			SslGcRefereeMessage.Referee.Command cmd = getAiFrame().getRefereeMsg().getCommand();
			if (cmd == SslGcRefereeMessage.Referee.Command.FORCE_START)
			{
				triggerEvent(EEvent.GO);
			}
		}


		@Override
		public void doEntryActions()
		{
			setNewSkill(new IdleSkill());
		}
	}

	private class ActiveState extends AState
	{
		EChallengeType type;


		public ActiveState(EChallengeType type)
		{
			super();
			this.type = type;
		}


		@Override
		public void doEntryActions()
		{
			ABotSkill skill;

			switch (type)
			{
				case STATIC_BALL ->
				{
					MoveConstraints mcGet = getBot().getMoveConstraints();
					mcGet.setVelMax(getMaxSpeed);
					mcGet.setAccMax(getAcceleration);
					skill = new BotSkillGetBall(mcGet,
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent(), getRotationSpeed,
							getDockingSpeed);
				}
				case MOVING_BALL ->
				{
					MoveConstraints mcIntercept = getBot().getMoveConstraints();
					mcIntercept.setVelMax(interceptMaxSpeed);
					mcIntercept.setAccMax(interceptAcceleration);
					skill = new BotSkillInterceptBall(mcIntercept);
					KickerDribblerCommands kd = new KickerDribblerCommands();
					kd.setDribbler(getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent());
					skill.setKickerDribbler(kd);
				}
				case TECH_CHALLENGE_STAGE1 ->
				{
					MoveConstraints mcTC2022Stage1 = getBot().getMoveConstraints();
					mcTC2022Stage1.setVelMax(getMaxSpeed);
					mcTC2022Stage1.setAccMax(getAcceleration);
					skill = new BotSkillTechChallenge2022Stage1(mcTC2022Stage1,
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent(),
							getRotationSpeed);
				}
				case TECH_CHALLENGE_STAGE2 ->
				{
					MoveConstraints mcTC2022Stage2 = getBot().getMoveConstraints();
					mcTC2022Stage2.setVelMax(getMaxSpeed);
					mcTC2022Stage2.setAccMax(getAcceleration);
					skill = new BotSkillTechChallenge2022Stage2(mcTC2022Stage2,
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent(),
							getRotationSpeed);
				}
				case TECH_CHALLENGE_STAGE3 ->
				{
					MoveConstraints mcTC2022Stage3 = getBot().getMoveConstraints();
					mcTC2022Stage3.setVelMax(getMaxSpeed);
					mcTC2022Stage3.setAccMax(getAcceleration);
					skill = new BotSkillTechChallenge2022Stage3(mcTC2022Stage3,
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent(),
							getRotationSpeed);
				}
				case TECH_CHALLENGE_STAGE4 ->
				{
					MoveConstraints mcTC2022Stage4 = getBot().getMoveConstraints();
					mcTC2022Stage4.setVelMax(getMaxSpeed);
					mcTC2022Stage4.setAccMax(getAcceleration);
					skill = new BotSkillTechChallenge2022Stage4(mcTC2022Stage4,
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultSpeed(),
							getBot().getRobotInfo().getBotParams().getDribblerSpecs().getDefaultMaxCurrent(),
							getRotationSpeed);
				}
				default -> throw new IllegalStateException("Unexpected value: " + type);
			}

			BotSkillWrapperSkill wrapper = new BotSkillWrapperSkill(skill);
			setNewSkill(wrapper);
		}


		@Override
		public void doUpdate()
		{
			SslGcRefereeMessage.Referee.Command cmd = getAiFrame().getRefereeMsg().getCommand();
			if (cmd == SslGcRefereeMessage.Referee.Command.STOP)
			{
				triggerEvent(EEvent.STOPPED);
			}
		}
	}
}
