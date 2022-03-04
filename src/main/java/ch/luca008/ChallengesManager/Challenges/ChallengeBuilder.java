package ch.luca008.ChallengesManager.Challenges;

import ch.luca008.ChallengesManager.Required.Required;
import ch.luca008.ChallengesManager.Required.RequiredLoader;
import ch.luca008.ChallengesManager.Reward;
import ch.luca008.Items.Item;
import ch.luca008.Items.ItemBuilder;
import ch.luca008.Utils.JsonUtils;
import org.bukkit.Material;
import org.json.simple.JSONObject;

import java.util.UUID;

public class ChallengeBuilder {
    private UUID uuid = UUID.randomUUID();
    private UUID categoryUuid;
    private String name = "defaultName";
    private String description;
    private Challenge.ChallengeType type = Challenge.ChallengeType.INVENTORY;
    private boolean active = false;
    private Item icon = new ItemBuilder().setMaterial(Material.STONE).createItem();
    private int page = 1;
    private int slot = 10;
    private int redoneLimit = -1;
    private long lastEdited = -1;
    private Required required;
    private Reward reward;

    public ChallengeBuilder(){}//pour build sans json

    public ChallengeBuilder(JSONObject json){
        if(json.containsKey("uuid")){
            uuid = UUID.fromString((String) json.get("uuid"));
        }
        if(json.containsKey("name")){
            name = (String) json.get("name");
        }
        if(json.containsKey("category")){
            categoryUuid = UUID.fromString((String)json.get("category"));
        }
        if(json.containsKey("description")){
            description = (String) json.get("description");
        }
        if(json.containsKey("type")){
            type = Challenge.ChallengeType.valueOf((String) json.get("type"));
        }
        if(json.containsKey("active")){
            active = (boolean) json.get("active");
        }
        if(json.containsKey("icon")){
            icon = Item.fromJson(((JSONObject)json.get("icon")).toString());
        }
        if(json.containsKey("page")){
            page = JsonUtils.getInt(json,"page");
        }
        if(json.containsKey("slot")){
            slot = JsonUtils.getInt(json,"slot");
        }
        if(json.containsKey("lastEdited")){
            lastEdited = JsonUtils.getLong(json,"lastEdited");
        }else lastEdited = -1;
        if(json.containsKey("redoneLimit")){
            redoneLimit = JsonUtils.getInt(json, "redoneLimit");
        }
        if(json.containsKey("required")){
            JSONObject reqJson = (JSONObject) json.get("required");
            if(!((JSONObject)reqJson.get("Required")).isEmpty()){
                required = new RequiredLoader().load(reqJson);
            }
        }
        if(json.containsKey("reward")){
            reward = new Reward((JSONObject) json.get("reward"));
        }
    }

    public ChallengeBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ChallengeBuilder setCategoryUuid(UUID categoryUuid) {
        this.categoryUuid = categoryUuid;
        return this;
    }

    public ChallengeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ChallengeBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ChallengeBuilder setType(Challenge.ChallengeType type) {
        this.type = type;
        return this;
    }

    public ChallengeBuilder setActive(boolean active) {
        this.active = active;
        return this;
    }

    public ChallengeBuilder setIcon(Item icon) {
        this.icon = icon;
        return this;
    }

    public ChallengeBuilder setPage(int page) {
        this.page = page;
        return this;
    }

    public ChallengeBuilder setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public ChallengeBuilder setRedoneLimit(int redoneLimit) {
        this.redoneLimit = redoneLimit;
        return this;
    }

    public ChallengeBuilder setLastEdited(long lastEdited) {
        this.lastEdited = lastEdited;
        return this;
    }

    public ChallengeBuilder setRequired(Required required) {
        this.required = required;
        return this;
    }

    public ChallengeBuilder setReward(Reward reward) {
        this.reward = reward;
        return this;
    }

    public Challenge createChallenge() {
        return new Challenge(uuid, categoryUuid, name, description, type, active, icon, page, slot, redoneLimit, lastEdited, required, reward);
    }

    public void flash(Challenge challenge){
        challenge.setCategoryUuid(categoryUuid);
        challenge.setName(name);
        challenge.setDescription(description);
        challenge.setActive(active);
        challenge.setIcon(icon);
        challenge.setPage(page);
        challenge.setSlot(slot);
        challenge.setRedoneLimit(redoneLimit);
        if(required!=null&&required.getType().match(challenge.getType())){ //Interdit de changer le requis une fois set la première fois. Vérifie si le nouveau requis est tjs du meme type
            challenge.setRequired(required);
        }else{
            challenge.setRequired(null);
        }
        challenge.setReward(reward);
    }
}