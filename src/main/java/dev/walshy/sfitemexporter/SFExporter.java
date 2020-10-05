package dev.walshy.sfitemexporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.setup.ResearchSetup;
import io.github.thebusybiscuit.slimefun4.implementation.setup.SlimefunItemSetup;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;

public class SFExporter {

    public static void main(String[] args) {
        new SFExporter().exportItems();
    }

    public JsonArray loadSFItems() {
        final JsonArray root = new JsonArray();

        MockBukkit.mock();
        final SlimefunPlugin instance = MockBukkit.load(SlimefunPlugin.class);

        SlimefunItemSetup.setup(instance);
        ResearchSetup.setupResearches();
        int loaded = loadItems(root);
        System.out.println("Loaded in " + loaded + " items!");

        return root;
    }

    public void exportItems() {
        final JsonArray root = loadSFItems();

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

    private JsonObject itemToJson(final ItemStack is) {
        JsonObject mcItem = new JsonObject();
        mcItem.addProperty("material", is.getType().toString());

        if (is.getAmount() > 1)
            mcItem.addProperty("amount", is.getAmount());

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
