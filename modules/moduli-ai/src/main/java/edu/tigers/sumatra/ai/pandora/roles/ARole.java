/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Abstract type base type for all roles.
 */
@Log4j2
public abstract class ARole
{
	private final ERole type;
	private final IStateMachine<IState> stateMachine;

	private BotID botID = BotID.noBot();
	private boolean firstUpdate = true;
	private boolean completed = false;
	private AthenaAiFrame aiFrame = null;

	private ISkill currentSkill = new IdleSkill();
	private ISkill newSkillInternal = currentSkill;
	private ISkill newSkillExternal = null;


	public ARole(final ERole type)
	{
		this.type = type;
		stateMachine = new StateMachine<>();
		stateMachine.setExtendedLogging(!SumatraModel.getInstance().isProductive());
	}


	/**
	 * Add a transition.
	 *
	 * @param currentState the state for which the transition should be triggered, can be null for wildcard
	 * @param event        the event that triggers the transition
	 * @param nextState    the resulting state
	 */
	protected final void addTransition(final IState currentState, final IEvent event, final IState nextState)
	{
		stateMachine.addTransition(currentState, event, nextState);
	}


	/**
	 * Add a wildcard transition
	 *
	 * @param event     the event that triggers the transition
	 * @param nextState the resulting state
	 */
	protected final void addTransition(final IEvent event, final IState nextState)
	{
		stateMachine.addTransition(null, event, nextState);
	}


	/**
	 * Set the initial state to start with. This is mandatory for all roles!
	 *
	 * @param initialState first state to set
	 */
	protected final void setInitialState(final IState initialState)
	{
		stateMachine.setInitialState(initialState);
	}


	/**
	 * Go to next state
	 *
	 * @param event to be triggered
	 */
	public final void triggerEvent(final IEvent event)
	{
		stateMachine.triggerEvent(event);
	}


	/**
	 * Go to specified state
	 *
	 * @param state the new state to switch to
	 */
	protected final void changeState(final IState state)
	{
		stateMachine.changeState(state);
	}


	/**
	 * Identifier of the current state
	 *
	 * @return the current state - can be null!
	 */
	public final IState getCurrentState()
	{
		return stateMachine.getCurrentState();
	}


	/**
	 * This method should only be called from the RoleAssigner!!!
	 *
	 * @param newBotId the new id that should be assigned to this role
	 */
	public final void assignBotID(final BotID newBotId)
	{
		if (newBotId.isUninitializedID())
		{
			throw new IllegalArgumentException("Someone tries to initialize role '" + this
					+ "' with an UNINITIALIZED BotID!!!");
		}
		botID = newBotId;
		stateMachine.setName("Role " + getType() + " " + botID);
	}


	/**
	 * @param curFrame current frame
	 */
	public final void updateBefore(final AthenaAiFrame curFrame)
	{
		aiFrame = curFrame;
	}


	/**
	 * @param curFrame current frame
	 */
	public final void update(final AthenaAiFrame curFrame)
	{
		updateBefore(curFrame);

		try
		{
			if (!curFrame.getWorldFrame().getTigerBotsAvailable().containsKey(getBotID()))
			{
				setCompleted();
			}

			if (firstUpdate)
			{
				if (hasBeenAssigned())
				{
					beforeFirstUpdate();
					firstUpdate = false;
				} else
				{
					log.error(this + ": Update was called although role has not been assigned!");
					return;
				}
			}

			if (isCompleted())
			{
				return;
			}

			beforeUpdate();

			if (stateMachine.getCurrentState() == null)
			{
				setCompleted();
			} else
			{
				stateMachine.update();
			}

			afterUpdate();
			if (newSkillInternal != null)
			{
				// make sure that new skills are only published to Skill Executor after the whole
				// state update loop is done.
				newSkillExternal = newSkillInternal;
				newSkillInternal = null;
			}
		} catch (Exception err)
		{
			log.error("Exception on role update (" + getBotID() + ", " + getType() + ")", err);
		}
	}


	/**
	 * This method is called after the state machine update.
	 * Use this for role-global actions. <br>
	 * This method will also be called after the role completed but was not removed yet
	 */
	protected void afterUpdate()
	{
	}


	/**
	 * This method is called before the state machine update.
	 * Use this for role-global actions. <br>
	 */
	protected void beforeUpdate()
	{
	}


	/**
	 * This method is called before the state machine update and only once after role instantiation
	 * Use this for role-global actions. <br>
	 */
	protected void beforeFirstUpdate()
	{
	}


	protected void onCompleted()
	{
	}


	/**
	 * @return type of role
	 */
	public final ERole getType()
	{
		return type;
	}


	/**
	 * @return bot id
	 */
	public final BotID getBotID()
	{
		return botID;
	}


	/**
	 * Returns the current position of the bot associated with this role.<br>
	 * <strong>WARNING: Use only after role has been assigned!!!</strong> (Makes no sense otherwise...)
	 *
	 * @return position
	 */
	public final IVector2 getPos()
	{
		return getWFrame().getTiger(botID).getPos();
	}


	/**
	 * Get the tracked bot of this role
	 *
	 * @return tracked bot
	 */
	public final ITrackedBot getBot()
	{
		return getWFrame().getTiger(getBotID());
	}


	/**
	 * @return Whether this role has been assigned to a real bot
	 */
	private boolean hasBeenAssigned()
	{
		return !botID.isUninitializedID();
	}


	/**
	 * @return true when this role is completed
	 */
	public final boolean isCompleted()
	{
		return completed;
	}


	/**
	 * Sets this role to completed.
	 */
	public final void setCompleted()
	{
		if (!completed)
		{
			log.trace(this + ": Completed");
			completed = true;
			stateMachine.stop();
			if (currentSkill.getClass() != IdleSkill.class)
			{
				setNewSkill(new IdleSkill());
			}
			onCompleted();
		}
	}


	@Override
	public final String toString()
	{
		return type.toString();
	}


	/**
	 * @return the current ai frame
	 */
	public final AthenaAiFrame getAiFrame()
	{
		return aiFrame;
	}


	/**
	 * @return the tactical field from the aiFrame
	 */
	public final TacticalField getTacticalField()
	{
		return aiFrame.getTacticalField();
	}


	/**
	 * @return the current worldframe
	 */
	public final WorldFrame getWFrame()
	{
		return aiFrame.getWorldFrame();
	}


	/**
	 * @return the current ball
	 */
	public final ITrackedBall getBall()
	{
		return getWFrame().getBall();
	}


	/**
	 * @param identifier shape layer identifier
	 * @return the respective list from the tactical field
	 */
	public final List<IDrawableShape> getShapes(final IShapeLayer identifier)
	{
		return getAiFrame().getShapeMap().get(identifier);
	}


	/**
	 * @return the currentSkill
	 */
	public final ISkill getCurrentSkill()
	{
		return currentSkill;
	}


	/**
	 * @param newSkill the currentSkill to set
	 */
	public final void setNewSkill(final ISkill newSkill)
	{
		currentSkill = newSkill;
		newSkillInternal = newSkill;
	}


	/**
	 * DO NOT call this from AI. This is used by SkillExecutor!
	 *
	 * @return the newSkill
	 */
	public final ISkill getNewSkill()
	{
		ISkill skill = newSkillExternal;
		newSkillExternal = null;
		return skill;
	}


	/**
	 * State with a skill
	 *
	 * @param <T> the skill class
	 */
	@RequiredArgsConstructor
	protected class RoleState<T extends ASkill> extends AState
	{
		private final List<Transition> transitions = new ArrayList<>();
		private final Supplier<T> supplier;
		protected T skill;


		public final void addTransition(ESkillState skillState, IState nextState)
		{
			transitions.add(new Transition(() -> skill.getSkillState() == skillState, nextState));
		}


		public final void addTransition(Supplier<Boolean> evaluation, IState nextState)
		{
			transitions.add(new Transition(evaluation, nextState));
		}


		protected void onInit()
		{
			// can be overwritten
		}


		protected void onExit()
		{
			// can be overwritten
		}


		protected void onUpdate()
		{
			// can be overwritten
		}


		@Override
		public final void doEntryActions()
		{
			skill = supplier.get();
			setNewSkill(skill);
			onInit();
		}


		@Override
		public final void doExitActions()
		{
			onExit();
		}


		@Override
		public final void doUpdate()
		{
			for (var transition : transitions)
			{
				if (transition.evaluation.get())
				{
					changeState(transition.nextState);
					return;
				}
			}
			onUpdate();
		}


		@Override
		public String getIdentifier()
		{
			if (skill != null)
			{
				return super.getIdentifier() + "<" + skill.getClass().getSimpleName() + ">";
			}
			return super.getIdentifier();
		}
	}

	@Value
	private static class Transition
	{
		Supplier<Boolean> evaluation;
		IState nextState;
	}


	public Map<IEvent, Map<IState, IState>> getStateGraph()
	{
		return stateMachine.getTransitions();
	}
}
