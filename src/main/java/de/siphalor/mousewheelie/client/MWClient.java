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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.siphalor.mousewheelie.client.mixin.KeyMappingAccessor;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static final KeyMapping SORT_KEY_BINDING = new KeyMapping(
			"sort_inventory", InputConstants.Type.MOUSE, 2, "mw_category");

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

	public void onEndTick(Minecraft mc) {
		if (cooldown == 0) {
			if (InputConstants.isKeyDown(mc.getWindow().getWindow(),
					((KeyMappingAccessor)SORT_KEY_BINDING).getKey().getValue())) {
				if (mc.screen instanceof IContainerScreen screen) {
					LogUtils.getLogger().info("good screen");
					screen.mouseWheelie_triggerSort();
					cooldown = 11;
				}
			}
		}
		if (cooldown > 0) cooldown--;
	}

	public static boolean isOnLocalServer() {
		return CLIENT.getSingleplayerServer() != null;
	}
}
