/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ids;

/**
 * Uninitialized object id.
 */
public class UninitializedID extends AObjectID
{
	private static final UninitializedID DEFAULT_INSTANCE = new UninitializedID();


	/**
	 * Instead of creating a new instance, use the field from AObjectID
	 */
	public UninitializedID()
	{
		super(AObjectID.UNINITIALIZED_ID);
	}


	/**
	 * @return default instance
	 */
	public static UninitializedID instance()
	{
		return DEFAULT_INSTANCE;
	}
}
