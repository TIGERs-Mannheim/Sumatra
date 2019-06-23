/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.io.IOException;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPersistence
{
	
	/**
	 * Open Database
	 */
	void open();
	
	
	/**
	 * Close database
	 */
	void close();
	
	
	/**
	 * Delete the database from filesystem
	 * 
	 * @throws IOException
	 */
	void delete() throws IOException;
}
