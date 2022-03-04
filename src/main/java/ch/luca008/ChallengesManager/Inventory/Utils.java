package ch.luca008.ChallengesManager.Inventory;

import ch.luca008.Challenges;
import ch.luca008.Items.Item;
import ch.luca008.Items.ItemBuilder;
import ch.luca008.UniPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static int getCategoryRealSlot(int slot) {
        if (slot >= 1 && slot < 8)
            return slot + 9;
        if (slot >= 8 && slot < 15)
            return slot + 11;
        if (slot >= 15 && slot < 22)
            return slot + 13;
        if (slot >= 22 && slot < 29)
            return slot + 15;
        return slot;
    }

    private static void round(Inventory inv, ItemStack item){
        int count = inv.getSize();
        if(count==27||count==36||count==45||count==54){
            for(int i=0;i<9;i++) {
                if(inv.getItem(i)==null) {
                    inv.setItem(i, item);
                }
            }
            for(int i=inv.getSize()-9;i<inv.getSize();i++) {
                if(inv.getItem(i)==null) {
                    inv.setItem(i, item);
                }
            }
            for(int i=9;i<inv.getSize();i+=9) {
                if(i<count){
                    if(inv.getItem(i)==null) {
                        inv.setItem(i, item);
                    }
                    if(inv.getItem(i+8)==null) {
                        inv.setItem(i+8, item);
                    }
                }
            }
        }
    }

    public static Inventory round(Inventory inv, @Nullable Item item){
        if(item==null)item = Challenges.getGlobalConfig().getChallengesMenuIcon();
        round(inv, item.toItemStack(1));
        return inv;
    }

    /**
     * Return the same inventory with 2 paper. One with Next and the other with Previous to move from page to page into this inventory.
     * <p>
     * "inventoryType : Challenge" isn't used here after all. See {@link #getSampleInventoryPages(UniPlayer, int, int, int, int, ItemStack)}
     * @param inv the inventory who requires the two items
     * @param pageCurrent the current inventory page (needed to add xxx_Page_N° into the itemstack's nbt)
     * @param player the player who will have the inventory (needed to get his lang)
     * @param inventoryType Main - Category - Challenge (needed to specify which inventory page to open when items clicked)
     * @return Return the same inventory
     */
    public static Inventory addChangePage(Inventory inv, int pageCurrent, UniPlayer player, String inventoryType){
        Item next = new ItemBuilder().
                setMaterial(Material.ARROW).setCustomData(1).
                setName(player.getChallengeMessage("Inventory-Next-Page")).setUid(inventoryType+"_Page_"+(pageCurrent+1)).
                createItem();
        Item prev = new ItemBuilder().
                setMaterial(Material.ARROW).setCustomData(2).
                setName(player.getChallengeMessage("Inventory-Prev-Page")).setUid(inventoryType+"_Page_"+(pageCurrent-1)).
                createItem();
        inv.setItem(48,prev.toItemStack(1));
        inv.setItem(50,next.toItemStack(1));
        return inv;
    }

    /**
     * Create the 2 itemstack for pos 21 & 23 of the special items inventory.
     * @param player the player who gets the inventory
     * @param reqPage the current open page for required
     * @param reqMax this challenge's required max page (1 page = 7 items. So items/7 rounded up)
     * @param rewPage the current open page for reward
     * @param rewMax this challenge's reward max page (1 page = 7 items. So items/7 rounded up)
     * @param defaultItem the default item that will be displayed if req&rewMax < 1
     * @return a two length array with prev and next pages items.
     */
    public static ItemStack[] getSampleInventoryPages(UniPlayer player, int reqPage, int reqMax, int rewPage, int rewMax, ItemStack defaultItem){
        if(reqMax<=1&&rewMax<=1){
            return new ItemStack[]{defaultItem,defaultItem};
        }
        String right = " "+player.getChallengeMessage("Inventory-Page-Right-Click");
        String left = " "+player.getChallengeMessage("Inventory-Page-Left-Click");
        List<String> loreNext = new ArrayList<>();
        if(reqPage+1<reqMax) loreNext.add(player.getChallengeMessage("Challenge-Item-Required")+left);
        if(rewPage+1<rewMax) loreNext.add(player.getChallengeMessage("Challenge-Item-Reward")+right);
        Item next = new ItemBuilder().
                setMaterial(Material.ARROW).setCustomData(1).setUid("Next")
                .setName(player.getChallengeMessage("Inventory-Next-Page"))
                .setLore(loreNext)
                .createItem();

        List<String> lorePrev = new ArrayList<>();
        if(reqPage>0) lorePrev.add(player.getChallengeMessage("Challenge-Item-Required")+left);
        if(rewPage>0) lorePrev.add(player.getChallengeMessage("Challenge-Item-Reward")+right);
        Item prev = new ItemBuilder().
                setMaterial(Material.ARROW).setCustomData(2).setUid("Prev")
                .setName(player.getChallengeMessage("Inventory-Prev-Page"))
                .setLore(lorePrev)
                .createItem();
        return new ItemStack[]{lorePrev.isEmpty()?defaultItem:prev.toItemStack(1),loreNext.isEmpty()?defaultItem:next.toItemStack(1)};
    }
}
