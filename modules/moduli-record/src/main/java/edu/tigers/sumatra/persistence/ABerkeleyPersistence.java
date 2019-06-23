/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


/**
 * Abstract berkeley persistence for all data independent actions.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABerkeleyPersistence implements IFrameByTimestampPersistence, IPersistence
{
	private static final Logger log = Logger.getLogger(ABerkeleyPersistence.class.getName());
	
	private final BerkeleyEnv env = new BerkeleyEnv();
	private final String dbPath;
	
	
	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	protected ABerkeleyPersistence(final String dbPath)
	{
		if (dbPath.endsWith(".zip"))
		{
			this.dbPath = dbPath.substring(0, dbPath.length() - 4);
			if (!new File(this.dbPath).exists())
			{
				unpackDatabase(dbPath);
			}
		} else
		{
			this.dbPath = dbPath;
		}
	}
	
	
	public void setCompressOnClose(final boolean compressOnClose)
	{
		env.setCompressOnClose(compressOnClose);
	}
	
	
	private void unpackDatabase(final String dbPath)
	{
		log.info("Unpacking database...");
		try
		{
			ZipFile zipFile = new ZipFile(new File(dbPath));
			zipFile.extractAll(new File(this.dbPath).getParent());
			log.info("Unpacking finished.");
		} catch (ZipException e)
		{
			log.error("Unpacking failed.", e);
		}
	}
	
	
	@Override
	public void open()
	{
		File envHome = new File(this.dbPath);
		env.open(envHome);
	}
	
	
	/**
	 * Close this database
	 */
	@Override
	public void close()
	{
		env.close();
	}
	
	
	@Override
	public void delete() throws IOException
	{
		if (env.isOpen())
		{
			throw new IllegalStateException("Database must be closed before deletion.");
		}
		File envHome = new File(this.dbPath);
		FileUtils.deleteDirectory(envHome);
	}
	
	
	/**
	 * @return the env
	 */
	protected final BerkeleyEnv getEnv()
	{
		return env;
	}
	
	
	protected Long getLargerKey(final Long k1, final Long k2)
	{
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		if (k1 > k2)
		{
			return k1;
		}
		return k2;
	}
	
	
	protected Long getSmallerKey(final Long k1, final Long k2)
	{
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		if (k1 < k2)
		{
			return k1;
		}
		return k2;
	}
	
	
	protected Long getNearestKey(final long key, final Long k1, final Long k2)
	{
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		long diff1 = Math.abs(key - k1);
		long diff2 = Math.abs(key - k2);
		
		if (diff1 < diff2)
		{
			return k1;
		}
		return k2;
	}
	
	
	/**
	 * @return the db path
	 */
	public String getDbPath()
	{
		return dbPath;
	}
}
