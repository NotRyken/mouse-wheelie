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

package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

@Environment(EnvType.CLIENT)
public class CreativeContainerScreenHelper<T extends CreativeInventoryScreen> extends ContainerScreenHelper<T> {
	public CreativeContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		super(screen, clickEventFactory);
	}

	@Override
	public int getScope(Slot slot, boolean preferSmallerScopes) {
		if (screen.isInventoryTabSelected()) {
			return super.getScope(slot, preferSmallerScopes);
		}
		if (slot.inventory instanceof PlayerInventory) {
			if (isHotbarSlot(slot)) {
				return 0;
			}
		}
		return INVALID_SCOPE;
	}
}
