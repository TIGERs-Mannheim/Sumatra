/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.EntityModel;

import edu.tigers.sumatra.persistence.proxy.ColorProxy;
import edu.tigers.sumatra.persistence.proxy.ConcurrentHashMapProxy;
import edu.tigers.sumatra.persistence.proxy.EnumMapProxy;
import edu.tigers.sumatra.persistence.proxy.LinkedHashSetProxy;
import edu.tigers.sumatra.persistence.proxy.TreeMapProxy;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;


/**
 * This environment class manages a berkeley database.
 * It will be used to open und close a entity store
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyEnv
{
	private static final Logger log = Logger.getLogger(BerkeleyEnv.class.getName());
	private File envHome;
	
	private DatabaseSession session = new DatabaseSession();
	
	private EntityModel model = new AnnotationModel();
	private EnvironmentConfig myEnvConfig = new EnvironmentConfig();
	private StoreConfig storeConfig = new StoreConfig();
	
	private static final Map<File, DatabaseSession> SESSIONS = new HashMap<>();
	
	
	public BerkeleyEnv()
	{
		model.registerClass(ColorProxy.class);
		model.registerClass(EnumMapProxy.class);
		model.registerClass(ConcurrentHashMapProxy.class);
		model.registerClass(LinkedHashSetProxy.class);
		model.registerClass(TreeMapProxy.class);
	}
	
	
	/**
	 * Open the database
	 *
	 * @param envFile path to database
	 */
	public void open(final File envFile)
	{
		if (isOpen())
		{
			throw new IllegalStateException("Environment is only opened.");
		}
		envHome = envFile.getAbsoluteFile();
		DatabaseSession existingSession = SESSIONS.get(envHome);
		if (existingSession == null)
		{
			createFolders(envHome);
			
			// Open the environment and entity store
			myEnvConfig.setAllowCreate(true);
			storeConfig.setAllowCreate(true);
			storeConfig.setModel(model);
			storeConfig.setDeferredWrite(false);
			session.myEnv = new Environment(envHome, myEnvConfig);
			session.store = new EntityStore(session.myEnv, "EntityStore", storeConfig);
			myEnvConfig = null;
			storeConfig = null;
			model = null;
			attachShutDownHook();
			
			SESSIONS.put(envHome, session);
		} else
		{
			session = existingSession;
		}
		
		session.numHandles++;
	}
	
	
	private void createFolders(final File envHome)
	{
		if (!envHome.exists())
		{
			boolean mkdirs = envHome.mkdirs();
			if (!mkdirs)
			{
				log.error("Could not create " + envHome);
			}
		}
	}
	
	
	/**
	 * Close the store and environment
	 * 
	 * @return true, if env was closed
	 */
	public void close()
	{
		if (!isOpen())
		{
			log.warn("BerkeleyEnv already closed: " + envHome);
			return;
		}
		
		session.numHandles--;
		if (session.numHandles > 0)
		{
			return;
		}
		try
		{
			session.store.close();
			session.store = null;
		} catch (DatabaseException dbe)
		{
			log.error("Error closing store", dbe);
		}
		try
		{
			session.myEnv.close();
			session.myEnv = null;
		} catch (DatabaseException dbe)
		{
			log.error("Error closing myEnv", dbe);
		}
		
		if (session.compressOnClose)
		{
			try
			{
				compress();
			} catch (IOException e)
			{
				log.error("Could not compress database.", e);
			}
		}
		
		SESSIONS.remove(envHome);
	}
	
	
	/**
	 * Compress the database
	 *
	 * @throws IOException
	 */
	public void compress() throws IOException
	{
		if (isOpen())
		{
			throw new IOException("Database must be closed before deletion.");
		}
		File zipFileHandle = new File(envHome.getParentFile(), envHome.getName() + ".zip");
		if (zipFileHandle.exists())
		{
			return;
		}
		ZipParameters zipParams = new ZipParameters();
		zipParams.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		zipParams.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
		log.info("Compressing database...");
		long tStart = System.nanoTime();
		try
		{
			ZipFile zipFile = new ZipFile(zipFileHandle);
			zipFile.addFolder(envHome, zipParams);
		} catch (ZipException e)
		{
			throw new IOException("Could not compress database.", e);
		}
		double duration = (System.nanoTime() - tStart) / 1e9;
		
		String fileSize = FileUtils.byteCountToDisplaySize(zipFileHandle.length());
		log.info(String.format("Compressed database in %.2fs to %s", duration, fileSize));
	}
	
	
	private void attachShutDownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (isOpen())
			{
				log.warn("Database " + envHome + " was open on shutdown. It would be better to close it explicitly.");
				close();
			}
		}, "DB Shutdown"));
	}
	
	
	/**
	 * Return a handle to the entity store
	 * 
	 * @return
	 */
	public EntityStore getEntityStore()
	{
		return session.store;
	}
	
	
	public EntityModel getModel()
	{
		return model;
	}
	
	
	public StoreConfig getStoreConfig()
	{
		return storeConfig;
	}
	
	
	/**
	 * @return
	 */
	public boolean isOpen()
	{
		return session.numHandles > 0;
	}
	
	
	public void setCompressOnClose(final boolean compressOnClose)
	{
		session.compressOnClose = compressOnClose;
	}
	
	
	private static class DatabaseSession
	{
		Environment myEnv;
		EntityStore store;
		boolean compressOnClose = false;
		int numHandles = 0;
	}
}
