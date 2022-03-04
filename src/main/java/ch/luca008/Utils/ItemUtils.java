package ch.luca008.Utils;

import NBT.NMSManager;
import ch.luca008.Items.ItemBuilder;
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
        Object nmsItem = NMSManager.getNMSItem(item);
        Object tags = NMSManager.getTags(item);//NBTTagCompound "tag"
        Class<?> itemstack = NMSManager.getNMSClass("ItemStack");
        Class<?> nbtbase = NMSManager.getNMSClass("NBTBase");
        Class<?> nbtstring = NMSManager.getNMSClass("NBTTagString");
        Class<?> nbtlist = NMSManager.getNMSClass("NBTTagList");
        Object id = null;
        Object val = NMSManager.invoke(nbtstring, "a", new Class[]{String.class}, nbtstring, encodedURL);
        if(Integer.parseInt(NMSManager.ServerVersion.split("_")[1])<16) {//1.15-
            id = NMSManager.invoke(nbtstring, "a", new Class[]{String.class}, nbtstring, UUID.randomUUID().toString());
        }
        Object compoundSO = null;
        Object compoundProp = null;
        Object compound0 = null;
        Object textures = null;
        try{
            if(id==null){//rentre uniquement si server version >= 1.16
                id = NMSManager.getNMSClass("NBTTagIntArray").getConstructor(List.class).newInstance(Arrays.asList(0,0,0,0));
            }
            textures = nbtlist.getConstructor().newInstance();
            compoundSO = NMSManager.getNMSClass("NBTTagCompound").getConstructor().newInstance();
            compoundProp = compoundSO.getClass().getConstructor().newInstance();
            compound0 = compoundSO.getClass().getConstructor().newInstance();
        }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
            System.err.println("Can't get a new instance for NBTTagCompund or NBTTagList.");
            e.printStackTrace();
        }
        if(compound0!=null){
            NMSManager.invoke(compound0.getClass(), "set", new Class[]{String.class, nbtbase}, compound0, "Value", val);
        }
        if(textures!=null){
            NMSManager.invoke(textures.getClass(), "b", new Class[]{int.class, nbtbase}, textures, 0, compound0);
        }
        if(compoundProp!=null){
            NMSManager.invoke(compoundProp.getClass(), "set", new Class[]{String.class, nbtbase}, compoundProp, "textures", textures);
        }
        if(compoundSO!=null){
            NMSManager.invoke(compoundSO.getClass(), "set", new Class[]{String.class, nbtbase}, compoundSO, "Id", id);
            NMSManager.invoke(compoundSO.getClass(), "set", new Class[]{String.class, nbtbase}, compoundSO, "Properties", compoundProp);
        }
        NMSManager.invoke(tags.getClass(), "set", new Class[]{String.class, nbtbase}, tags, "SkullOwner", compoundSO);
        NMSManager.invoke(itemstack, "setTag", new Class[]{tags.getClass()}, nmsItem, tags);
        item = NMSManager.getBukkitItem(nmsItem);
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
