/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	private static final List<RcmAction> ALL_ACTIONS;
	
	static
	{
		List<RcmAction> actions = new ArrayList<>();
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
	
	private final Enum<? extends Enum<?>> actionEnum;
	private final EActionType actionType;
	
	
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
		List<RcmAction> actions = new ArrayList<>();
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
		actions.add(new RcmAction(ESkill.TOUCH_KICK, EActionType.SKILL));
		actions.add(new RcmAction(ESkill.INTERCEPTION, EActionType.SKILL));
		return actions;
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
	
	
	/**
	 * @return the actionEnum
	 */
	public final Enum getActionEnum()
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
		return actionType == other.actionType;
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
		Map<String, String> jsonAction = new LinkedHashMap<>(2);
		jsonAction.put("actionType", getActionType().name());
		jsonAction.put("actionEnum", getActionEnum().name());
		return new JSONObject(jsonAction);
	}
	
	
	/**
	 * Possible action types
	 */
	public enum EActionType
	{
		/**
		 * EControllerAction
		 */
		SIMPLE(EControllerAction.values()),
		/**
		 * ESkillname
		 */
		SKILL(ESkill.TOUCH_KICK, ESkill.INTERCEPTION),
		/**
		 * ERcmEvent
		 */
		EVENT(ERcmEvent.values());
		
		private final List<Enum<?>> enums;
		
		
		EActionType(final Enum<?>... enums)
		{
			this.enums = Arrays.asList(enums);
		}
		
		
		/**
		 * @return the enums
		 */
		public final List<Enum> getEnums()
		{
			return Collections.unmodifiableList(enums);
		}
	}
}
