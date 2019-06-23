/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Maps one or more identifiers to a flexible action
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RcmActionMapping
{
	private final List<ExtIdentifier>	identifiers;
	private RcmAction							action;
	
	
	/**
	 * @param identifiers
	 * @param action
	 */
	public RcmActionMapping(final List<ExtIdentifier> identifiers, final RcmAction action)
	{
		this.identifiers = identifiers;
		this.action = action;
	}
	
	
	/**
	 * @return the identifiers
	 */
	public final List<ExtIdentifier> getIdentifiers()
	{
		return identifiers;
	}
	
	
	/**
	 * @return the action
	 */
	public final RcmAction getAction()
	{
		return action;
	}
	
	
	/**
	 * @param action
	 */
	public final void setAction(final RcmAction action)
	{
		this.action = action;
	}
	
	
	@Override
	public String toString()
	{
		return "RcmActionMapping [identifiers=" + identifiers + ", action=" + action + "]";
	}
	
	
	/**
	 * @return
	 */
	public JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<>();
		jsonMapping.put("action", action.toJSON());
		List<String> jsonIds = new ArrayList<>(identifiers.size());
		for (ExtIdentifier extId : identifiers)
		{
			jsonIds.add(extId.getExtIdentifier());
		}
		jsonMapping.put("identifiers", jsonIds);
		return new JSONObject(jsonMapping);
	}
	
	
	/**
	 * @param jsonObj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static RcmActionMapping fromJSON(final JSONObject jsonObj)
	{
		Map<String, Object> jsonMapping = jsonObj;
		JSONObject jsonAction = (JSONObject) jsonMapping.get("action");
		RcmAction action = RcmAction.fromJSON(jsonAction);
		List<String> jsonIdentifiers = (List<String>) jsonMapping.get("identifiers");
		List<ExtIdentifier> extIds = new ArrayList<>(jsonIdentifiers.size());
		for (String strId : jsonIdentifiers)
		{
			extIds.add(ExtIdentifier.valueOf(strId));
		}
		return new RcmActionMapping(extIds, action);
	}
}