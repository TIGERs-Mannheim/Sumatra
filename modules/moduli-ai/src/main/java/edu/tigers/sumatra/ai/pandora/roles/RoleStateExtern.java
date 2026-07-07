package edu.tigers.sumatra.ai.pandora.roles;

import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.TransitionableState;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


/**
 * State with a skill
 *
 * @param <T> the skill class
 */
public class RoleStateExtern<T extends ASkill> extends TransitionableState
{
	private final Supplier<T> supplier;
	private final ARole role;
	protected T skill;


	public RoleStateExtern(
			Supplier<T> supplier,
			ARole role
	)
	{
		super(role::changeState);
		this.supplier = supplier;
		this.role = role;
	}


	public final void addTransition(ESkillState skillState, IState nextState)
	{
		addTransition(
				"Skill State == " + skillState,
				() -> skill.getSkillState() == skillState,
				nextState
		);
	}


	public final void addTransition(ESkillState skillState, BooleanSupplier evaluation, IState nextState)
	{
		addTransition(skillState, evaluation.toString(), evaluation, nextState);
	}


	public final void addTransition(ESkillState skillState, String name, BooleanSupplier evaluation, IState nextState)
	{
		addTransition(
				"Skill State == " + skillState + " && " + name,
				() -> skill.getSkillState() == skillState && evaluation.getAsBoolean(),
				nextState
		);
	}


	@Override
	protected final void onInit()
	{
		skill = supplier.get();
		role.setNewSkill(skill);
		onEntry();
	}


	protected void onEntry()
	{
		// can be overridden by subclasses
	}


	@Override
	public String getName()
	{
		if (skill != null)
		{
			return super.getName() + "<" + skill.getClass().getSimpleName() + ">";
		}
		return super.getName();
	}
}
