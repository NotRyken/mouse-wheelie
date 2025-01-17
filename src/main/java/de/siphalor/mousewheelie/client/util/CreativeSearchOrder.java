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

package de.siphalor.mousewheelie.client.util;

import de.siphalor.mousewheelie.MWConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public class CreativeSearchOrder {
	private static FeatureFlagSet lastFeatureSet = null;
	private static final Object2IntMap<StackMatcher> stackToSearchPositionLookup = new Object2IntOpenHashMap<>();
	static {
		stackToSearchPositionLookup.defaultReturnValue(Integer.MAX_VALUE);
	}
	private static final ReadWriteLock stackToSearchPositionLookupLock = new ReentrantReadWriteLock();

	public static Lock getReadLock() {
		return stackToSearchPositionLookupLock.readLock();
	}

	public static int getStackSearchPosition(ItemStack stack) {
		int pos = stackToSearchPositionLookup.getInt(StackMatcher.of(stack));
		if (pos == Integer.MAX_VALUE) {
			pos = stackToSearchPositionLookup.getInt(StackMatcher.ignoreNbt(stack));
		}
		return pos;
	}

	// Called on config change and when the feature set changes (on world join)
	public static void refreshItemSearchPositionLookup() {
		if (MWConfig.sort.optimizeCreativeSearchSort) {
			Minecraft client = Minecraft.getInstance();
			if (client.level == null) {
				return;
			}
			FeatureFlagSet enabledFeatures = client.level.enabledFeatures();

			if (stackToSearchPositionLookup.isEmpty() || !Objects.equals(enabledFeatures, lastFeatureSet)) {
				CreativeModeTabs.tryRebuildTabContents(enabledFeatures, true, client.level.registryAccess());
				Collection<ItemStack> displayStacks = new ArrayList<>(CreativeModeTabs.searchTab().getDisplayItems());
				new Thread(() -> {
					Lock lock = stackToSearchPositionLookupLock.writeLock();
					lock.lock();
					stackToSearchPositionLookup.clear();
					if (displayStacks.isEmpty()) {
						lock.unlock();
						return;
					}

					int i = 0;
					for (ItemStack stack : displayStacks) {
						StackMatcher plainMatcher = StackMatcher.ignoreNbt(stack);
						if (!stack.hasTag() || !stackToSearchPositionLookup.containsKey(plainMatcher)) {
							stackToSearchPositionLookup.put(plainMatcher, i);
							i++;
						}
						stackToSearchPositionLookup.put(StackMatcher.of(stack), i);
						i++;
					}
					lock.unlock();
				}, "Mouse Wheelie: creative search stack position lookup builder").start();
			}

		} else {
			Lock lock = stackToSearchPositionLookupLock.writeLock();
			lock.lock();
			stackToSearchPositionLookup.clear();
			lock.unlock();
		}
	}
}
