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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper<T extends AbstractContainerScreen<?>> {
	protected final T screen;
	protected final ClickEventFactory clickEventFactory;
	public static final int INVALID_SCOPE = Integer.MAX_VALUE;

	protected ContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		this.screen = screen;
		this.clickEventFactory = clickEventFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractContainerScreen<?>> ContainerScreenHelper<T> of(T screen, ClickEventFactory clickEventFactory) {
		if (screen instanceof CreativeModeInventoryScreen) {
			return (ContainerScreenHelper<T>) new CreativeContainerScreenHelper<>((CreativeModeInventoryScreen) screen, clickEventFactory);
		}
		return new ContainerScreenHelper<>(screen, clickEventFactory);
	}

	public InteractionManager.InteractionEvent createClickEvent(Slot slot, int action, ClickType actionType) {
		return clickEventFactory.create(slot, action, actionType);
	}

	public boolean isHotbarSlot(Slot slot) {
		return ((ISlot) slot).mouseWheelie_getIndexInInv() < 9;
	}

	public int getScope(Slot slot) {
		return getScope(slot, false);
	}

	public int getScope(Slot slot, boolean preferSmallerScopes) {
		if (slot.container == null || ((ISlot) slot).mouseWheelie_getIndexInInv() >= slot.container.getContainerSize() || !slot.mayPlace(ItemStack.EMPTY)) {
			return INVALID_SCOPE;
		}
		if (screen instanceof EffectRenderingInventoryScreen) {
			if (slot.container instanceof Inventory) {
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
			if (slot.container instanceof Inventory) {
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
