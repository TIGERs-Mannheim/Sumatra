/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.exceptions.PatternSerializerException;


/**
 * Serialize Pattern
 * 
 * @author osteinbrecher
 * 
 */
public class PatterListSerializer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private File	file;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param fileName
	 */
	public PatterListSerializer(String fileName)
	{
		file = new File(fileName);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param patternList
	 */
	public void writeFile(List<Pattern> patternList)
	{
		FileOutputStream fileStream = null;
		ObjectOutputStream objectStream = null;
		try
		{
			fileStream = new FileOutputStream(file);
			objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(patternList);
		} catch (final IOException e)
		{
			throw new PatternSerializerException(e);
		} finally
		{
			try
			{
				if (objectStream != null)
				{
					objectStream.close();
				}
				if (fileStream != null)
				{
					fileStream.close();
				}
			} catch (IOException err)
			{
				throw new PatternSerializerException(err);
			}
			
		}
	}
	
	
	/**
	 * @return
	 */
	public List<Pattern> readFile()
	{
		final List<Pattern> patternList = new ArrayList<Pattern>();
		FileInputStream fileStream = null;
		InputStream buffer = null;
		ObjectInput input = null;
		
		try
		{
			fileStream = new FileInputStream(file);
			
			// check if file is empty
			if (fileStream.available() != 0)
			{
				buffer = new BufferedInputStream(fileStream);
				input = new ObjectInputStream(buffer);
				@SuppressWarnings("unchecked")
				final List<Pattern> tmpList = (List<Pattern>) input.readObject();
				
				
				for (final Pattern pattern : tmpList)
				{
					patternList.add(new Pattern(pattern));
				}
			}
		} catch (final IOException e)
		{
			close(input, buffer, fileStream);
			throw new PatternSerializerException(e);
		} catch (final ClassNotFoundException err)
		{
			close(input, buffer, fileStream);
			throw new PatternSerializerException("class not found err", err);
		} finally
		{
			close(input, buffer, fileStream);
		}
		
		return patternList;
	}
	
	
	private void close(ObjectInput input, InputStream buffer, FileInputStream fileStream)
	{
		try
		{
			if (input != null)
			{
				input.close();
			}
		} catch (IOException e)
		{
			throw new PatternSerializerException(e);
		}
		try
		{
			if (buffer != null)
			{
				buffer.close();
			}
		} catch (IOException e)
		{
			throw new PatternSerializerException(e);
		}
		try
		{
			if (fileStream != null)
			{
				fileStream.close();
			}
		} catch (IOException e)
		{
			throw new PatternSerializerException(e);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Set new Logfile.
	 * 
	 * @param logFile
	 */
	public void setPath(String logFile)
	{
		file = new File(logFile);
	}
}
