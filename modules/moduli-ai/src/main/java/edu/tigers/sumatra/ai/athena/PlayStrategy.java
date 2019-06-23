/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotIDMap;


/**
 * This stores all tactical information.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class PlayStrategy implements IPlayStrategy
{
	private final List<APlay> activePlays;
	private final Map<EPlay, RoleMapping> roleMapping;
	
	
	/**
	 * @param builder
	 */
	public PlayStrategy(final Builder builder)
	{
		activePlays = Collections.unmodifiableList(builder.activePlays);
		roleMapping = Collections.unmodifiableMap(builder.roleMapping);
	}
	
	
	@Override
	public BotIDMap<ARole> getActiveRoles()
	{
		BotIDMap<ARole> roles = new BotIDMap<>();
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				roles.put(role.getBotID(), role);
			}
		}
		return roles;
	}
	
	
	@Override
	public List<ARole> getActiveRoles(final ERole... roleTypes)
	{
		final List<ERole> types = Arrays.asList(roleTypes);
		List<ARole> roles = new ArrayList<>();
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				if (types.contains(role.getType()))
				{
					roles.add(role);
				}
			}
		}
		return roles;
	}
	
	
	@Override
	public List<ARole> getActiveRoles(final EPlay playType)
	{
		List<ARole> roles = new ArrayList<>();
		for (APlay play : activePlays)
		{
			if (play.getType() != playType)
			{
				continue;
			}
			roles.addAll(play.getRoles());
		}
		return roles;
	}
	
	
	@Override
	public List<APlay> getActivePlays()
	{
		return activePlays;
	}
	
	
	@Override
	public Map<EPlay, RoleMapping> getRoleMapping()
	{
		return roleMapping;
	}
	
	/**
	 * Use this builder to create a {@link PlayStrategy}
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class Builder
	{
		private List<APlay> activePlays;
		private Map<EPlay, RoleMapping> roleMapping;
		
		
		/**
		 * Default
		 */
		public Builder()
		{
			activePlays = new ArrayList<>();
			roleMapping = new EnumMap<>(EPlay.class);
		}
		
		
		/**
		 * Create a new {@link PlayStrategy} with the data in this Builder
		 * 
		 * @return
		 */
		public IPlayStrategy build()
		{
			return new PlayStrategy(this);
		}
		
		
		/**
		 * @return the activePlays
		 */
		public final List<APlay> getActivePlays()
		{
			return activePlays;
		}
		
		
		/**
		 * @return the role mapping
		 */
		public Map<EPlay, RoleMapping> getRoleMapping()
		{
			return roleMapping;
		}
	}
}
