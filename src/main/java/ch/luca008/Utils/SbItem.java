package ch.luca008.Utils;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Item.Enchant;
import ch.luca008.SpigotApi.Item.Item;
import ch.luca008.SpigotApi.Item.ItemAttribute;
import ch.luca008.SpigotApi.Item.Meta.Meta;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Objects;

public class SbItem extends Item {

    private final String privateName;

    public SbItem(String privateName, String uid, Material material, String name, List<String> lore, List<Enchant> enchantList, List<ItemFlag> flags, List<ItemAttribute> attributes, Meta meta, int repairCost, int customData, int damage, boolean invulnerable) {
        super(uid, material, name, lore, enchantList, flags, attributes, meta, repairCost, customData, damage, invulnerable);
        this.privateName = privateName;
    }

    public SbItem(String privateName, Item item){
        this(privateName, item.getUid(), item.getMaterial(), item.getName(), item.getLore(), item.getEnchantList(), item.getFlags(), item.getAttributes(), item.getMeta(), item.getRepairCost(), item.getCustomModelData(), item.getDamage(), item.isInvulnerable());
    }

    public SbItem(Item item){
        this(null, item);
    }

    public static SbItem fromJson(String json){
        try {
            JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader((JSONObject) new JSONParser().parse(json));
            return new SbItem(r.c("PrivateName") ? r.getString("PrivateName") : null, Item.fromJson(json));
        } catch (ParseException e) {
            System.err.println("Can't load SbItem with JSON:\n" + json);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject toJson(){
        JSONObject json = super.toJson();
        if(privateName!=null&&!privateName.isEmpty())json.put("PrivateName", privateName);
        return json;
    }

    public String getPrivateName() {
        return privateName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SbItem sbItem)) return false;
        if (!super.equals(o)) return false;
        return privateName.equals(sbItem.privateName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), privateName);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
