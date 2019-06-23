/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.02.2012
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.natives;

/**
 * This class provides the method {@link #detectOs()} to detect the operating system this program is running at.
 * 
 * @author Gero
 */
public final class OsDetector
{
	// --------------------------------------------------------------------------
	// --- classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public enum EOsName
	{
		/** */
		WINDOWS,
		/** */
		MAC,
		/** */
		UNIX,
		/** */
		SOLARIS,
		/** */
		UNKNOWN;
	}
	
	/**
	 */
	public enum EOsArch
	{
		/** */
		x86,
		/** */
		x64,
		/** */
		SPARC,
		/** */
		PPC,
		/** */
		UNKNOWN;
	}
	
	
	private OsDetector()
	{
		
	}
	
	/**
	 */
	public static class OsIdentifier
	{
		private final EOsName	name;
		private final EOsArch	arch;
		
		
		/**
		 * @param name
		 * @param arch
		 */
		public OsIdentifier(final EOsName name, final EOsArch arch)
		{
			super();
			this.name = name;
			this.arch = arch;
		}
		
		
		/**
		 * @return
		 */
		public EOsName getName()
		{
			return name;
		}
		
		
		/**
		 * @return
		 */
		public EOsArch getArch()
		{
			return arch;
		}
		
		
		@Override
		public String toString()
		{
			return name.name() + "/" + arch.name();
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((arch == null) ? 0 : arch.hashCode());
			result = (prime * result) + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final OsIdentifier other = (OsIdentifier) obj;
			if (arch != other.arch)
			{
				return false;
			}
			if (name != other.name)
			{
				return false;
			}
			return true;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static OsIdentifier detectOs()
	{
		return new OsIdentifier(getOsName(), getOsArch());
	}
	
	
	/**
	 * @return
	 */
	public static EOsName getOsName()
	{
		final EOsName detName;
		
		if (isWindows())
		{
			detName = EOsName.WINDOWS;
		} else if (isMac())
		{
			detName = EOsName.MAC;
		} else if (isUnix())
		{
			detName = EOsName.UNIX;
		} else if (isSolaris())
		{
			detName = EOsName.SOLARIS;
		} else
		{
			detName = EOsName.UNKNOWN;
		}
		
		return detName;
	}
	
	
	/**
	 * @return
	 */
	public static EOsArch getOsArch()
	{
		final String arch = System.getProperty("os.arch").toLowerCase();
		
		final EOsArch detArch;
		
		if (arch.indexOf("64") >= 0)
		{
			detArch = EOsArch.x64;
		} else if (arch.indexOf("86") >= 0)
		{
			detArch = EOsArch.x86;
		} else if (arch.indexOf("ppc") >= 0)
		{
			detArch = EOsArch.PPC;
		} else if (arch.indexOf("sparc") >= 0)
		{
			detArch = EOsArch.SPARC;
		} else
		{
			detArch = EOsArch.UNKNOWN;
		}
		
		return detArch;
	}
	
	
	/**
	 * @return
	 */
	public static boolean isWindows()
	{
		final String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}
	
	
	/**
	 * @return
	 */
	public static boolean isMac()
	{
		final String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("mac") >= 0);
	}
	
	
	/**
	 * @return
	 */
	public static boolean isUnix()
	{
		final String os = System.getProperty("os.name").toLowerCase();
		return ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0));
	}
	
	
	/**
	 * @return
	 */
	public static boolean isSolaris()
	{
		final String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("sunos") >= 0);
	}
}
