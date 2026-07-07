package edu.tigers.sumatra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Manage global shortcuts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalShortcuts
{
	private static final List<UiShortcut> UI_SHORTCUTS = new CopyOnWriteArrayList<>();

	/**
	 * A Set of KeyStrokes that are currently disabled
	 */
	private static final Set<KeyStroke> disabledKeystrokes = new HashSet<>();


	/**
	 * This adds a Global Shortcut which applies to the current Sumatra window
	 * @param name the description of what the Shortcut does
	 * @param component the component that the Shortcut is for
	 * @param runnable the method the Shortcut is supposed to execute
	 * @param keyStroke the keystroke that activates the Shortcut
	 */
	public static void add(
			String name,
			Component component,
			Runnable runnable,
			KeyStroke keyStroke
	)
	{
		addMultiple(name,component,runnable,Set.of(keyStroke));
	}

	/**
	 * This adds a Global Shortcut which applies to the current Sumatra window
	 * the difference to add is, that it allows for multiple keyStrokes that activate the shortcut
	 * @param name the description of what the Shortcut does
	 * @param component the component that the Shortcut is for
	 * @param runnable the method the Shortcut is supposed to execute
	 * @param keyStrokes a set of keystrokes that each activate the shortcut
	 */
	public static void addMultiple(
			String name,
			Component component,
			Runnable runnable,
			Set<KeyStroke> keyStrokes
	)
	{
		KeyEventDispatcher dispatcher = e -> {
			var eventKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
			// if the currently pressed keyStroke is supposed to be disabled, do nothing and return false
			if(disabledKeystrokes.contains(eventKeyStroke))
			{
				return false;
			}

			if (keyStrokes.contains(eventKeyStroke))
			{
				var rootComponent = findRootComponent(component);
				Component eventComponent = e.getComponent();
				if (eventComponent != null && findRootComponent(eventComponent) == rootComponent)
				{
					runnable.run();
					return true;
				}
			}
			return false;
		};


		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		UI_SHORTCUTS.add(UiShortcut.builder()
				.name(name)
				.keyStrokes(keyStrokes)
				.component(component)
				.dispatcher(dispatcher)
				.build()
		);
	}


	/**
	 * this method allows to disable shortcuts with specific keystrokes temporarily
	 * it is meant as a workaraound for keystrokes that conflict in certain contexts
	 * example: disable all space bar shortcuts that are space bar in text fields
	 * they need to be re-enabled afterward with enableAllKeyStrokes()
	 */
	public static void disableByKeyStroke(KeyStroke keyStroke)
	{
		disabledKeystrokes.add(keyStroke);
	}


	/**
	 * This function enables all currently disabled shortcuts
	 * You need to call this after the shortcuts no longer need to be disabled
	 */
	public static void enableAllShortcuts()
	{
		disabledKeystrokes.clear();
	}


	public static void removeAllForFrame(JFrame frame)
	{
		UI_SHORTCUTS.stream().filter(s -> findRootComponent(s.getComponent()) == frame).forEach(s -> {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(s.getDispatcher());
			UI_SHORTCUTS.remove(s);
		});
	}


	public static void removeAllForComponent(Component component)
	{
		UI_SHORTCUTS.stream().filter(s -> s.getComponent() == component).forEach(s -> {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(s.getDispatcher());
			UI_SHORTCUTS.remove(s);
		});
	}


	public static List<UiShortcut> getShortcuts(Component component)
	{
		var rootComponent = findRootComponent(component);
		return UI_SHORTCUTS.stream()
				.filter(s -> findRootComponent(s.getComponent()) == rootComponent)
				.toList();
	}


	private static Component findRootComponent(Component component)
	{
		Component parent = component.getParent();
		if (parent == null)
		{
			return component;
		}
		return findRootComponent(parent);
	}
}
