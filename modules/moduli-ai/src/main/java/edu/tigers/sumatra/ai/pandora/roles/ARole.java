/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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
	@Getter
	private final IStateMachine<IState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());

	private BotID botID = BotID.noBot();
	private boolean firstUpdate = true;
	private boolean completed = false;
	private AthenaAiFrame aiFrame = null;

	private ISkill currentSkill = new IdleSkill();
	private ISkill newSkillInternal = currentSkill;
	private ISkill newSkillExternal = null;


	protected ARole(final ERole type)
	{
		this.type = type;
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
	public final void addTransition(final IEvent event, final IState nextState)
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
		stateMachine.setName("Role " + this + " " + botID);
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
					log.error("{}: Update was called although role has not been assigned!", this);
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
			log.error("Exception on role update ({}, {})", getBotID(), this, err);
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
	public final List<IDrawableShape> getShapes(final IShapeLayerIdentifier identifier)
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
	 * A role state with a simple {@link MoveToSkill}.
	 */
	protected class MoveState extends RoleState<MoveToSkill>
	{
		public MoveState()
		{
			super(MoveToSkill::new);
		}


		/**
		 * Adjusts destination when near friendly bot.
		 *
		 * @param dest the desired destination
		 * @return a valid destination
		 */
		private IVector2 adjustPositionWhenNearBot(IVector2 dest)
		{
			double speedTolerance = 0.3;
			IVector2 tmpDest = dest;
			for (ITrackedBot bot : getWFrame().getBots().values())
			{
				if (!bot.getBotId().getTeamColor().equals(getBotID().getTeamColor())
						|| bot.getBotId().equals(getBotID())
						|| bot.getVel().getLength2() > speedTolerance)
				{
					// only consider our own bots
					// and ignore myself
					// and ignore moving bots
					continue;
				}
				double tolerance = (Geometry.getBotRadius() * 2) - 20;
				if (bot.getPos().isCloseTo(dest, tolerance))
				{
					// position is inside other bot, move outside
					tmpDest = LineMath.stepAlongLine(bot.getPos(), dest, tolerance + 20);
				}
			}
			return tmpDest;
		}


		/**
		 * This method adjusts a MoveDestination when its invalid:
		 * - Position is too close to ball.
		 * - Position is in PenArea.
		 * - Position is Near friendly Bot.
		 *
		 * @param dest
		 * @return
		 */
		protected IVector2 adjustMovePositionWhenItsInvalid(IVector2 dest)
		{
			IVector2 dest1 = adjustPositionWhenNearBot(dest);
			return SkillUtil.movePosOutOfPenAreaWrtBall(dest1, getBall(),
					Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()),
					Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius()));
		}
	}


	protected class RoleState<T extends ASkill> extends RoleStateExtern<T>
	{
		public RoleState(Supplier<T> supplier)
		{
			super(supplier, ARole.this);
		}
	}


	public Map<IEvent, Map<IState, IState>> getStateGraph()
	{
		return stateMachine.getTransitions();
	}
}
