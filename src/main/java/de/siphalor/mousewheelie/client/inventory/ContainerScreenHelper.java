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

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper<T extends HandledScreen<?>> {
	protected final T screen;
	protected final ClickEventFactory clickEventFactory;
	public static final int INVALID_SCOPE = Integer.MAX_VALUE;

	protected ContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		this.screen = screen;
		this.clickEventFactory = clickEventFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T extends HandledScreen<?>> ContainerScreenHelper<T> of(T screen, ClickEventFactory clickEventFactory) {
		if (screen instanceof CreativeInventoryScreen) {
			return (ContainerScreenHelper<T>) new CreativeContainerScreenHelper<>((CreativeInventoryScreen) screen, clickEventFactory);
		}
		return new ContainerScreenHelper<>(screen, clickEventFactory);
	}

	public InteractionManager.InteractionEvent createClickEvent(Slot slot, int action, SlotActionType actionType) {
		return clickEventFactory.create(slot, action, actionType);
	}

	public boolean isHotbarSlot(Slot slot) {
		return ((ISlot) slot).mouseWheelie_getIndexInInv() < 9;
	}

	public int getScope(Slot slot) {
		return getScope(slot, false);
	}

	public int getScope(Slot slot, boolean preferSmallerScopes) {
		if (slot.inventory == null || ((ISlot) slot).mouseWheelie_getIndexInInv() >= slot.inventory.size() || !slot.canInsert(ItemStack.EMPTY)) {
			return INVALID_SCOPE;
		}
		if (screen instanceof AbstractInventoryScreen) {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					return 0;
				} else if (((ISlot) slot).mouseWheelie_getIndexInInv() >= 40) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					if (MWConfig.general.hotbarScoping == MWConfig.General.HotbarScoping.HARD
							|| MWConfig.general.hotbarScoping == MWConfig.General.HotbarScoping.SOFT && preferSmallerScopes) {
						return -1;
					}
				}
				return 0;
			}
			return 1;
		}
	}
}
