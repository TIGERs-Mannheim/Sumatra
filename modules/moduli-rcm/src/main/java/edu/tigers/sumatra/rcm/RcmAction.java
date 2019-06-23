/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import edu.tigers.sumatra.skillsystem.ESkill;


/**
 * Flexible RCM action
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RcmAction
{
	private static final List<RcmAction>	ALL_ACTIONS;
	private final Enum<? extends Enum<?>>	actionEnum;
	private final EActionType					actionType;
														
	/**
	 */
	public enum EActionType
	{
		/** EControllerAction */
		SIMPLE(EControllerAction.class, EControllerAction.values()),
		/** ESkillname */
		SKILL(ESkill.class, ESkill.KICK, ESkill.INTERCEPTION, ESkill.REDIRECT, ESkill.KICK_TEST),
		/** ERcmEvent */
		EVENT(ERcmEvent.class, ERcmEvent.values());
		
		private final List<Enum<?>>	enums;
		private final Class<?>			enumClass;
												
												
		private EActionType(final Class<?> enumClass, final Enum<?>... enums)
		{
			this.enums = Arrays.asList(enums);
			this.enumClass = enumClass;
		}
		
		
		/**
		 * @return the enums
		 */
		public final List<Enum<?>> getEnums()
		{
			return Collections.unmodifiableList(enums);
		}
		
		
		/**
		 * @return the enumClass
		 */
		public final Class<?> getEnumClass()
		{
			return enumClass;
		}
	}
	
	
	static
	{
		List<RcmAction> actions = new ArrayList<RcmAction>();
		for (EActionType actionType : EActionType.values())
		{
			for (Enum<?> e : actionType.getEnums())
			{
				RcmAction action = new RcmAction(e, actionType);
				actions.add(action);
			}
		}
		ALL_ACTIONS = Collections.unmodifiableList(actions);
	}
	
	
	/**
	 * @param actionEnum
	 * @param actionType
	 */
	public RcmAction(final Enum<? extends Enum<?>> actionEnum, final EActionType actionType)
	{
		this.actionEnum = actionEnum;
		this.actionType = actionType;
	}
	
	
	/**
	 * @return the actionEnum
	 */
	public final Enum<? extends Enum<?>> getActionEnum()
	{
		return actionEnum;
	}
	
	
	/**
	 * @return the actionType
	 */
	public final EActionType getActionType()
	{
		return actionType;
	}
	
	
	/**
	 * @return
	 */
	public static List<RcmAction> getAllActions()
	{
		return ALL_ACTIONS;
	}
	
	
	/**
	 * @return
	 */
	public static List<RcmAction> getDefaultActions()
	{
		List<RcmAction> actions = new ArrayList<RcmAction>();
		actions.add(new RcmAction(EControllerAction.FORWARD, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.BACKWARD, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.LEFT, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.RIGHT, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.ROTATE_LEFT, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.ROTATE_RIGHT, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.KICK_ARM, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.KICK_FORCE, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.CHIP_ARM, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.DISARM, EActionType.SIMPLE));
		actions.add(new RcmAction(EControllerAction.DRIBBLE, EActionType.SIMPLE));
		actions.add(new RcmAction(ERcmEvent.NEXT_BOT, EActionType.EVENT));
		actions.add(new RcmAction(ERcmEvent.PREV_BOT, EActionType.EVENT));
		actions.add(new RcmAction(ERcmEvent.UNASSIGN_BOT, EActionType.EVENT));
		actions.add(new RcmAction(ERcmEvent.SPEED_MODE_TOGGLE, EActionType.EVENT));
		actions.add(new RcmAction(ESkill.KICK, EActionType.SKILL));
		actions.add(new RcmAction(ESkill.INTERCEPTION, EActionType.SKILL));
		return actions;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((actionEnum == null) ? 0 : actionEnum.hashCode());
		result = (prime * result) + ((actionType == null) ? 0 : actionType.hashCode());
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
		RcmAction other = (RcmAction) obj;
		if (actionEnum == null)
		{
			if (other.actionEnum != null)
			{
				return false;
			}
		} else if (!actionEnum.equals(other.actionEnum))
		{
			return false;
		}
		if (actionType != other.actionType)
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return actionEnum.name();
	}
	
	
	/**
	 * @return
	 */
	public JSONObject toJSON()
	{
		Map<String, String> jsonAction = new LinkedHashMap<String, String>(2);
		jsonAction.put("actionType", getActionType().name());
		jsonAction.put("actionEnum", getActionEnum().name());
		return new JSONObject(jsonAction);
	}
	
	
	/**
	 * @param jsonAction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static RcmAction fromJSON(final JSONObject jsonAction)
	{
		Map<String, String> map = jsonAction;
		EActionType actionType = EActionType.valueOf(map.get("actionType"));
		String strActionEnum = map.get("actionEnum");
		final Enum<?> actionEnum;
		switch (actionType)
		{
			case EVENT:
				actionEnum = ERcmEvent.valueOf(strActionEnum);
				break;
			case SIMPLE:
				actionEnum = EControllerAction.valueOf(strActionEnum);
				break;
			case SKILL:
				actionEnum = ESkill.valueOf(strActionEnum);
				break;
			default:
				throw new IllegalStateException();
		}
		return new RcmAction(actionEnum, actionType);
	}
}
