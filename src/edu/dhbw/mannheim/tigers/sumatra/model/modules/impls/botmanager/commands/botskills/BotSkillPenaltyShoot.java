/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author KaiE
 */
public class BotSkillPenaltyShoot extends ABotSkill
{
	
	/**
	 * @author KaiE
	 */
	public enum EPenaltyShootFlags
	{
		/** */
		NO_OP(0),
		/** if this flag is set the direction to turn is left */
		TURNING_LEFT(1),
		/** if this flag is set the direction to turn is right */
		TURNING_RIGHT(TURNING_LEFT.getValue() << 1),
		/** during the turn the dribbler will be activated */
		USING_DRIBBLER(TURNING_RIGHT.getValue() << 1),
		/***/
		FORWARD_MOVEMENT_LEVEL_1(USING_DRIBBLER.getValue() << 1),
		/***/
		FORWARD_MOVEMENT_LEVEL_2((FORWARD_MOVEMENT_LEVEL_1.getValue() << 1) | FORWARD_MOVEMENT_LEVEL_1.getValue()),
		/***/
		FORWARD_MOVEMENT_LEVEL_3((FORWARD_MOVEMENT_LEVEL_2.getValue() << 1) | FORWARD_MOVEMENT_LEVEL_2.getValue());
		
		
		private EPenaltyShootFlags(final int binval)
		{
			value = binval;
		}
		
		private final int	value;
		
		
		/**
		 * @return the flag value
		 */
		public int getValue()
		{
			return value;
		}
	}
	
	@SerialData(type = ESerialDataType.INT16)
	private int	penaltyPos[]	= new int[2];	// move to penalty
	@SerialData(type = ESerialDataType.INT16)
	private int	ballPos[]		= new int[2];	// adjust position to ball
	@SerialData(type = ESerialDataType.UINT16)
	private int	timetoShoot;						// the time to shoot
	@SerialData(type = ESerialDataType.UINT8)
	private int	flags;								// e.g. TURNING_LEFT // USING_DRIBBLER
															
															
	/**
	 */
	public BotSkillPenaltyShoot()
	{
		super(EBotSkill.PENALTY_SHOOT);
	}
	
	
	/**
	 * @param penPos
	 * @param ballPosition
	 * @param millistoShoot
	 * @param flg1
	 * @param flg2
	 * @param flg3
	 */
	public BotSkillPenaltyShoot(final IVector2 penPos, final IVector2 ballPosition, final Integer millistoShoot,
			final EPenaltyShootFlags flg1, final EPenaltyShootFlags flg2, final EPenaltyShootFlags flg3)
	{
		super(EBotSkill.PENALTY_SHOOT);
		
		penaltyPos[0] = (int) (penPos.x());
		penaltyPos[1] = (int) (penPos.y());
		
		ballPos[0] = (int) (ballPosition.x());
		ballPos[1] = (int) (ballPosition.y());
		
		timetoShoot = millistoShoot;
		flags = 0;
		flags |= flg1.getValue();
		flags |= flg2.getValue();
		flags |= flg3.getValue();
	}
	
	
	/**
	 * @param pos
	 */
	public void updateBallPos(final IVector2 pos)
	{
		ballPos[0] = (int) (pos.x());
		ballPos[1] = (int) (pos.y());
	}
	
	
	/**
	 * @param flag
	 */
	public void removeFlag(final EPenaltyShootFlags flag)
	{
		flags &= ~flag.getValue();
	}
	
	
	/**
	 * @param flag
	 */
	public void addFlag(final EPenaltyShootFlags flag)
	{
		flags |= flag.getValue();
	}
}
