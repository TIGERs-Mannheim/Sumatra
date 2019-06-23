package edu.tigers.sumatra.statemachine;

/**
 * @author nicolai.ommer
 */
@FunctionalInterface
public interface IStateIdentifier
{
	/**
	 * @return identifier name
	 */
	String name();
}
