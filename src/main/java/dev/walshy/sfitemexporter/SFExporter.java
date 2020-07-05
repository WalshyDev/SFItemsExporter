package dev.walshy.sfitemexporter;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.setup.SlimefunItemSetup;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SFExporter {

    public static void main(String[] args) {
        new SFExporter().exportItems();
    }

    private void exportItems() {
        final JsonArray root = new JsonArray();

        final ServerMock server = MockBukkit.mock();
        final SlimefunPlugin instance = MockBukkit.load(SlimefunPlugin.class);

        registerDefaultTags(server);

        SlimefunItemSetup.setup(instance);

        int loaded = loadItems(root);

        System.out.println("\n\nLoaded in " + loaded + " items!");

        try (FileWriter fw = new FileWriter(new File("items.json"))) {
            fw.write(root.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int loadItems(JsonArray root) {
        int loaded = 0;
        for (SlimefunItem item : SlimefunPlugin.getRegistry().getAllSlimefunItems()) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("id", item.getID());
            itemObj.add("item", itemToJson(item.getItem()));
            itemObj.addProperty("category", item.getCategory().getUnlocalizedName());
            itemObj.addProperty("categoryTier", item.getCategory().getTier());

            final JsonObject itemSettings = new JsonObject();
            for (ItemSetting<?> setting : item.getItemSettings()) {
                itemSettings.addProperty(setting.getKey(), String.valueOf(setting.getDefaultValue()));
            }
            itemObj.add("settings", itemSettings);

            loadRecipe(item, itemObj);

            if (item.getResearch() != null) {
                itemObj.addProperty("research", item.getResearch().getKey().toString());
                itemObj.addProperty("researchCost", item.getResearch().getCost());
            }
            itemObj.addProperty("useableInWorkbench", item.isUseableInWorkbench());
            item.getWikipage().ifPresent(link -> itemObj.addProperty("wikiLink", link));

            itemObj.addProperty("electric", item instanceof EnergyNetComponent);
            if (item instanceof EnergyNetComponent) {
                EnergyNetComponent component = (EnergyNetComponent) item;
                itemObj.addProperty("electricType", component.getEnergyComponentType().toString());
                itemObj.addProperty("electricCapacity", component.getCapacity());
            }

            itemObj.addProperty("radioactive", item instanceof Radioactive);
            if (item instanceof Radioactive)
                itemObj.addProperty("radioactivityLevel",
                    ((Radioactive) item).getRadioactivity().toString());

            root.add(itemObj);
            loaded++;
        }

        return loaded;
    }

    private void loadRecipe(SlimefunItem item, JsonObject itemObj) {
        if (item.getRecipeType() != null && item.getRecipe() != null) {
            itemObj.addProperty("recipeType", item.getRecipeType().getKey().toString());

            JsonArray recipe = new JsonArray();
            for (ItemStack is : item.getRecipe()) {
                if (is != null)
                    recipe.add(itemToJson(is));
                else
                    recipe.add(JsonNull.INSTANCE);
            }
            itemObj.add("recipe", recipe);
        }
    }

    private void registerDefaultTags(ServerMock server) {
        // We really don't need these to be accurate, just fill them with some examples
        // that approximate the actual content
        server.createMaterialTag(NamespacedKey.minecraft("logs"),
            Material.OAK_LOG,
            Material.STRIPPED_OAK_LOG,
            Material.OAK_WOOD,
            Material.STRIPPED_OAK_WOOD,
            Material.ACACIA_LOG,
            Material.STRIPPED_ACACIA_LOG,
            Material.ACACIA_WOOD,
            Material.STRIPPED_ACACIA_WOOD
        );
        server.createMaterialTag(NamespacedKey.minecraft("wooden_trapdoors"),
            Material.OAK_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.SPRUCE_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR,
            Material.ACACIA_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR
        );
        server.createMaterialTag(NamespacedKey.minecraft("wooden_slabs"),
            Material.OAK_SLAB,
            Material.BIRCH_SLAB,
            Material.JUNGLE_SLAB,
            Material.SPRUCE_SLAB,
            Material.ACACIA_SLAB,
            Material.DARK_OAK_SLAB
        );
        server.createMaterialTag(NamespacedKey.minecraft("wooden_fences"),
            Material.OAK_FENCE,
            Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE,
            Material.SPRUCE_FENCE,
            Material.ACACIA_FENCE,
            Material.DARK_OAK_FENCE
        );
        server.createMaterialTag(NamespacedKey.minecraft("planks"),
            Material.OAK_PLANKS,
            Material.BIRCH_PLANKS,
            Material.SPRUCE_PLANKS,
            Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS,
            Material.DARK_OAK_PLANKS
        );
        server.createMaterialTag(NamespacedKey.minecraft("small_flowers"),
            Material.POPPY,
            Material.DANDELION,
            Material.AZURE_BLUET,
            Material.LILY_OF_THE_VALLEY
        );
        server.createMaterialTag(NamespacedKey.minecraft("leaves"),
            Material.OAK_LEAVES,
            Material.BIRCH_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES,
            Material.DARK_OAK_LEAVES
        );
        server.createMaterialTag(NamespacedKey.minecraft("saplings"),
            Material.OAK_SAPLING,
            Material.BIRCH_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING
        );
        server.createMaterialTag(NamespacedKey.minecraft("coral_blocks"),
            Material.BRAIN_CORAL_BLOCK,
            Material.BUBBLE_CORAL_BLOCK,
            Material.FIRE_CORAL_BLOCK,
            Material.HORN_CORAL_BLOCK,
            Material.TUBE_CORAL_BLOCK
        );
        server.createMaterialTag(NamespacedKey.minecraft("corals"),
            Material.BRAIN_CORAL,
            Material.BUBBLE_CORAL,
            Material.FIRE_CORAL,
            Material.HORN_CORAL,
            Material.TUBE_CORAL
        );
    }

    private JsonObject itemToJson(final ItemStack is) {
        JsonObject mcItem = new JsonObject();
        mcItem.addProperty("material", is.getType().toString());
        final ItemMeta im = is.getItemMeta();
        if (im.hasDisplayName())
            mcItem.addProperty("name", im.getDisplayName().replace(ChatColor.COLOR_CHAR, '&'));

        JsonArray lore = new JsonArray();
        if (im.hasLore())
            im.getLore().stream().map(s -> s.replace(ChatColor.COLOR_CHAR, '&')).forEach(lore::add);
        mcItem.add("lore", lore);

        return mcItem;
    }
}
