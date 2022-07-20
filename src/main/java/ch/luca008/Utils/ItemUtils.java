package ch.luca008.Utils;

import ch.luca008.Items.ItemBuilder;
import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.UniPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemUtils {

    public static ItemBuilder getUnavailableItem(boolean isCategory, boolean isActive, UniPlayer player){
        ItemBuilder ib = new ItemBuilder();
        ib.setMaterial(Material.RED_STAINED_GLASS_PANE);
        String key = isCategory ? "Category-":"Challenge-";
        if(!isActive){
            ib.setLore(StringUtils.asLore("\n"+player.getChallengeMessage(key+"Item-Inactive")));
        }else{
            ib.setLore(StringUtils.asLore("\n"+player.getChallengeMessage(key+"Item-Locked")));
        }
        return ib;
    }

    /**
     * Gets the 7(or less) itemstacks which needs to be displayed at this page (Sample inventory: required and reward)
     * @param page Starts from 0
     * @return A 7 or less itemstacks array.
     */
    public static ItemStack[] getItemstacks(ItemStack[] full, int page){
        if(full==null||full.length==0){
            return new ItemStack[0];
        }
        int startIndex = page*7;
        int maxIndex = startIndex+6;
        int currentLength = full.length;
        if(currentLength<=7||startIndex>=currentLength)return full;
        ItemStack[] items = new ItemStack[maxIndex>=currentLength?currentLength-startIndex:7];
        System.arraycopy(full, startIndex, items, 0, items.length);
        return items;
    }

    public static ItemStack applySignature(ItemStack item, String encodedURL){
        ReflectionApi s = SpigotApi.getReflectionApi();
        Object nmsItem = SpigotApi.getNbtApi().getNMSItem(item);
        Object tags = SpigotApi.getNbtApi().getNBT(item).getTags(); //NBTTagCompound "tag"
        Class<?> itemstack = s.spigot().getNMSClass("ItemStack");
        Class<?> nbtbase = s.spigot().getNMSClass("NBTBase");
        Class<?> nbtstring = s.spigot().getNMSClass("NBTTagString");
        Class<?> nbtlist = s.spigot().getNMSClass("NBTTagList");
        Object id = null;
        Object val = SpigotApi.getReflectionApi().invoke(nbtstring, nbtstring, "a", new Class[]{String.class}, encodedURL);
        if(Integer.parseInt(s.getServerVersion().split("_")[1])<16) {//1.15-
            id = SpigotApi.getReflectionApi().invoke(nbtstring, nbtstring, "a", new Class[]{String.class}, UUID.randomUUID().toString());
        }
        Object compoundSO = null;
        Object compoundProp = null;
        Object compound0 = null;
        Object textures = null;
        try{
            if(id==null){//rentre uniquement si server version >= 1.16
                id = s.spigot().getNMSClass("NBTTagIntArray").getConstructor(List.class).newInstance(Arrays.asList(0,0,0,0));
            }
            textures = nbtlist.getConstructor().newInstance();
            compoundSO = s.spigot().getNMSClass("NBTTagCompound").getConstructor().newInstance();
            compoundProp = compoundSO.getClass().getConstructor().newInstance();
            compound0 = compoundSO.getClass().getConstructor().newInstance();
        }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
            System.err.println("Can't get a new instance for NBTTagCompund or NBTTagList.");
            e.printStackTrace();
        }
        if(compound0!=null){
            s.invoke(compound0.getClass(), compound0, "set", new Class[]{String.class, nbtbase}, "Value", val);
        }
        if(textures!=null){
            s.invoke(textures.getClass(), textures, "b", new Class[]{int.class, nbtbase}, 0, compound0);
        }
        if(compoundProp!=null){
            s.invoke(compoundProp.getClass(), compoundProp, "set", new Class[]{String.class, nbtbase}, "textures", textures);
        }
        if(compoundSO!=null){
            s.invoke(compoundSO.getClass(), compoundSO, "set", new Class[]{String.class, nbtbase}, "Id", id);
            s.invoke(compoundSO.getClass(), compoundSO, "set", new Class[]{String.class, nbtbase}, "Properties", compoundProp);
        }
        s.invoke(tags.getClass(), tags, "set", new Class[]{String.class, nbtbase}, "SkullOwner", compoundSO);
        s.invoke(itemstack, nmsItem, "setTag", new Class[]{tags.getClass()}, tags);
        item = SpigotApi.getNbtApi().getBukkitItem(nmsItem);
        return item;
    }

    /**
     * Replace § with & into an item's displayname
     * <p>
     * Exemple -> Name=§9Fish§Chips. Output with keepFirstOne : §9Fish&Chips. Without : &9Fish&Chips
     * @param item the item whose name needs to change
     * @param initialName the name with the &
     * @return the exact same item with his name changed
     */
    public static ItemStack removeNamedColor(ItemStack item, String initialName){
        if(item.hasItemMeta()&&item.getItemMeta().hasDisplayName()){
            if(initialName.contains("&")){
                ItemMeta im = item.getItemMeta();
                int start = im.getDisplayName().toLowerCase().indexOf(initialName.toLowerCase().replace("&","§"));
                if(start!=-1){//Obligatoire car paper spigot 1.16.5 s'amuse à retirer les codes couleurs majuscules (Fish§Chips -> Fish§chips), alors contais et replace ne fonctionnent pas
                    char[] cArr = im.getDisplayName().toCharArray();
                    System.arraycopy(initialName.toCharArray(), 0, cArr, start, initialName.toCharArray().length);
                    im.setDisplayName(new String(cArr));
                    item.setItemMeta(im);
                }
            }
        }
        return item;
    }
}
