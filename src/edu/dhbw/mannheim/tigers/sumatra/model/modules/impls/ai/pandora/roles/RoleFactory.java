/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Simple factory-class for converting {@link ERole}-types to {@link ARole} instances
 * 
 * @see #createRole(ERole)
 * @author Gero
 */
public final class RoleFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static List<ERole>		availableRoles	= new ArrayList<ERole>();
	private static final Logger	log				= Logger.getLogger(RoleFactory.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * static class
	 */
	private RoleFactory()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @param eRole
	 * @return
	 */
	public static ARole createRole(ERole eRole)
	{
		ARole aRole = null;
		try
		{
			final Constructor<?> con = eRole.getConstructor();
			aRole = (ARole) con.newInstance();
		} catch (final SecurityException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (final InstantiationException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (final IllegalAccessException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (final IllegalArgumentException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (final InvocationTargetException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (final IllegalStateException err)
		{
			throw new RoleNotCreateable("", err);
		} catch (NoSuchMethodException err)
		{
			throw new RoleNotCreateable("Role " + eRole + " has no default constructor and can not be generated.", err);
		}
		
		return aRole;
	}
	
	private static class RoleNotCreateable extends Error
	{
		/**  */
		private static final long	serialVersionUID	= 89775383135278930L;
		
		
		public RoleNotCreateable(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
	
	/**
	 * Checks all roles and filles availableRoles with valid roles
	 */
	public static void selfCheckRoles()
	{
		availableRoles.clear();
		for (ERole role : ERole.values())
		{
			try
			{
				ARole aRole = createRole(role);
				availableRoles.add(role);
				if (!aRole.doSelfCheck())
				{
					log.warn("StateMachine for role " + aRole + " is not complete!");
				}
			} catch (RoleNotCreateable err)
			{
				log.warn("Role type could not be handled by role factory! Role = " + role, err);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the availableRoles
	 */
	public static synchronized List<ERole> getAvailableRoles()
	{
		return availableRoles;
	}
}
