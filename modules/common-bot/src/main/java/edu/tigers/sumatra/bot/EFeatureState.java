package edu.tigers.sumatra.bot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EFeatureState
{
	/**  */
	WORKING(0),
	/**  */
	KAPUT(2),
	/**  */
	UNKNOWN(0xFF);

	private final int	id;


	EFeatureState(final int id)
	{
		this.id = id;
	}


	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EFeatureState getFeatureStateConstant(final int id)
	{
		for (EFeatureState s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return UNKNOWN;
	}
}
