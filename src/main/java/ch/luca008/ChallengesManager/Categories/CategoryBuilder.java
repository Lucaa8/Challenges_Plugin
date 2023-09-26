package ch.luca008.ChallengesManager.Categories;

import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.SbItem;
import org.bukkit.Material;
import org.json.simple.JSONObject;

import java.util.UUID;

public class CategoryBuilder {

    private UUID uuid;
    private String name;
    private String description;
    private boolean active;
    private Material color;
    private SbItem icon;
    private int page;
    private int slot;
    private long lastEdited;

    public CategoryBuilder(){}//pour build sans json

    public CategoryBuilder(JSONObject json){
        if(json.containsKey("uuid")){
            uuid = UUID.fromString((String) json.get("uuid"));
        }//pas besoin d'en set un rdm si !contains car c'est deja fais dans le createCategory()
        if(json.containsKey("name")){
            name = (String) json.get("name");
        }else name = "defaultName";
        if(json.containsKey("description")){
            description = (String) json.get("description");
        }
        if(json.containsKey("active")){
            active = (boolean) json.get("active");
        }else active = false;
        if(json.containsKey("color")){
            color = Material.valueOf((String)json.get("color"));
        }else color = Material.BLACK_STAINED_GLASS_PANE;
        if(json.containsKey("icon")){
            icon = SbItem.fromJson(json.get("icon").toString());
        }else icon = new SbItem(new ItemBuilder().setMaterial(Material.STONE).createItem());
        if(json.containsKey("page")){
            page = JsonUtils.getInt(json,"page");
        }else page = 1;
        if(json.containsKey("slot")){
            slot = JsonUtils.getInt(json,"slot");
        }else slot = 10;
        if(json.containsKey("lastEdited")){
            lastEdited = JsonUtils.getLong(json,"lastEdited");
        }else lastEdited = -1;
    }

    public CategoryBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public CategoryBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CategoryBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CategoryBuilder setActive(boolean active) {
        this.active = active;
        return this;
    }

    public CategoryBuilder setColor(String color) {
        this.color = Material.valueOf(color);
        return this;
    }

    public CategoryBuilder setIcon(SbItem icon) {
        this.icon = icon;
        return this;
    }

    public CategoryBuilder setPage(int page) {
        this.page = page;
        return this;
    }

    public CategoryBuilder setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public CategoryBuilder setLastEdited(int lastEdited){
        this.lastEdited = lastEdited;
        return this;
    }

    public Category createCategory() {
        return new Category(uuid==null?UUID.randomUUID():uuid, name, description, active, color, icon, page, slot, lastEdited);
    }

    public void flash(Category category){
        category.setName(name);
        category.setDescription(description);
        category.setActive(active);
        category.setColor(color);
        category.setIcon(icon);
        category.setPage(page);
        category.setSlot(slot);
    }
}
