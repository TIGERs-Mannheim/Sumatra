/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillFactory;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.botskills.data.IMatchCommand;
import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
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
	
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curPosition = new int[3];
	
	/** [Q6.2 ms] */
	@SerialData(type = ESerialDataType.UINT8)
	private int posDelay;
	
	/** Vision camera ID */
	@SerialData(type = ESerialDataType.UINT8)
	private int camId;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int flags;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int skillId;
	
	/** */
	@SerialData(type = ESerialDataType.TAIL)
	private byte[] skillData;
	
	private ABotSkill skill;
	
	private static final int UNUSED_FIELD = 0x7FFF;
	
	
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
		setSkill(matchCtrl.getSkill());
		setKickerAutocharge(matchCtrl.isAutoCharge());
		setMultimediaControl(matchCtrl.getMultimediaControl());
		setStrictVelocityLimit(matchCtrl.isStrictVelocityLimit());
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
		flags &= ~(0x07);
		
		flags |= (song.getId() & 0x07);
	}
	
	
	/**
	 * @param enable
	 */
	@Override
	public void setStrictVelocityLimit(final boolean enable)
	{
		flags &= ~(0x80);
		
		if (enable)
		{
			flags |= 0x80;
		}
	}
	
	
	private void setLEDs(ELedColor ledColor)
	{
		flags &= ~0x70;
		flags |= ledColor.getId() << 4;
	}
	
	
	@Override
	public void setMultimediaControl(final MultimediaControl control)
	{
		setLEDs(control.getLedColor());
		setSong(control.getSong());
	}
}
