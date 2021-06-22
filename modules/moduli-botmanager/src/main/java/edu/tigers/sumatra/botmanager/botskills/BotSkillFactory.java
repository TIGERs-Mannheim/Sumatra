/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.botmanager.serial.SerialDescription;
import edu.tigers.sumatra.botmanager.serial.SerialException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Create bot skills
 *
 * @author andre
 */
public final class BotSkillFactory
{
	private static final Logger log = LogManager.getLogger(BotSkillFactory.class.getName());

	private static final int MAX_SKILL_DATA_SIZE = 16;

	private static BotSkillFactory instance = null;

	private final Map<Integer, SerialDescription> skills = new HashMap<>();


	private BotSkillFactory()
	{
	}


	/**
	 * Get singleton instance.
	 *
	 * @return
	 */
	public static synchronized BotSkillFactory getInstance()
	{
		if (instance == null)
		{
			instance = new BotSkillFactory();
		}
		return instance;
	}


	/**
	 * Call once per application lifetime to parse all commands.
	 */
	public void loadSkills()
	{
		skills.clear();

		for (EBotSkill esk : EBotSkill.values())
		{
			getSerialDescription(esk).ifPresent(d -> skills.put(esk.getId(), d));
		}
	}


	private Optional<SerialDescription> getSerialDescription(final EBotSkill esk)
	{
		SerialDescription desc;
		try
		{
			desc = new SerialDescription(esk.getInstanceableClass().getImpl());

			// do sanity checks for encode and decode - should not throw any exception
			desc.decode(desc.encode(desc.newInstance()));
		} catch (SerialException err)
		{
			log.error("Could not load bot skill: " + esk, err);
			return Optional.empty();
		}

		ABotSkill ask;
		try
		{
			ask = (ABotSkill) desc.newInstance();
		} catch (SerialException err)
		{
			log.error("Could not create instance of: " + esk, err);
			return Optional.empty();
		} catch (ClassCastException err)
		{
			log.error(esk + " is not based on ABotSkill!", err);
			return Optional.empty();
		}

		if (ask.getType().getId() != esk.getId())
		{
			log.error("EBotSkill id mismatch in bot skill: " + esk + ". The bot skill does not use the correct enum.");
			return Optional.empty();
		}

		if (skills.get(esk.getId()) != null)
		{
			log.error(esk + "'s skill code is already defined by: "
					+ skills.get(esk.getId()).getClass().getName());
			return Optional.empty();
		}
		return Optional.of(desc);
	}


	/**
	 * Parse byte data and create command.
	 *
	 * @param data
	 * @param skillId
	 * @return
	 */
	public ABotSkill decode(final byte[] data, final int skillId)
	{
		if (!skills.containsKey(skillId))
		{
			log.warn("Unknown skill: " + skillId + ", length: " + data.length);
			return null;
		}

		SerialDescription skillDesc = skills.get(skillId);

		ABotSkill ask;

		try
		{
			ask = (ABotSkill) skillDesc.decode(data);
		} catch (SerialException err)
		{
			log.error("Could not parse cmd: " + skillId, err);
			return null;
		}

		return ask;
	}


	/**
	 * Encode command in byte stream.
	 *
	 * @param skill
	 * @return
	 */
	public byte[] encode(final ABotSkill skill)
	{
		int skillId = skill.getType().getId();

		if (!skills.containsKey(skillId))
		{
			log.error("No description for skill: " + skillId);
			return new byte[0];
		}

		SerialDescription skillDesc = skills.get(skillId);

		byte[] skillData;
		try
		{
			skillData = skillDesc.encode(skill);
		} catch (SerialException err)
		{
			log.error("Could not encode skill: " + skillId, err);
			return new byte[0];
		}

		if (skillData.length > MAX_SKILL_DATA_SIZE)
		{
			log.error("Skill " + skill.getType() + " exceeds data usage limit of " + MAX_SKILL_DATA_SIZE);
			return new byte[0];
		}

		return skillData;
	}


	/**
	 * @param skill
	 * @return
	 */
	public int getLength(final ABotSkill skill)
	{
		int length;
		int skillId = skill.getType().getId();

		if (!skills.containsKey(skillId))
		{
			log.error("No description for skill: " + skillId);
			return 0;
		}

		SerialDescription skillDesc = skills.get(skillId);

		try
		{
			length = skillDesc.getLength(skill);
		} catch (SerialException err)
		{
			log.error("Could not get length of: " + skill.getType(), err);
			return 0;
		}

		return length;
	}
}
