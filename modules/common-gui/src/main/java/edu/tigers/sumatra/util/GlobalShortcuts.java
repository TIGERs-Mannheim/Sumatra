/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Manage global shortcuts
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class GlobalShortcuts
{
	private static final Map<EShortcut, ShortcutWrapper> SHORTCUTS = new ConcurrentHashMap<>();
	
	private static class ShortcutWrapper
	{
		private KeyEventDispatcher	dispatcher;
	}

    /**
     * Enum which contains the available Key shortcuts
     */
	public enum EKeyModifier {
		CTRL,
		ALT,
		SHIFT;

        /**
         * Method to evaluate if the correct keys were pressed.
         *
         * @param e
         * @return
         */
		public boolean isKeyPressed(KeyEvent e) {
			switch (this) {
				case CTRL:
					return e.isControlDown();
				case ALT:
					return e.isAltDown();
				case SHIFT:
					return e.isShiftDown();
				default:
						return false;
			}
		}
	}
	
	
	/**
	 * Enum with all available global shortcuts
	 */
	public enum EShortcut
	{
		/**  */
		EMERGENCY_MODE(KeyEvent.VK_ESCAPE, "esc"),
		/**  */
		START_STOP(KeyEvent.VK_F11, "F11"),
		
		/**  */
		MATCH_MODE(KeyEvent.VK_F1, "F1"),
		
		/**  */
		MATCH_LAYOUT(KeyEvent.VK_F8, "F8"),
		/**  */
		TIMEOUT_LAYOUT(KeyEvent.VK_F9, "F9"),
		/**  */
		DEFAULT_LAYOUT(KeyEvent.VK_F10, "F10"),
		
		/**  */
		CHARGE_ALL_BOTS(KeyEvent.VK_F5, "F5"),
		/**  */
		DISCHARGE_ALL_BOTS(KeyEvent.VK_F6, "F6"),
		
		/**  */
		RESET_FIELD(KeyEvent.VK_F7, "F7"),
		
		/**  */
		REFEREE_HALT(KeyEvent.VK_F4, "F4"),
		/**  */
		REFEREE_STOP(KeyEvent.VK_F3, "F3"),
		/**  */
		REFEREE_START(KeyEvent.VK_F2, "F2"),

		/** */
		REFBOX_HALT(KeyEvent.VK_SEPARATOR, "SEPARATOR", EKeyModifier.CTRL),
		/** */
		REFBOX_STOP(KeyEvent.VK_NUMPAD0, "NUMPAD0", EKeyModifier.CTRL),
		/** */
		REFBOX_START_NORMAL(KeyEvent.VK_ENTER, "ENTER", EKeyModifier.CTRL),
		/** */
		REFBOX_START_FORCE(KeyEvent.VK_NUMPAD5, "NUMPAD5", EKeyModifier.CTRL),
		/** */
		REFBOX_KICKOFF_YELLOW(KeyEvent.VK_NUMPAD1, "NUMPAD1", EKeyModifier.CTRL),
		/** */
		REFBOX_KICKOFF_BLUE(KeyEvent.VK_NUMPAD3, "NUMPAD3", EKeyModifier.CTRL),
		/** */
		REFBOX_INDIRECT_YELLOW(KeyEvent.VK_NUMPAD4, "NUMPAD4", EKeyModifier.CTRL),
		/** */
		REFBOX_INDIRECT_BLUE(KeyEvent.VK_NUMPAD6, "NUMPAD6", EKeyModifier.CTRL),
		/** */
		REFBOX_DIRECT_YELLOW(KeyEvent.VK_NUMPAD7, "NUMPAD7", EKeyModifier.CTRL),
		/** */
		REFBOX_DIRECT_BLUE(KeyEvent.VK_NUMPAD9, "NUMPAD9", EKeyModifier.CTRL),
		
		AUTOREF_TOGGLE(KeyEvent.VK_F12, "F12"),
		
		;
		
		private final int		key;
		private final String	desc;
		private final Set<EKeyModifier> modifiers;
		
		
		EShortcut(final int key, final String desc, final EKeyModifier... modifiers)
		{
			this.key = key;
			this.desc = desc;

			this.modifiers = new HashSet<>(Arrays.asList(modifiers));
		}
		
		
		/**
		 * @return the key
		 */
		public final int getKey()
		{
			return key;
		}
		
		
		/**
		 * @return the desc
		 */
		public final String getDesc()
		{
			return desc;
		}

		/**
		 * @return the modifiers
		 */
		public final Set<EKeyModifier> getModifiers() {return modifiers;}
	}
	
	
	private GlobalShortcuts()
	{
	}
	
	
	/**
	 * @param shortcut
	 * @param run
	 */
	public static void register(final EShortcut shortcut, final Runnable run)
	{
		KeyEventDispatcher ked = e -> {
			if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == shortcut.key)
			{
				final boolean optionalKeyPressed = isOptionalKeyPressedForShortcut(e, shortcut);

				if(optionalKeyPressed) {
					run.run();
				}
			}
			return false;
		};
		ShortcutWrapper w = new ShortcutWrapper();
		w.dispatcher = ked;
		SHORTCUTS.put(shortcut, w);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked);
	}

	private static boolean isOptionalKeyPressedForShortcut(KeyEvent e, EShortcut shortcut) {
		boolean keyIsPressed = true;

		for (EKeyModifier modifier : shortcut.getModifiers()) {
			keyIsPressed &= modifier.isKeyPressed(e);
		}

		return keyIsPressed;
	}
	
	
	/**
	 * @param shortcut
	 */
	public static void unregisterAll(final EShortcut shortcut)
	{
		ShortcutWrapper shortcutWrapper = SHORTCUTS.remove(shortcut);
		if (shortcutWrapper != null)
		{
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(shortcutWrapper.dispatcher);
		}
	}
}
