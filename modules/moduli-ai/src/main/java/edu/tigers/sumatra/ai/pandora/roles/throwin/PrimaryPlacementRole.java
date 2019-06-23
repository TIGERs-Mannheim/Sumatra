/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.throwin;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.skillsystem.skills.PullBackSkillV2;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author MarkG
 */
public class PrimaryPlacementRole extends APlacementRole
{
	private static final Logger log = Logger.getLogger(PrimaryPlacementRole.class.getName());
	
	
	@Override
	protected void beforeUpdate()
	{
		if (isBallAtTarget() && (getCurrentState() != EStateId.CLEAR))
		{
			triggerEvent(EStateId.CLEAR);
		} else if (isBallToCloseToFieldBorder() && (getCurrentState() != EStateId.PULL))
		{
			triggerEvent(EStateId.PULL);
		} else if (!isBallToCloseToFieldBorder() && (getCurrentState() == EStateId.PULL))
		{
			triggerEvent(EStateId.PREPARE);
		}
		printText(getCurrentState().name(), 0);
	}
	
	
	/**
	  */
	public PrimaryPlacementRole()
	{
		super(ERole.PRIMARY_AUTOMATED_THROW_IN);
		setInitialState(new PrepareState());
		addTransition(EStateId.SHOOT, new ShootState());
		addTransition(EStateId.PULL, new PullState());
		addTransition(EStateId.PREPARE, new PrepareState());
		addTransition(EStateId.CLEAR, new ClearState());
	}
	
	
	private enum EStateId
	{
		PREPARE,
		SHOOT,
		PULL,
		CLEAR
	}
	
	private class PrepareState implements IRoleState
	{
		private AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			IVector2 ballToTarget = getAiFrame().getTacticalField().getThrowInInfo().getPos()
					.subtractNew(getWFrame().getBall().getPos());
			IVector2 dest = getAiFrame().getWorldFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(-200));
			if (GeoMath.distancePP(dest, getWFrame().getBall().getPos()) <= OffensiveConstants
					.getAutomatedThrowInPushDinstance())
			{
				dest = getAiFrame().getWorldFrame().getBall().getPos()
						.addNew(ballToTarget.scaleToNew(-OffensiveConstants.getAutomatedThrowInPushDinstance() * 1.2));
			}
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(dest);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 ballToTarget = getAiFrame().getTacticalField().getThrowInInfo().getPos()
					.subtractNew(getWFrame().getBall().getPos());
			IVector2 dest = getAiFrame().getWorldFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(-300));
			if (GeoMath.distancePP(getAiFrame().getTacticalField().getThrowInInfo().getPos(),
					getWFrame().getBall().getPos()) <= OffensiveConstants
							.getAutomatedThrowInPushDinstance())
			{
				dest = getAiFrame().getWorldFrame().getBall().getPos()
						.addNew(ballToTarget.scaleToNew(-OffensiveConstants.getAutomatedThrowInPushDinstance() * 1.2));
			}
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			
			if ((GeoMath.distancePP(dest, getPos()) < 50)
					&& getAiFrame().getTacticalField().getThrowInInfo().isReceiverReady())
			{
				triggerEvent(EStateId.SHOOT);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class ClearState implements IRoleState
	{
		private AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			IVector2 target = getWFrame().getBall().getPos()
					.addNew(getPos().subtractNew(getWFrame().getBall().getPos()).scaleToNew(400));
			if (GeoMath.distancePP(getPos(), getAiFrame().getTacticalField().getThrowInInfo().getPos()) > 400)
			{
				target = getPos();
			}
			skill.getMoveCon().getMoveConstraints().setAccMax(1);
			skill.getMoveCon().getMoveConstraints().setVelMax(1);
			skill.getMoveCon().updateDestination(target);
			if (!isBallAtTarget())
			{
				triggerEvent(EStateId.PREPARE);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.CLEAR;
		}
		
	}
	
	private class PullState implements IRoleState
	{
		private PullBackSkillV2	skill			= null;
		private AMoveToSkill		move			= null;
		private boolean			pullActive	= false;
		
		
		@Override
		public void doEntryActions()
		{
			pullActive = false;
			move = AMoveToSkill.createMoveToSkill();
			setNewSkill(move);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 movePos = calcPreparePos();
			move.getMoveCon().updateDestination(movePos);
			move.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			if (GeoMath.distancePP(getPos(), getWFrame().getBall().getPos()) < 400)
			{
				// move slow
				move.getMoveCon().getMoveConstraints().setVelMax(0.5);
			} else
			{
				// move fast
				move.getMoveCon().getMoveConstraints().setDefaultVelLimit();
			}
			if (!pullActive)
			{
				if ((GeoMath.distancePP(getPos(), movePos) < 20) && (getBot().getVel().getLength() < 0.1))
				{
					pullActive = true;
					skill = new PullBackSkillV2(AVector2.ZERO_VECTOR);
					setNewSkill(skill);
				}
			} else
			{
				// check for switch back to prepare
				if (skill.isIdle())
				{
					pullActive = false;
					move = AMoveToSkill.createMoveToSkill();
					setNewSkill(move);
				}
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.PULL;
		}
		
		
		private IVector2 calcPreparePos()
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			double fieldWidth = Geometry.getFieldWidth();
			double fieldLength = Geometry.getFieldLength();
			
			IVector2 target2 = null;
			double dirX = 0;
			double dirY = 0;
			if (ballPos.x() > ((fieldLength / 2.0) - 150))
			{
				dirX = 1;
			} else if (ballPos.x() < ((-fieldLength / 2.0) + 150))
			{
				dirX = -1;
			}
			if (ballPos.y() > ((fieldWidth / 2.0) - 150))
			{
				dirY = 1;
			} else if (ballPos.y() < ((-fieldWidth / 2.0) + 150))
			{
				dirY = -1;
			}
			target2 = new Vector2(dirX, dirY);
			target2 = target2.normalizeNew();
			if ((dirX == 0) && (dirY == 0))
			{
				target2 = getWFrame().getBall().getPos()
						.addNew(AVector2.ZERO_VECTOR.subtractNew(getWFrame().getBall().getPos())
								.scaleToNew(200));
			} else
			{
				target2 = ballPos.addNew(target2.multiplyNew(-200));
			}
			return target2;
		}
		
	}
	
	private class ShootState implements IRoleState
	{
		private KickSkill		skill;
		private boolean		kickSKillActive	= true;
		private boolean		safeMoveActive		= false;
		private AMoveSkill	mskill				= AMoveToSkill.createMoveToSkill();
		private long			waitTimer			= 0;
		private boolean		waited				= false;
		
		
		@Override
		public void doEntryActions()
		{
			if ((skill != null) && (skill.getMoveCon() != null))
			{
				skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
				skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			}
			DynamicPosition target = null;
			target = new DynamicPosition(getAiFrame().getTacticalField().getThrowInInfo().getPos());
			skill = new KickSkill(target);
			skill.setKickMode(EKickMode.FIXED_SPEED);
			skill.setMoveMode(EMoveMode.CHILL);
			skill.setKickSpeed(2);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((skill != null) && (skill.getMoveCon() != null))
			{
				skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
				skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			}
			DynamicPosition target = null;
			BotID receiver = null;
			for (ARole role : getAiFrame().getPlayStrategy().getActiveRoles(ERole.SECONDARY_AUTOMATED_THROW_IN))
			{
				target = new DynamicPosition(getAiFrame().getTacticalField().getThrowInInfo().getPos());
				log.trace("Placement shoot to bot: " + role.getBotID());
				receiver = role.getBotID();
			}
			if (target == null)
			{
				log.warn("Could not find secondary throw in helper, this is not good !");
				target = new DynamicPosition(getAiFrame().getTacticalField().getThrowInInfo().getPos());
			}
			if (waited && !isPathToTargetFree(target, receiver))
			{
				target = chooseNewTarget(target, receiver);
			}
			skill.setReceiver(target);
			
			if (!isPathToTargetFree(target, receiver) && (waitTimer == 0) && !waited && isBotBehindBall())
			{
				waitTimer = getWFrame().getTimestamp();
			} else if (!isPathToTargetFree(target, receiver)
					&& ((getWFrame().getTimestamp() - waitTimer) < OffensiveConstants
							.getAutomatedThrowInWaitForFreeSightTime()))
			{
				setNewSkill(new IdleSkill());
			} else if (getCurrentSkill().toString() != ESkill.KICK.toString())
			{
				waitTimer = 0;
				waited = true;
				skill = new KickSkill(target);
				skill.setKickMode(EKickMode.FIXED_SPEED);
				skill.setMoveMode(EMoveMode.CHILL);
				skill.setKickSpeed(2);
				setNewSkill(skill);
			}
			
			if ((GeoMath.distancePP(getWFrame().getBall().getPos(),
					getAiFrame().getTacticalField().getThrowInInfo().getPos()) < OffensiveConstants
							.getAutomatedThrowInPushDinstance()))
			{
				kickSKillActive = false;
				if (GeoMath.distancePP(getPos(), getWFrame().getBall().getPos()) < OffensiveConstants
						.getAutomatedThrowInPushDinstance())
				{
					IVector2 botDest = getWFrame().getBall().getPos()
							.addNew(new Vector2(-OffensiveConstants.getAutomatedThrowInPushDinstance() * 1.2, 0));
					if (!Geometry.getField().isPointInShape(botDest))
					{
						botDest = getWFrame().getBall().getPos()
								.addNew(new Vector2(OffensiveConstants.getAutomatedThrowInPushDinstance() * 1.2, 0));
					}
					if (!safeMoveActive)
					{
						safeMoveActive = true;
						mskill = AMoveToSkill.createMoveToSkill();
						setNewSkill(mskill);
					}
					mskill.getMoveCon().setPenaltyAreaAllowedTheir(true);
					mskill.getMoveCon().setPenaltyAreaAllowedOur(true);
					mskill.getMoveCon().updateDestination(botDest);
					mskill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
				} else
				{
					safeMoveActive = false;
					setNewSkill(new IdleSkill());
				}
			} else if (kickSKillActive == false)
			{
				safeMoveActive = false;
				kickSKillActive = true;
				skill = new KickSkill(target);
				skill.setKickMode(EKickMode.FIXED_SPEED);
				skill.setMoveMode(EMoveMode.CHILL);
				skill.setKickSpeed(2);
				setNewSkill(skill);
			}
		}
		
		
		private boolean isPathToTargetFree(final IVector2 target, final BotID receiver)
		{
			Collection<BotID> ignoredIds = new ArrayList<BotID>();
			ignoredIds.add(receiver);
			ignoredIds.add(getBotID());
			final double raySize = 40;
			return AiMath.p2pVisibility(getWFrame(), getWFrame().getBall().getPos(), target, raySize, ignoredIds);
		}
		
		
		private boolean isBotBehindBall()
		{
			/**
			 * This method is actually not needed because the robot will only enter the shootState
			 * when the robot is already positioned. So its ok, that this method always returns true.
			 */
			return true;
		}
		
		
		private DynamicPosition chooseNewTarget(final DynamicPosition target, final BotID receiver)
		{
			IVector2 newTarget = null;
			IVector2 ballToTargetNormal = target.subtractNew(getWFrame().getBall().getPos()).getNormalVector()
					.normalizeNew();
			
			newTarget = target.addNew(ballToTargetNormal.multiplyNew(50));
			int i = 0;
			while (!isPathToTargetFree(newTarget, receiver))
			{
				newTarget = target.addNew(ballToTargetNormal.multiplyNew(i * 50));
				if (i > 30)
				{
					log.warn("Could not find suitable pass Position for ball placement");
					break;
				}
				i++;
			}
			return new DynamicPosition(newTarget);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.SHOOT;
		}
	}
	
}
