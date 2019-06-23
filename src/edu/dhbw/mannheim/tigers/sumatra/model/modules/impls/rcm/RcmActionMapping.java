/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;


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
		Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
		jsonMapping.put("action", action.toJSON());
		List<String> jsonIds = new ArrayList<String>(identifiers.size());
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
		List<ExtIdentifier> extIds = new ArrayList<ExtIdentifier>(jsonIdentifiers.size());
		for (String strId : jsonIdentifiers)
		{
			extIds.add(ExtIdentifier.valueOf(strId));
		}
		return new RcmActionMapping(extIds, action);
	}
}