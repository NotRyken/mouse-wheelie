/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client;

import com.mojang.logging.LogUtils;
import de.siphalor.mousewheelie.client.mixin.KeyBindingAccessor;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static final KeyBinding SORT_KEY_BINDING = new KeyBinding(
			"sort_inventory", InputUtil.Type.MOUSE, 2, "mw_category");

	public static int lastUpdatedSlot = -1;

	public static int cooldown = 0;

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(SORT_KEY_BINDING);
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			CreativeSearchOrder.refreshItemSearchPositionLookup();
		});
	}

	public void onEndTick(MinecraftClient mc) {
		if (cooldown == 0) {
			if (InputUtil.isKeyPressed(mc.getWindow().getHandle(),
					((KeyBindingAccessor)SORT_KEY_BINDING).getBoundKey().getCode())) {
				if (mc.currentScreen instanceof IContainerScreen screen) {
					LogUtils.getLogger().info("good screen");
					screen.mouseWheelie_triggerSort();
					cooldown = 11;
				}
			}
		}
		if (cooldown > 0) cooldown--;
	}

	public static boolean isOnLocalServer() {
		return CLIENT.getServer() != null;
	}
}
