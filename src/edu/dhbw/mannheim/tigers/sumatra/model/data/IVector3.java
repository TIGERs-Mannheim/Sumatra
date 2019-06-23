/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.10.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * This interface allows transparent access to {@link Vector3} and {@link Vector3f}
 * 
 * @see Vector3
 * @see Vector3f
 * 
 * @see Vector2
 * @see Vector2f
 * 
 * @author Gero
 * 
 */
public interface IVector3 extends IVector2
{
	/**
	 * @return The Z part of the vector
	 */
	public float z();
}
