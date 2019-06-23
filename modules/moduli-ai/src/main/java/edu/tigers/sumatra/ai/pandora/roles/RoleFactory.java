/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;
import com.github.g3force.instanceables.InstanceableParameter;


/**
 * Simple factory-class for converting {@link ERole}-types to {@link ARole} instances
 * 
 * @see #createRole(ERole, Object...)
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
	 * @param eRole
	 * @param args
	 * @return
	 * @throws NotCreateableException
	 */
	public static ARole createRole(final ERole eRole, final Object... args) throws NotCreateableException
	{
		return (ARole) eRole.getInstanceableClass().newInstance(args);
	}
	
	
	/**
	 * Create Role with default Parameter values
	 * 
	 * @param eRole
	 * @return
	 * @throws NotCreateableException
	 */
	public static ARole createDefaultRole(final ERole eRole) throws NotCreateableException
	{
		List<Object> objParams = new ArrayList<Object>(eRole.getInstanceableClass().getParams().size());
		for (InstanceableParameter param : eRole.getInstanceableClass().getParams())
		{
			Object objParam = param.parseString(param.getDefaultValue());
			objParams.add(objParam);
		}
		return createRole(eRole, objParams.toArray());
	}
	
	
	/**
	 * Checks all roles and filles availableRoles with valid roles
	 */
	public static void selfCheckRoles()
	{
		log.trace("Start selfChecking roles");
		availableRoles.clear();
		for (ERole role : ERole.values())
		{
			try
			{
				ARole aRole = createDefaultRole(role);
				availableRoles.add(role);
				if (!aRole.doSelfCheck())
				{
					log.warn("StateMachine for role " + aRole + " is not complete!");
				}
			} catch (NotCreateableException err)
			{
				log.warn("Role type could not be handled by role factory! Role = " + role, err);
			}
		}
		log.trace("Role selfcheck done");
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
