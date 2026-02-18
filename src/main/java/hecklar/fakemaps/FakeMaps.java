package hecklar.fakemaps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeMaps implements ClientModInitializer {
	public static final String MOD_ID = "fake-maps";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean renderingEnabled = true;
	public static boolean vanillaMapsEnabled = false;
	public static boolean itemFramesEnabled = true;

	@Override
	public void onInitializeClient() {
		KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fake-maps.toggle",
				InputUtil.Type.MOUSE,
				GLFW.GLFW_MOUSE_BUTTON_5,
				"category.fake-maps"
		));

		KeyBinding toggleVanillaMaps = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fake-maps.toggle-vanilla-maps",
				InputUtil.Type.MOUSE,
				GLFW.GLFW_MOUSE_BUTTON_4,
				"category.fake-maps"
		));

		KeyBinding toggleItemFrames = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fake-maps.toggle-item-frames",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"category.fake-maps"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				renderingEnabled = !renderingEnabled;
				LOGGER.info("FakeMaps rendering: {}", renderingEnabled ? "ON" : "OFF");
			}
			while (toggleVanillaMaps.wasPressed()) {
				vanillaMapsEnabled = !vanillaMapsEnabled;
				LOGGER.info("Vanilla maps: {}", vanillaMapsEnabled ? "ON" : "OFF");
			}
			while (toggleItemFrames.wasPressed()) {
				itemFramesEnabled = !itemFramesEnabled;
				LOGGER.info("Item frames: {}", itemFramesEnabled ? "ON" : "OFF");
			}
		});
	}
}