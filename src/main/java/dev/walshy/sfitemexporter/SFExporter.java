package dev.walshy.sfitemexporter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SFExporter extends JavaPlugin {

    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        try {
            exportItems();
        } catch (Exception e) {
            // In case there's some stupid thing happening
            e.printStackTrace();
        }
        Bukkit.shutdown();
    }

    private void exportItems() {
        JsonArray root = new JsonArray();

        for (SlimefunItem item : SlimefunPlugin.getRegistry().getAllSlimefunItems()) {
            getLogger().info("Exporting " + item.getID());

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
        }

        try (FileWriter fw = new FileWriter(new File("output.json"))) {
            fw.write(root.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
