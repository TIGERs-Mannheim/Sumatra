/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * Manage global shortcuts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalShortcuts
{
	private static final List<UiShortcut> UI_SHORTCUTS = new CopyOnWriteArrayList<>();


	public static void add(
			String name,
			Component component,
			Runnable runnable,
			KeyStroke keyStroke)
	{
		KeyEventDispatcher dispatcher = e -> {
			var eventKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
			if (eventKeyStroke.equals(keyStroke))
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


		String keys = InputEvent.getModifiersExText(keyStroke.getModifiers())
				+ "+"
				+ KeyEvent.getKeyText(keyStroke.getKeyCode());
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		UI_SHORTCUTS.add(UiShortcut.builder()
				.name(name)
				.keys(keys)
				.component(component)
				.dispatcher(dispatcher)
				.build()
		);
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
				.collect(Collectors.toUnmodifiableList());
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
