package edu.tigers.sumatra.botmanager.botskills.data;

import lombok.Getter;


/**
 * The kicker mode that robot should kick with.
 */
public enum EKickerMode
{
	DISARM(0),
	ARM(1),
	FORCE(2),
	ARM_TIME(3),
	NONE(0x0F),

	;

	@Getter
	private final int id;


	EKickerMode(final int id)
	{
		this.id = id;
	}


	/**
	 * @param id
	 * @return enum
	 */
	public static EKickerMode fromId(final int id)
	{
		for (final EKickerMode mode : values())
		{
			if (mode.getId() == id)
			{
				return mode;
			}
		}
		return NONE;
	}
}
