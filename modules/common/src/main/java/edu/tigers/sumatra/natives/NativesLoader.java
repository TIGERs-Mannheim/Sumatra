/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.02.2012
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.natives;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.natives.OsDetector.EOsArch;
import edu.tigers.sumatra.natives.OsDetector.EOsName;
import edu.tigers.sumatra.natives.OsDetector.OsIdentifier;


/**
 * This class loads native library with a given name from a previously defined subfolder, depending on the os and
 * architecture.
 * 
 * @see #loadLibrary(String)
 * @author Gero
 */
public class NativesLoader
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger							log							= Logger.getLogger(NativesLoader.class
																											.getName());
	
	private static final String							JAVA_LIBRARY_PATH_KEY	= "java.library.path";
	
	/**  */
	public static final Map<OsIdentifier, String>	DEFAULT_FOLDER_MAP		= new HashMap<OsIdentifier, String>();
	
	static
	{
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.WINDOWS, EOsArch.x86), "win32");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.WINDOWS, EOsArch.x64), "win64");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.UNIX, EOsArch.x86), "unix32");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.UNIX, EOsArch.x64), "unix64");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.MAC, EOsArch.x86), "mac");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.MAC, EOsArch.x64), "mac");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.MAC, EOsArch.PPC), "ppc");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.SOLARIS, EOsArch.x86), "sol86");
		DEFAULT_FOLDER_MAP.put(new OsIdentifier(EOsName.SOLARIS, EOsArch.SPARC), "sparc");
	}
	
	private static final String							DELIMITER					= System.getProperty("file.separator");
	
	private String												basePath;
	private final Map<OsIdentifier, String>			folderMap					= new HashMap<OsIdentifier, String>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param basePath
	 */
	public NativesLoader(final String basePath)
	{
		this(basePath, DEFAULT_FOLDER_MAP, false);
	}
	
	
	/**
	 * @param basePath
	 * @param applyLibraryPathHack
	 */
	public NativesLoader(final String basePath, final boolean applyLibraryPathHack)
	{
		this(basePath, DEFAULT_FOLDER_MAP, applyLibraryPathHack);
	}
	
	
	/**
	 * @param basePath
	 * @param folderMap
	 * @param applyLibraryPathHack
	 */
	public NativesLoader(final String basePath, final Map<OsIdentifier, String> folderMap,
			final boolean applyLibraryPathHack)
	{
		super();
		setBasePath(basePath);
		
		// Check for leading/trailing delimiters
		for (final Entry<OsIdentifier, String> entry : folderMap.entrySet())
		{
			String folder = entry.getValue();
			
			if (!folder.startsWith("\\") && !folder.startsWith("/"))
			{
				// Add leading delimiter
				folder = DELIMITER + folder;
			}
			
			if (folder.endsWith("\\"))
			{
				// Cut trailing delimiter
				folder = folder.substring(0, folder.length() - 2);
			} else if (folder.endsWith("/"))
			{
				// Cut trailing delimiter
				folder = folder.substring(0, folder.length() - 1);
			}
			
			this.folderMap.put(entry.getKey(), folder);
		}
		
		// Eventually apply library path hack
		if (applyLibraryPathHack)
		{
			// Add all paths in the folder map to the library path
			StringBuffer newPath = new StringBuffer();
			newPath.append(System.getProperty(JAVA_LIBRARY_PATH_KEY));
			for (final Entry<OsIdentifier, String> entry : this.folderMap.entrySet())
			{
				if (entry.getKey().equals(OsDetector.detectOs()))
				{
					newPath.append(";");
					newPath.append(this.basePath);
					newPath.append(entry.getValue());
				}
			}
			System.setProperty(JAVA_LIBRARY_PATH_KEY, newPath.toString());
			//
			// // Apply actual hack: Asure that the new properties are used by setting the erroneously buffered 'sys_paths'
			// to
			// // 'null'.
			try
			{
				final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				AccessController.doPrivileged((PrivilegedAction<Void>)
						() -> {
							fieldSysPath.setAccessible(true);
							try
							{
								fieldSysPath.set(null, null);
							} catch (Exception err)
							{
								log.fatal("Unable to perform library-path hack!", err);
							}
							fieldSysPath.setAccessible(false);
							return null; // nothing to return
					}
						);
			} catch (final NoSuchFieldException err)
			{
				log.fatal("Unable to perform library-path hack!", err);
			} catch (final SecurityException err)
			{
				log.fatal("Unable to perform library-path hack!", err);
			} catch (final IllegalArgumentException err)
			{
				log.fatal("Unable to perform library-path hack!", err);
			}
			//
			// } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException err)
			// {
			// log.fatal("Unable to perform library-path hack!", err);
			// }
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Checks if the given osId matches {@link OsDetector#detectOs()} and then calls {@link #loadLibrary(String)}.
	 * 
	 * @param libName The case-insensitive name of the library without os-dependent prefixes/suffixes!
	 * @param osId The OS for which this library should be loaded.
	 * @return Whether the given osId matched and the library was (trying to be) loaded or not.
	 * @throws LoaderException
	 */
	public boolean loadLibrary(final String libName, final OsIdentifier osId) throws LoaderException
	{
		if (osId == OsDetector.detectOs())
		{
			loadLibrary(libName);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Uses {@link System#load(String)}
	 * 
	 * @param libName The case-insensitive name of the library without os-dependent prefixes/suffixes!
	 * @throws LoaderException
	 */
	public void loadLibrary(final String libName) throws LoaderException
	{
		final OsIdentifier os = OsDetector.detectOs();
		final String folder = folderMap.get(os);
		
		// Check for present entry
		if (folder == null)
		{
			throw new LoaderException("No folder stored for OsIdentifier " + os + "!");
		}
		
		// Combine base path and subfolder
		final String absolutePath = basePath + folder;
		final File subFolder = new File(absolutePath);
		
		// Check for existence
		if (!subFolder.exists())
		{
			throw new LoaderException("Path '" + absolutePath + "' stored for OsIdentifier '" + os + "' does not exist!");
		}
		
		// Load! =)
		final String fullPath = absolutePath + DELIMITER + libName;
		final File fullFile = new File(fullPath);
		
		if (!fullFile.exists())
		{
			// Filter for libName (while omitting os-dependent extensions)
			final File[] files = subFolder.listFiles(new MyFilenameFilter(libName));
			
			// If libName is unique, exactlly one file should have passed the filter
			if ((files != null) && (files.length == 1))
			{
				System.load(files[0].getAbsolutePath());
			} else
			{
				throw new LoaderException("The given lib-name '" + libName + "' seems to be non-unique in the folder '"
						+ folder + "' for the OsIdentifier '" + os + "'.");
			}
		} else
		{
			// Very unlikely, as libraries normally have extensions which are typical for their architecure/os
			System.load(fullPath);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the basePath
	 */
	public String getBasePath()
	{
		return basePath;
	}
	
	
	/**
	 * @param basePath the basePath to set
	 */
	public void setBasePath(String basePath)
	{
		char c = basePath.charAt(basePath.length() - 1);
		while ((c == '\\') || (c == '/'))
		{
			// Cut trailing delimiter
			basePath = basePath.substring(0, basePath.length() - 1);
			c = basePath.charAt(basePath.length() - 1);
		}
		// this.basePath = new File(basePath).getAbsolutePath();
		
		this.basePath = basePath;
	}
	
	/**
	 */
	public static class LoaderException extends Exception
	{
		private static final long	serialVersionUID	= 3378774656349136360L;
		
		
		/**
		 * @param msg
		 */
		public LoaderException(final String msg)
		{
			super(msg);
		}
	}
	
	private static class MyFilenameFilter implements FilenameFilter
	{
		String	libName;
		
		
		/**
		 * @param libName
		 */
		public MyFilenameFilter(final String libName)
		{
			this.libName = libName;
		}
		
		
		@Override
		public boolean accept(final File file, final String filename)
		{
			String name = filename;
			// Omit os-dependent suffixes
			final String[] nameParts = name.split("\\.(?=[^\\.]+$)");
			if ((nameParts.length < 1) || nameParts[0].isEmpty())
			{
				return false;
			}
			
			name = nameParts[0];
			
			// Omit lib prefix
			if (name.startsWith("lib") && (name.length() > 3))
			{
				name = name.substring(3);
			}
			
			if (name.endsWith("64"))
			{
				name = name.substring(0, name.length() - 2);
			}
			return name.toLowerCase().equals(libName.toLowerCase());
		}
	}
}
