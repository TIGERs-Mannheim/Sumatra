package edu.tigers.sumatra.util;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * This class represents the data Structure for Shortcuts inside sumatra.
 * Each Shortcut can have multiple keyStrokes attached, hence the list
 * It is primarily used in the GlobalShortcuts class and can also return descriptions of the keybinds for display purposes
 */
@Value
@Builder
public class UiShortcut
{
	@NonNull
	String name;
	@NonNull
	Set<KeyStroke> keyStrokes;
	@NonNull
	Component component;
	@NonNull
	KeyEventDispatcher dispatcher;



	/**
	 * This method returns the description of the key Combos for this Shortcut.
	 * Multiple Shortcuts are distinguished by a " | "
	 * This is for display purposes only. DO NOT USE THIS TO IDENTIFY THE KEYBIND
	 * Use the keyStroke for that.
	 */
	public String getKeys()
	{
		StringBuilder fullDescription = new StringBuilder();
		// iterate over each

		keyStrokes.forEach(
				keyStroke -> {
					fullDescription.append(getKeyDescription(keyStroke));
					fullDescription.append(" | ");
				});
		// delete the last three characters so the last entry gets printed without separator
		fullDescription.delete(fullDescription.length()-3,fullDescription.length());
		return fullDescription.toString();
	}

	/**
	 * This function generates a description of a provided keyStroke
	 * */
	public String getKeyDescription(KeyStroke keyStroke)
	{

		//if there is no modifier, there is no need for the +, just return the keyCode
		if (keyStroke.getModifiers() == 0)
		{
			return KeyEvent.getKeyText(keyStroke.getKeyCode());
		}
		//else, return modifier + keycode
		return InputEvent.getModifiersExText(keyStroke.getModifiers())
				+ "+"
				+ KeyEvent.getKeyText(keyStroke.getKeyCode());
	}
}
