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

package de.siphalor.mousewheelie;

import com.google.common.base.CaseFormat;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.tweed4.annotated.*;
import de.siphalor.tweed4.config.ConfigEnvironment;
import de.siphalor.tweed4.config.ConfigScope;
import de.siphalor.tweed4.config.constraints.RangeConstraint;
import de.siphalor.tweed4.data.DataList;
import de.siphalor.tweed4.data.DataObject;
import de.siphalor.tweed4.data.DataValue;

@SuppressWarnings({"WeakerAccess", "unused"})
@ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = {"tweed4:lang_json_descriptions", "tweed4:coat"}, casing = CaseFormat.LOWER_HYPHEN)
@AConfigBackground("textures/block/green_concrete_powder.png")
public class MWConfig {
	@AConfigEntry(comment = "General settings")
	public static General general = new General();

	@AConfigBackground("textures/block/acacia_log.png")
	public static class General {
		@AConfigEntry(
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		)
		public int interactionRate = 10;
		@AConfigEntry(
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		)
		public int integratedInteractionRate = 1;

		@AConfigEntry(comment = "Whether item types should check nbt data.\nThis is for example used by scrolling and drop-clicking.\nNONE disables this, ALL checks for exactly the same nbt and SOME allows for differences in damage and enchantments.")
		public ItemStackUtils.NbtMatchMode itemKindsNbtMatchMode = ItemStackUtils.NbtMatchMode.SOME;

		public enum HotbarScoping {HARD, SOFT, NONE}

		public HotbarScoping hotbarScoping = HotbarScoping.SOFT;

		@AConfigListener("interaction-rate")
		public void onReloadInteractionRate() {
			if (!MWClient.isOnLocalServer()) {
				InteractionManager.setTickRate(interactionRate);
			}
		}

		@AConfigListener("integrated-interaction-rate")
		public void onReloadIntegratedInteractionRate() {
			if (MWClient.isOnLocalServer()) {
				InteractionManager.setTickRate(integratedInteractionRate);
			}
		}
	}

	public static Sort sort = new Sort();

	@AConfigBackground("textures/block/barrel_top.png")
	public static class Sort {
		public SortMode primarySort = SortMode.CREATIVE;
		public SortMode shiftSort = SortMode.QUANTITY;
		public SortMode controlSort = SortMode.ALPHABET;
		public boolean serverAcceleratedSorting = true;

		@AConfigEntry(scope = ConfigScope.SMALLEST)
		public boolean optimizeCreativeSearchSort = true;

		@AConfigListener("optimize-creative-search-sort")
		public void onReloadOptimizeCreativeSearchSort() {
			CreativeSearchOrder.refreshItemSearchPositionLookup();
		}
	}

	@AConfigFixer
	public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void fixConfig(O dataObject, O rootObject) {
		if (dataObject.has("general") && dataObject.get("general").isObject()) {
			O general = dataObject.get("general").asObject();

			general.remove("hotbar-scope");
		}
	}

	@AConfigFixer("sort")
	public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void fixSortModes(O sort, O mainConfig) {
		if (!sort.has("optimize-creative-search-sort")) {
			if (sort.getString("primary-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("primary-sort", "creative");
			}
			if (sort.getString("shift-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("shift-sort", "creative");
			}
			if (sort.getString("control-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("control-sort", "creative");
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat) {
		moveConfigEntry(root, origin, name, destCat, name);
	}

	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat, String newName) {
		if (origin.has(name)) {
			O dest;
			if (root.has(destCat) && root.get(destCat).isObject()) {
				dest = root.get(destCat).asObject();
			} else {
				dest = root.addObject(destCat);
			}
			dest.set(newName, origin.get(name));
			origin.remove(name);
		}
	}
}
