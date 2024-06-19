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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import com.google.common.base.Suppliers;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.inventory.sort.InventorySorter;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
@Mixin(HandledScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen implements IContainerScreen {
	protected MixinAbstractContainerScreen(Text textComponent_1) {
		super(textComponent_1);
	}

	@Shadow
	protected abstract void onMouseClick(Slot slot_1, int int_1, int int_2, SlotActionType slotActionType_1);

	@Shadow
	@Final
	protected ScreenHandler handler;

	@Shadow
	protected Slot focusedSlot;

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Unique
	private final Supplier<ContainerScreenHelper<HandledScreen<ScreenHandler>>> screenHelper = Suppliers.memoize(
			() -> ContainerScreenHelper.of((HandledScreen<ScreenHandler>) (Object) this, (slot, data, slotActionType) -> new InteractionManager.CallbackEvent(() -> {
				onMouseClick(slot, ((ISlot) slot).mouseWheelie_getIdInContainer(), data, slotActionType);
				return InteractionManager.TICK_WAITER;
			}, true))
	);

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean mouseWheelie_triggerSort() {
		if (focusedSlot == null)
			return false;
		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player.getAbilities().creativeMode
				&& GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) != 0
				&& (!focusedSlot.getStack().isEmpty() == handler.getCursorStack().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter(screenHelper.get(), (HandledScreen<?>) (Object) this, focusedSlot);
		SortMode sortMode;
		if (hasShiftDown()) {
			sortMode = MWConfig.sort.shiftSort;
		} else if (hasControlDown()) {
			sortMode = MWConfig.sort.controlSort;
		} else {
			sortMode = MWConfig.sort.primarySort;
		}
		if (sortMode == null) return false;
		sorter.sort(sortMode);
		return true;
	}
}
