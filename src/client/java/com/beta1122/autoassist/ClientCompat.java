package com.beta1122.autoassist;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

/**
 * Small compatibility layer for the screen/keyboard API changes between the
 * Minecraft 26.1 and 26.2/26.3 client classes.
 */
public final class ClientCompat {
	private static final Field MINECRAFT_SCREEN_FIELD = findField(Minecraft.class, "screen");
	private static final Method MINECRAFT_SET_SCREEN = findMethod(Minecraft.class, "setScreen", Screen.class);
	private static final Method GUI_SCREEN = findMethod(Gui.class, "screen");
	private static final Method GUI_SET_SCREEN = findMethod(Gui.class, "setScreen", Screen.class);
	private static final InputConstants.Type KEYBOARD_TYPE = findKeyboardType();

	private ClientCompat() {
	}

	public static InputConstants.Type keyboardType() {
		return KEYBOARD_TYPE;
	}

	public static Screen screen(Minecraft client) {
		if (MINECRAFT_SCREEN_FIELD != null) {
			try {
				return (Screen) MINECRAFT_SCREEN_FIELD.get(client);
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Could not read Minecraft.screen", exception);
			}
		}

		if (GUI_SCREEN != null) {
			try {
				return (Screen) GUI_SCREEN.invoke(client.gui);
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not call Gui.screen()", exception);
			}
		}

		return null;
	}

	public static void setScreen(Minecraft client, Screen screen) {
		if (MINECRAFT_SET_SCREEN != null) {
			try {
				MINECRAFT_SET_SCREEN.invoke(client, screen);
				return;
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not call Minecraft.setScreen", exception);
			}
		}

		if (GUI_SET_SCREEN != null) {
			try {
				GUI_SET_SCREEN.invoke(client.gui, screen);
				return;
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not call Gui.setScreen", exception);
			}
		}

		throw new IllegalStateException("No supported screen API found");
	}

	private static Field findField(Class<?> owner, String name) {
		try {
			Field field = owner.getField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException exception) {
			return null;
		}
	}

	private static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
		try {
			Method method = owner.getMethod(name, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (ReflectiveOperationException exception) {
			return null;
		}
	}

	private static InputConstants.Type findKeyboardType() {
		for (String name : new String[]{"KEYBOARD", "KEYSYM"}) {
			try {
				return (InputConstants.Type) InputConstants.Type.class.getField(name).get(null);
			} catch (ReflectiveOperationException ignored) {
				// Try the next name.
			}
		}

		throw new IllegalStateException("No keyboard InputConstants.Type found");
	}
}
