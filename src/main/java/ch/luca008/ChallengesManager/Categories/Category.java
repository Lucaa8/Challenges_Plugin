package ch.luca008.ChallengesManager.Categories;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.SpigotApi.Item.Meta.Skull;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Category {

    private UUID uuid;
    private String name;
    private String description;
    private boolean active;
    private Material color;
    private SbItem icon;
    private int page;
    private int slot;
    //only used when a category is edited
    private long lastEdited;
    public boolean asChanged = false;

    public Category(UUID uuid, String name, String description, boolean active, Material color, SbItem icon, int page, int slot, long lastEdited) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.active = active;
        this.color = color;
        this.icon = icon;
        this.page = page;
        this.slot = slot;
        this.lastEdited = lastEdited==-1?System.currentTimeMillis():lastEdited;
    }

    public void flash(JSONObject json){
        new CategoryBuilder(json).flash(this);
        lastEdited = System.currentTimeMillis();
        asChanged = true;
    }

    //SETTER/GETTER
    public UUID getUuid() {
        return uuid;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public boolean hasDescription(){
        return description!=null&&!description.isEmpty();
    }
    public String getDescription() {
        return description;
    }

    public void setActive(boolean active){
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }

    public void setColor(Material color) {
        this.color = color;
    }
    public Material getColor() {
        return color;
    }

    public void setIcon(SbItem icon) {
        this.icon = icon;
    }
    public SbItem getIcon() {
        return icon;
    }

    public void setPage(int page) {
        this.page = page;
    }
    public int getPage() {
        return page;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
    public int getSlot() {
        return slot;
    }

    public long getLastEdited(){
        return lastEdited;
    }
    //SETTER/GETTER

    //MISC
    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        j.put("uuid", uuid.toString());
        j.put("name", name);
        if(description!=null&&!description.isEmpty())j.put("description", description);
        j.put("active", active);
        j.put("color", color.name());
        j.put("icon", icon.toJson());
        j.put("page", page);
        j.put("slot", slot);
        if(lastEdited!=-1)j.put("lastEdited", lastEdited);
        return j;
    }

    public void unload(){
        try{
            if(asChanged){
                File f = new File(Manager.categoriesBaseDirectory, getUuid().toString()+".json");
                if(!f.exists()){
                    f.createNewFile();
                }
                Files.write(Paths.get(f.toURI()), JsonUtils.prettyJson(toJson()).getBytes(StandardCharsets.UTF_8));
            }
        }catch (IOException e) {
            System.err.println("Can't unload category with uuid " + getUuid() + ".");
            e.printStackTrace();
        }
    }

    public ItemStack toItem(Storage storage, UniPlayer player){
        ItemBuilder ib = new ItemBuilder();
        if(storage.isUnlocked(this).size()>0||(!isActive()&&!Perms.Permission.CHALLENGE_ADMIN_BYPASS.hasPermission(player))){
            ib = ItemUtils.getUnavailableItem(true,isActive(),player);
        }else{
            ib.setMaterial(getIcon().getMaterial());
            if(getIcon().hasMeta()){
                ib.setMeta(getIcon().getMeta());
            }
            if(getIcon().getCustomData()>0){
                ib.setCustomData(getIcon().getCustomData());
            }
            ib.setLore(StringUtils.asLore((!isActive()?"§4(Inactive)§r\n":"")+Challenges.getGlobalConfig().getCategoriesDescriptionColor()+getDescription()+"\n\n"+player.getChallengeMessage("Category-Lore")));
        }
        ib.setName(player.getChallengeMessage("Category-Name",Map.entry("{0}",getName())));
        ib.setUid("Cat_"+getUuid());
        SbItem i = new SbItem(ib.createItem());
        ItemStack is = i.toItemStack(1);
        //adds a skullowner if item's type = PLAYER_HEAD
        if(i.getMeta() instanceof Skull){
            Skull s = (Skull) i.getMeta();
            if(s.getOwningType()==Skull.SkullOwnerType.PLAYER){
                is = s.applyOwner(is, player.getOfflinePlayer().getUniqueId());
            }
        }
        return ItemUtils.removeNamedColor(SpigotApi.getNBTTagApi().getNBT(is).setTag("HideFlags",127).getBukkitItem(), getName());
    }

    public void reset(UUID island){
        for(Challenge c : Challenges.getManager().getIndex().getChallenges(this)){
            c.reset(island);
        }
    }

    @Override
    public String toString() {
        return "Category{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", color=" + color +
                ", icon=" + icon.toString() +
                ", page=" + page +
                ", slot=" + slot +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return uuid.equals(category.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
