package dev.walshy.sfitemexporter;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.setup.ResearchSetup;
import io.github.thebusybiscuit.slimefun4.implementation.setup.SlimefunItemSetup;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main class for the "Slimefun Items Exporter",
 * simply run the exported jar and it will generate an "items.json" file by default.
 *
 * @author Walshy
 * @author TheBusyBiscuit
 */
public class SFExporter {

    private final Logger logger = Logger.getLogger("SlimefunItemsExporter");
    private final JsonArray root = new JsonArray();

    /**
     * This is the main method of this software
     *
     * @param args Optional command line arguments
     */
    public static void main(String[] args) {
        new SFExporter().exportItems(new File("items.json"));
    }

    /**
     * This method loads all {@link SlimefunItem SlimefunItems} into a {@link JsonArray}.
     *
     * @return A {@link JsonArray} with the data for every {@link SlimefunItem}
     */
    public JsonArray getAllSlimefunItems() {
        if (MockBukkit.isMocked()) {
            // We have already intiliazed all items, simply return the cached version
            return root;
        }

        MockBukkit.mock();
        final SlimefunPlugin instance = MockBukkit.load(SlimefunPlugin.class);

        SlimefunItemSetup.setup(instance);
        ResearchSetup.setupResearches();
        loadItems();

        return root;
    }

    /**
     * This exports the generated json data to the specified output file.
     *
     * @param file The output {@link File}
     */
    public void exportItems(File file) {
        final JsonArray items = getAllSlimefunItems();

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(items.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This loads all {@link SlimefunItem Slimefunitems} into the root {@link JsonArray}.
     */
    private void loadItems() {
        int items = 0;

        for (SlimefunItem item : SlimefunPlugin.getRegistry().getAllSlimefunItems()) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", item.getId());
            jsonObj.add("item", getAsJson(item.getItem()));
            jsonObj.addProperty("category", item.getCategory().getUnlocalizedName());
            jsonObj.addProperty("categoryTier", item.getCategory().getTier());

            final JsonObject itemSettings = new JsonObject();

            for (ItemSetting<?> setting : item.getItemSettings()) {
                itemSettings.addProperty(setting.getKey(), String.valueOf(setting.getDefaultValue()));
            }

            jsonObj.add("settings", itemSettings);

            loadRecipe(item, jsonObj);

            if (item.getResearch() != null) {
                jsonObj.addProperty("research", item.getResearch().getKey().toString());
                jsonObj.addProperty("researchCost", item.getResearch().getCost());
            }

            jsonObj.addProperty("useableInWorkbench", item.isUseableInWorkbench());
            item.getWikipage().ifPresent(link -> jsonObj.addProperty("wikiLink", link));

            jsonObj.addProperty("electric", item instanceof EnergyNetComponent);
            if (item instanceof EnergyNetComponent) {
                EnergyNetComponent component = (EnergyNetComponent) item;
                jsonObj.addProperty("electricType", component.getEnergyComponentType().toString());
                jsonObj.addProperty("electricCapacity", component.getCapacity());
            }

            jsonObj.addProperty("radioactive", item instanceof Radioactive);

            if (item instanceof Radioactive) {
                jsonObj.addProperty("radioactivityLevel", ((Radioactive) item).getRadioactivity().toString());
            }

            root.add(jsonObj);
            items++;
        }

        logger.log(Level.INFO, "Loaded in {0} items!", items);
    }

    /**
     * This loads the recipe for this {@link SlimefunItem} into the specified {@link JsonObject}.
     *
     * @param item The {@link SlimefunItem}
     * @param json Our target {@link JsonObject}
     */
    private void loadRecipe(SlimefunItem item, JsonObject json) {
        if (item.getRecipeType() != null && item.getRecipe() != null) {
            json.addProperty("recipeType", item.getRecipeType().getKey().toString());

            JsonArray recipe = new JsonArray();
            for (ItemStack is : item.getRecipe()) {
                if (is != null) {
                    recipe.add(getAsJson(is));
                } else {
                    recipe.add(JsonNull.INSTANCE);
                }

            }
            json.add("recipe", recipe);
        }
    }

    /**
     * This converts the given {@link ItemStack} into a {@link JsonObject}.
     *
     * @param is Our {@link ItemStack}.
     * @return The {@link JsonObject}-representation of this {@link ItemStack}
     */
    private JsonObject getAsJson(final ItemStack is) {
        final JsonObject json = new JsonObject();
        json.addProperty("material", is.getType().toString());

        if (is.getAmount() > 1) {
            json.addProperty("amount", is.getAmount());
        }

        final ItemMeta im = is.getItemMeta();
        if (im.hasDisplayName()) {
            json.addProperty("name", im.getDisplayName().replace(ChatColor.COLOR_CHAR, '&'));
        }

        final JsonArray lore = new JsonArray();
        if (im.hasLore()) {
            im.getLore().stream().map(s -> s.replace(ChatColor.COLOR_CHAR, '&')).forEach(lore::add);
        }
        json.add("lore", lore);

        return json;
    }
}