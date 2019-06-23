/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.model.SumatraModel;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


/**
 * Berkeley database
 */
public class BerkeleyDb
{
	private static final Logger log = Logger.getLogger(BerkeleyDb.class.getName());
	
	private final BerkeleyEnv env = new BerkeleyEnv();
	private final Path dbPath;
	private final Map<Class<?>, IBerkeleyAccessor<?>> accessors = new HashMap<>();
	
	
	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	public BerkeleyDb(final Path dbPath)
	{
		if (dbPath.toString().endsWith(".zip"))
		{
			this.dbPath = Paths.get(dbPath.toString().substring(0, dbPath.toString().length() - 4));
			if (!this.dbPath.toFile().exists())
			{
				unpackDatabase(dbPath.toFile());
			}
		} else
		{
			this.dbPath = dbPath;
		}
	}
	
	
	/**
	 * @return a new empty unopened database at the default location
	 */
	public static BerkeleyDb withDefaultLocation()
	{
		return new BerkeleyDb(Paths.get(getDefaultBasePath(), getDefaultName()));
	}
	
	
	/**
	 * @param customLocation a custom absolute path to a database
	 * @return a new database handle with a custom name at the default base path
	 */
	public static BerkeleyDb withCustomLocation(final Path customLocation)
	{
		return new BerkeleyDb(customLocation);
	}
	
	
	/**
	 * @return the default name for a new database
	 */
	public static String getDefaultName()
	{
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		dt.setTimeZone(TimeZone.getDefault());
		return dt.format(new Date());
	}
	
	
	/**
	 * @return
	 */
	public static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty("edu.tigers.sumatra.persistence.basePath", "data/record");
	}
	
	
	public <T> void add(Class<T> clazz, IBerkeleyAccessor<T> accessor)
	{
		accessors.put(clazz, accessor);
	}
	
	
	public <T> T get(Class<T> clazz, long key)
	{
		IBerkeleyAccessor<T> accessor = getAccessor(clazz);
		if (accessor == null)
		{
			return null;
		}
		return accessor.get(key);
	}
	
	
	public <T> List<T> getAll(Class<T> clazz)
	{
		IBerkeleyAccessor<T> accessor = getAccessor(clazz);
		if (accessor == null)
		{
			return Collections.emptyList();
		}
		return accessor.load();
	}
	
	
	public <T> long size(Class<T> clazz)
	{
		IBerkeleyAccessor<T> accessor = getAccessor(clazz);
		if (accessor == null)
		{
			return 0;
		}
		return accessor.size();
	}
	
	
	public <T> void write(Class<T> clazz, Collection<T> elements)
	{
		IBerkeleyAccessor<T> accessor = getAccessor(clazz);
		if (accessor != null)
		{
			accessor.write(elements);
		}
	}
	
	
	public <T> void write(Class<T> clazz, T element)
	{
		IBerkeleyAccessor<T> accessor = getAccessor(clazz);
		if (accessor != null)
		{
			accessor.write(element);
		}
	}
	
	
	private <T> IBerkeleyAccessor<T> getAccessor(Class<T> clazz)
	{
		// noinspection unchecked
		return (IBerkeleyAccessor<T>) accessors.get(clazz);
	}
	
	
	private void unpackDatabase(final File file)
	{
		log.info("Unpacking database...");
		try
		{
			ZipFile zipFile = new ZipFile(file);
			zipFile.extractAll(dbPath.toFile().getParent());
			log.info("Unpacking finished.");
		} catch (ZipException e)
		{
			log.error("Unpacking failed.", e);
		}
	}
	
	
	/**
	 * Open Database
	 */
	public void open()
	{
		env.open(dbPath.toFile());
		accessors.values().forEach(a -> a.open(env.getEntityStore()));
	}
	
	
	/**
	 * Close database
	 */
	public void close()
	{
		env.close();
	}
	
	
	/**
	 * Delete the database from filesystem
	 *
	 * @throws IOException
	 */
	public void delete() throws IOException
	{
		if (env.isOpen())
		{
			throw new IllegalStateException("Database must be closed before deletion.");
		}
		FileUtils.deleteDirectory(dbPath.toFile());
	}
	
	
	/**
	 * Compress the database
	 * 
	 * @throws IOException
	 */
	public void compress() throws IOException
	{
		env.compress();
	}
	
	
	/**
	 * @return the env
	 */
	public final BerkeleyEnv getEnv()
	{
		return env;
	}
	
	
	public Long getFirstKey()
	{
		return accessors.values().stream()
				.filter(IBerkeleyAccessor::isSumatraTimestampBased)
				.map(IBerkeleyAccessor::getFirstKey)
				.filter(Objects::nonNull)
				.reduce(this::getSmallerKey)
				.orElse(null);
	}
	
	
	public Long getLastKey()
	{
		return accessors.values().stream()
				.filter(IBerkeleyAccessor::isSumatraTimestampBased)
				.map(IBerkeleyAccessor::getLastKey)
				.filter(Objects::nonNull)
				.reduce(this::getLargerKey)
				.orElse(null);
	}
	
	
	public Long getNextKey(long tCur)
	{
		return accessors.values().stream()
				.filter(IBerkeleyAccessor::isSumatraTimestampBased)
				.map(s -> s.getNextKey(tCur))
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}
	
	
	public Long getPreviousKey(long tCur)
	{
		return accessors.values().stream()
				.filter(IBerkeleyAccessor::isSumatraTimestampBased)
				.map(s -> s.getPreviousKey(tCur))
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}
	
	
	public Long getKey(long tCur)
	{
		return accessors.values().stream()
				.filter(IBerkeleyAccessor::isSumatraTimestampBased)
				.map(s -> s.getNearestKey(tCur))
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}
	
	
	private Long getLargerKey(final Long k1, final Long k2)
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
	
	
	private Long getSmallerKey(final Long k1, final Long k2)
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
	
	
	private Long getNearestKey(final long key, final Long k1, final Long k2)
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
		return dbPath.toString();
	}
}
