package ch.luca008.Utils;

import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.UniPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
