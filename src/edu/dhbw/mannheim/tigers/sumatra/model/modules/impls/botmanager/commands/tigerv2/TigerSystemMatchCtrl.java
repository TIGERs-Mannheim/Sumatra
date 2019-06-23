/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.BotSkillFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Synopsis of all match relevant data to control one bot.
 * 
 * @note curPosition and posDelay are filled by Base Station!
 * @author AndreR
 */
public class TigerSystemMatchCtrl extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public static final int		MAX_SKILL_DATA_SIZE	= 12;
	
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private int						curPosition[]			= new int[3];
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						posDelay;
	
	/** [us] */
	@SerialData(type = ESerialDataType.UINT16)
	private int						kickDuration;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						kickFlags;
	
	/** [rpm] */
	@SerialData(type = ESerialDataType.UINT16)
	private int						dribblerSpeed;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						skillId;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						flags;
	
	/** [Hz] */
	@SerialData(type = ESerialDataType.UINT8)
	private int						feedbackFreq;
	
	/** */
	@SerialData(type = ESerialDataType.TAIL)
	private byte					skillData[];
	
	private static final int	UNUSED_FIELD			= 0x7FFF;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerSystemMatchCtrl()
	{
		super(ECommand.CMD_SYSTEM_MATCH_CTRL);
		
		curPosition[0] = UNUSED_FIELD;
		curPosition[1] = UNUSED_FIELD;
		curPosition[2] = UNUSED_FIELD;
		
		setSkill(new BotSkillMotorsOff());
		
		setController(true);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param skill
	 */
	public void setSkill(final ABotSkill skill)
	{
		skillData = BotSkillFactory.getInstance().encode(skill);
		skillId = skill.getType().getId();
	}
	
	
	/**
	 * Set target velocity for velocity controller.
	 * 
	 * @param xy Velocity in [m/s]
	 * @param orientation Rotational speed in [rad/s]
	 */
	public void setVelocity(final IVector2 xy, final float orientation)
	{
		setSkill(new BotSkillLocalVelocity(xy, orientation));
	}
	
	
	/**
	 * @param duration Kick duration in [us]
	 */
	public void setKickDuration(final float duration)
	{
		kickDuration = (int) duration;
	}
	
	
	/**
	 * @param speed Dribbler speed in RPM.
	 * @note Negative values reverse turning direction (push ball away).
	 */
	public void setDribblerSpeed(final float speed)
	{
		dribblerSpeed = (int) speed;
	}
	
	
	/**
	 * @param freq
	 */
	public void setFeedbackFreq(final int freq)
	{
		feedbackFreq = freq;
	}
	
	
	/**
	 * @param enable
	 * @param sequence increasing sequence number to implement flank-triggered data processing
	 */
	public void setKickerAutocharge(final boolean enable, final int sequence)
	{
		kickFlags &= ~(0xF8);
		kickFlags |= ((sequence & 0x0F) << 4);
		
		if (enable)
		{
			kickFlags |= 0x08;
		}
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param duration Firing duration in [us]
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @param sequence increasing sequence number to implement flank-triggered data processing
	 * @note This command requires a state-aware instance to keep track of older values and the sequence.
	 */
	public void setKick(final float duration, final EKickerDevice device, final EKickerMode mode, final int sequence)
	{
		setKick(duration, device.getValue(), mode.getId(), sequence);
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param duration Firing duration in [us]
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @param sequence increasing sequence number to implement flank-triggered data processing
	 * @note This command requires a state-aware instance to keep track of older values and the sequence.
	 */
	public void setKick(final float duration, final int device, final int mode, final int sequence)
	{
		kickDuration = (int) duration;
		kickFlags &= ~(0xF3); // setAutocharge also modifies a bit in this field
		kickFlags |= device | (mode << 1) | ((sequence & 0x0F) << 4);
	}
	
	
	/**
	 * Enable/Disable motor controllers.
	 * 
	 * @param enable
	 */
	public void setController(final boolean enable)
	{
		if (!enable)
		{
			setSkill(new BotSkillMotorsOff());
		}
	}
	
	
	/**
	 * Enable the robots super-mega-top-secret and ultra-annoying cheering functionality.
	 * 
	 * @param enable
	 * @note Expect severe joy and enthusiasm in the crowd!
	 */
	public void setCheering(final boolean enable)
	{
		if (enable)
		{
			flags |= 0x02;
		}
		else
		{
			flags &= ~0x02;
		}
	}
	
	
	/**
	 * Enable limited velocity mode.
	 * 
	 * @param enable
	 */
	public void setLimitedVelocity(final boolean enable)
	{
		if (enable)
		{
			flags |= 0x01;
		}
		else
		{
			flags &= ~0x01;
		}
	}
}
