/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.BotSkillFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.IMatchCommand;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Synopsis of all match relevant data to control one bot.
 * 
 * @note curPosition and posDelay are filled by Base Station!
 * @author AndreR
 */
public class TigerSystemMatchCtrl extends ACommand implements IMatchCommand
{
	/** */
	public static final int		MAX_SKILL_DATA_SIZE	= 12;
	
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private final int				curPosition[]			= new int[3];
	
	/** [Q6.2 ms] */
	@SerialData(type = ESerialDataType.UINT8)
	private int						posDelay;
	
	/** Vision camera ID */
	@SerialData(type = ESerialDataType.UINT8)
	private int						camId;
	
	/** [Q3.5 m/s] */
	@SerialData(type = ESerialDataType.UINT8)
	private int						kickSpeed;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						kickFlags;
	
	/** [rpm/100] */
	@SerialData(type = ESerialDataType.UINT8)
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
	
	private ABotSkill				skill;
	
	private static final int	UNUSED_FIELD			= 0x7FFF;
	
	
	/** */
	public TigerSystemMatchCtrl()
	{
		super(ECommand.CMD_SYSTEM_MATCH_CTRL);
		
		curPosition[0] = UNUSED_FIELD;
		curPosition[1] = UNUSED_FIELD;
		curPosition[2] = UNUSED_FIELD;
		
		setSkill(new BotSkillMotorsOff());
	}
	
	
	/**
	 * @param matchCtrl
	 */
	public TigerSystemMatchCtrl(final MatchCommand matchCtrl)
	{
		this();
		setSkill(matchCtrl.getSkill());
		setDribblerSpeed(matchCtrl.getDribbleSpeed());
		setFeedbackFreq(matchCtrl.getFeedbackFreq());
		setKickerAutocharge(matchCtrl.isAutoCharge());
		setKick(matchCtrl.getKickSpeed(), matchCtrl.getDevice(), matchCtrl.getMode());
		setCheering(matchCtrl.isCheer());
		setLEDs(matchCtrl.isLeftRed(), matchCtrl.isLeftGreen(), matchCtrl.isRightRed(), matchCtrl.isRightGreen());
		setSongFinalCountdown(matchCtrl.isSetSongFinalCountdown());
	}
	
	
	/**
	 * @param skill
	 */
	@Override
	public void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
		skillData = BotSkillFactory.getInstance().encode(skill);
		skillId = skill.getType().getId();
	}
	
	
	/**
	 * @return the skill
	 */
	@Override
	public final ABotSkill getSkill()
	{
		return skill;
	}
	
	
	/**
	 * @param speed Dribbler speed in RPM.
	 * @note Speed must always be positive.
	 */
	@Override
	public void setDribblerSpeed(final double speed)
	{
		if (speed < 0)
		{
			dribblerSpeed = 0;
		} else
		{
			dribblerSpeed = ((int) (speed + 50.0)) / 100;
		}
	}
	
	
	/**
	 * @param freq
	 */
	@Override
	public void setFeedbackFreq(final int freq)
	{
		feedbackFreq = freq;
	}
	
	
	/**
	 * @param enable
	 */
	@Override
	public void setKickerAutocharge(final boolean enable)
	{
		kickFlags &= ~(0x80);
		
		if (enable)
		{
			kickFlags |= 0x80;
		}
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param kickSpeed [m/s]
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	@Override
	public void setKick(final double kickSpeed, final EKickerDevice device, final EKickerMode mode)
	{
		setKick(kickSpeed, device.getValue(), mode.getId());
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param kickSpeed [m/s]
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	public void setKick(final double kickSpeed, final int device, final int mode)
	{
		int kickSpeedA = (int) (kickSpeed);
		int kickSpeedB = ((((int) (kickSpeed * 1000)) % 1000) + 15) / 32;
		
		if (kickSpeed > 7.96875)
		{
			kickSpeedA = 7;
			kickSpeedB = 31;
		}
		
		this.kickSpeed = (kickSpeedA << 5) | kickSpeedB;
		kickFlags &= ~(0x7F); // setAutocharge also modifies a bit in this field
		kickFlags |= device | (mode << 1);
	}
	
	
	/**
	 * Enable the robots super-mega-top-secret and ultra-annoying cheering functionality.
	 * 
	 * @param enable
	 * @note Expect severe joy and enthusiasm in the crowd!
	 */
	@Override
	public void setCheering(final boolean enable)
	{
		if (enable)
		{
			flags |= 0x02;
		} else
		{
			flags &= ~0x02;
		}
	}
	
	
	/**
	 * Start playing the intro of "Final Countdown" by Europe
	 * 
	 * @param enable
	 */
	@Override
	public void setSongFinalCountdown(final boolean enable)
	{
		if (enable)
		{
			flags |= 0x04;
		} else
		{
			flags &= ~0x04;
		}
	}
	
	
	/**
	 * @param leftRed
	 * @param leftGreen
	 * @param rightRed
	 * @param rightGreen
	 */
	@Override
	public void setLEDs(final boolean leftRed, final boolean leftGreen, final boolean rightRed, final boolean rightGreen)
	{
		flags &= ~0xF0;
		
		if (leftRed)
		{
			flags |= 0x10;
		}
		
		if (leftGreen)
		{
			flags |= 0x20;
		}
		
		if (rightRed)
		{
			flags |= 0x40;
		}
		
		if (rightGreen)
		{
			flags |= 0x80;
		}
	}
}
