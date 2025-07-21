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
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Synopsis of all match relevant data to control one bot.
 *
 * @author AndreR
 * @note curPosition and posDelay are filled by Base Station!
 */
public class TigerSystemMatchCtrl extends ACommand
{

	/**
	 * [mm], [mrad]
	 */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curPosition = new int[3];

	/**
	 * [Q6.2 ms]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int posDelay;

	/**
	 * Vision camera ID
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int camId;

	@SerialData(type = ESerialDataType.UINT8)
	private int flags;

	@SerialData(type = ESerialDataType.UINT8)
	private int skillId;

	/**
	 *
	 */
	@SerialData(type = ESerialDataType.TAIL)
	private byte[] skillData;

	private ABotSkill skill;

	private static final int UNUSED_FIELD = 0x7FFF;


	/**
	 * Constructor.
	 */
	public TigerSystemMatchCtrl()
	{
		super(ECommand.CMD_SYSTEM_MATCH_CTRL);

		curPosition[0] = UNUSED_FIELD;
		curPosition[1] = UNUSED_FIELD;
		curPosition[2] = UNUSED_FIELD;
	}


	/**
	 * @param matchCtrl
	 */
	public TigerSystemMatchCtrl(final MatchCommand matchCtrl)
	{
		this();
		setSkill(matchCtrl.getSkill());
		setMultimediaControl(matchCtrl.getMultimediaControl());
	}


	/**
	 * @param skill
	 */
	public void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
		skillData = BotSkillFactory.getInstance().encode(skill);
		skillId = skill.getType().getId();
	}


	/**
	 * @return the skill
	 */
	public final ABotSkill getSkill()
	{
		if (skill == null)
		{
			skill = BotSkillFactory.getInstance().decode(skillData, skillId);
		}
		return skill;
	}


	/**
	 * @param song Sing a song :)
	 */
	private void setSong(final ESong song)
	{
		flags &= ~(0x1F);

		flags |= (song.getId() & 0x1F);
	}


	private ESong getSong()
	{
		return ESong.getSongConstant(flags & 0x1F);
	}


	private void setLEDs(ELedColor ledColor)
	{
		flags &= ~0xE0;
		flags |= ledColor.getId() << 5;
	}


	private ELedColor getLEDs()
	{
		return ELedColor.getLedColorConstant((flags & 0xE0) >> 5);
	}


	public void setMultimediaControl(final MultimediaControl control)
	{
		setLEDs(control.getLedColor());
		setSong(control.getSong());
	}


	public MultimediaControl getMultimediaControl()
	{
		return new MultimediaControl(getLEDs(), getSong());
	}
}
