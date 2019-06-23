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
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.other.ESong;
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
	public static final int			MAX_SKILL_DATA_SIZE	= 15;
	
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private final int[]				curPosition				= new int[3];
	
	/** [Q6.2 ms] */
	@SerialData(type = ESerialDataType.UINT8)
	private int							posDelay;
	
	/** Vision camera ID */
	@SerialData(type = ESerialDataType.UINT8)
	private int							camId;
	
	/** [Hz] */
	@SerialData(type = ESerialDataType.UINT8)
	private int							feedbackFreq;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							flags;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							skillId;
	
	/** */
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]						skillData;
	
	private ABotSkill					skill;
	private EDataAcquisitionMode	acqMode					= EDataAcquisitionMode.NONE;
	
	private static final int		UNUSED_FIELD			= 0x7FFF;
	
	
	/** Constructor. */
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
		setDataAcquisitionMode(matchCtrl.getDataAcquisitionMode());
		setSkill(matchCtrl.getSkill());
		setFeedbackFreq(matchCtrl.getFeedbackFreq());
		setKickerAutocharge(matchCtrl.isAutoCharge());
		setMultimediaControl(matchCtrl.getMultimediaControl());
	}
	
	
	/**
	 * @param skill
	 */
	@Override
	public void setSkill(final ABotSkill skill)
	{
		switch (skill.getType())
		{
			case GLOBAL_POSITION:
			case GLOBAL_VELOCITY:
			case GLOBAL_VEL_XY_POS_W:
			case LOCAL_VELOCITY:
			case WHEEL_VELOCITY:
				AMoveBotSkill moveSkill = (AMoveBotSkill) skill;
				moveSkill.setDataAcquisitionMode(acqMode);
				break;
			default:
				break;
		}
		
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
		flags &= ~(0x08);
		
		if (enable)
		{
			flags |= 0x08;
		}
	}
	
	
	/**
	 * @param song Sing a song :)
	 */
	private void setSong(final ESong song)
	{
		flags &= ~(0x03);
		
		flags |= (song.getId() & 0x03);
	}
	
	
	/**
	 * @param leftRed
	 * @param leftGreen
	 * @param rightRed
	 * @param rightGreen
	 */
	private void setLEDs(final boolean leftRed, final boolean leftGreen, final boolean rightRed,
			final boolean rightGreen)
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
	
	
	@Override
	public void setMultimediaControl(final MultimediaControl control)
	{
		setLEDs(control.isLeftRed(), control.isLeftGreen(), control.isRightRed(), control.isRightGreen());
		setSong(control.getSong());
	}
	
	
	@Override
	public void setDataAcquisitionMode(final EDataAcquisitionMode acqMode)
	{
		this.acqMode = acqMode;
	}
}
