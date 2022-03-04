package ch.luca008.ChallengesManager.Required;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.InventoryCompletable;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.Comparators.ItemCountComparator;
import ch.luca008.Comparators.LongBufferComparator;
import ch.luca008.Items.Item;
import ch.luca008.Items.ItemBuilder;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.ItemUtils;
import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class Items implements Required{

    private List<Item> items;

    public Items(JSONObject json){
        ArrayList<Item> unsorted = new ArrayList<>();
        if(json.containsKey("Items")){
            JSONArray arr = (JSONArray) json.get("Items");
            for (Object o : arr) {
                JSONObject j = (JSONObject) o;
                unsorted.add(new Item(j));
            }
        }
        this.items = unsorted;
        Items.sort(this.items);
    }
    public Items(Item...items){
        this.items = new ArrayList<>();
        Collections.addAll(this.items, items);
        Items.sort(this.items);
    }

    public static void sort(List<Item> toSort){
        if(toSort!=null&&!toSort.isEmpty()){
            ArrayList<Item> sortWithIndex = new ArrayList<>();
            ArrayList<Item> sortWithCount = new ArrayList<>();
            for(Item i : toSort){
                if(i.getSortOrder()>=0){
                    sortWithIndex.add(i);
                }else{
                    sortWithCount.add(i);
                }
            }
            sortWithIndex.sort(Comparator.comparingInt(Item::getSortOrder));
            sortWithCount.sort(new ItemCountComparator());
            toSort.clear();
            toSort.addAll(sortWithIndex);
            toSort.addAll(sortWithCount);
        }
    }

    public List<Item> getItems(){
        return items;
    }

    /**
     * Transform a whole Item list to itemstack array. Including custom meta like skull with nullable player and a 7 size content array for required or reward inventories
     * @param items A list of items, for example a required items list or reward items list
     * @param player A potential player who will get this array (Skull or book beta for example)
     * @param page7 Used for "pages" of required and reward inventory. If page == 0 then method will return a 7 length array with index 0 to 6. -1 if you want the full array
     * @return A full array or 7 length array of the items specified and their respective counts.
     */
    public static ItemStack[] getItems(List<Item> items, @Nullable UniPlayer player, Integer page7){
        List<ItemStack> i = new ArrayList<>();
        for(Item item : items){
            i.addAll(Arrays.stream(item.getItem().toItemStacks(item.getCount(), player)).collect(Collectors.toList()));
        }
        ItemStack[] collection = i.toArray(new ItemStack[0]);
        if(page7!=null&&page7>=0){
            return ItemUtils.getItemstacks(collection, page7);
        }
        return collection;
    }

    public boolean containsItemMeta(){
        if(items==null)return false;
        for(Item i : items){
            if(i.getItem()!=null&&i.getItem().hasItemMeta())return true;
        }
        return false;
    }

    private long toLongBuffer(long current, Item i, Storage.ChallengeStorage storage){
        int count = getCount(current)+i.getCount()+(i.getIncrement()*storage.getDailyCompleted());
        int progress = (int)current;
        if(storage.hasCompletable()){
            progress += ((InventoryCompletable)storage.getCompletable()).getCompletableCount(i.getUuid());
        }
        return combine(count, progress);
    }

    private long combine(int count, int progress){
        return (long)count << 32 | progress & 0xFFFFFFFFL;
    }

    private int getCount(long buf){
        if(buf<=0)return 0;
        return (int)(buf >> 32);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray jarr = new JSONArray();
        for (Item item : items) {
            jarr.add(item.toJson());
        }
        json.put("Items",jarr);
        return json;
    }

    @Override
    public String toLore(Storage.ChallengeStorage storage, UniPlayer player) {
        String lore = "";
        Map<Material, Long> it = new LinkedHashMap<>();
        for(Item item : items){
            Material m = item.getItem().getMaterial();
            it.put(m, toLongBuffer(it.getOrDefault(m, 0L), item, storage));
        }
        List<Material> mats = it.entrySet().stream().sorted(Map.Entry.comparingByValue(new LongBufferComparator())).map(Map.Entry::getKey).collect(Collectors.toList());
        for(Material m : mats){
            int count = getCount(it.get(m));
            lore+="\n §f- §b"+count+" §a"+ StringUtils.enumName(m)+" §7- §9"+((int)it.get(m).longValue())+"/"+count;
        }
        return lore+"\n";
    }

    @Override
    public String toString(){
        String i = "";
        if(items!=null&&!items.isEmpty()){
            i="{";
            for (Item item : items) {
                i+=item.toString()+",";
            }
            i = i.substring(0,i.length()-1)+"}";
        }
        return "Items{"+(i.isEmpty()?"NULL":i)+"}";
    }

    @Override
    public RequiredType getType() {
        return RequiredType.Items;
    }

    private InventoryCompletable createNewCompletable() {
        List<UUID> u = new ArrayList<>();
        items.forEach(i->u.add(i.getUuid()));
        return new InventoryCompletable(u);
    }

    @Override
    public CompletableResult complete(Challenge c, UniPlayer p) {
        if(p.getIslandStorage().isPresent() && p.getPlayer().isPresent()){
            Storage.ChallengeStorage storage = p.getIslandStorage().get().getStorage(c.getUuid());
            InventoryCompletable completable = (InventoryCompletable) (storage.hasCompletable() ? storage.getCompletable() : storage.setCompletable(createNewCompletable()));
            Player player = p.getPlayer().get();
            boolean isCompleted = true;
            boolean hasProgressed = false;
            Map<Item, Integer> missings = new LinkedHashMap<>();
            for(Item item : items){
                int missing = (item.getCount()+(item.getIncrement()*storage.getDailyCompleted()))-completable.getCompletableCount(item.getUuid());
                for(int i=0;i<=35;i++){
                    if(missing<=0)break;
                    ItemStack current = player.getInventory().getItem(i);
                    if(item.getItem().isSimilar(current, p)){//p used for book meta {P} balise
                        hasProgressed = true;
                        int amount = current.getAmount();
                        if(amount<missing){
                            completable.addCompletableCount(item.getUuid(), amount);
                            missing-=amount;
                            player.getInventory().setItem(i, null);
                        }else{
                            completable.addCompletableCount(item.getUuid(), missing);
                            current.setAmount(amount-missing);
                            if(current.getAmount()<=0)player.getInventory().setItem(i,null);
                            missing=0;
                        }
                    }
                }
                if(missing>0){
                    missings.put(item, missing);
                    isCompleted = false;
                }
            }
            boolean finalIsCompleted = isCompleted;
            boolean finalHasProgressed = hasProgressed;
            return new CompletableResult() {
                @Override
                public boolean isCompleted() {
                    return finalIsCompleted;
                }

                @Override
                public boolean hasProgressed(){
                    return finalHasProgressed;
                }

                @Override
                public Object getMessage() {
                    if(!finalIsCompleted){
                        String miss = "";
                        for(Item item : missings.entrySet().stream().sorted(Map.Entry.comparingByValue((o1, o2) -> Integer.compare(o1,o2)*-1)).map(Map.Entry::getKey).collect(Collectors.toList())){
                            miss+=" §7- §a"+missings.get(item)+" §b"+StringUtils.enumName(item.getItem().getMaterial())+"\n";
                        }
                        String m;
                        if(finalHasProgressed)m = p.getMessage("Challenge-Completion-Progress", Map.entry("{0}",c.getName()))+"\n";
                        else {
                            m = p.getMessage("Challenge-Completion-Unable", Map.entry("{0}",c.getName()));
                        }
                        m += p.getMessage("Challenge-Completion-Items-None", Map.entry("{0}",miss));
                        return m;
                    }
                    return "";
                }

                @Override
                public UniPlayer getPlayer(){
                    return p;
                }

                @Override
                public Challenge getChallenge(){
                    return c;
                }
            };
        }
        return null;
    }

    public static class Item{
        UUID uuid;
        int count;
        int increment;
        int sortOrder = -1;
        ch.luca008.Items.Item item;
        public Item(ch.luca008.Items.Item item, int count, int increment, int sortOrder){
            this.uuid = UUID.randomUUID();
            this.count = count;
            this.increment = increment;
            this.item = item;
            this.sortOrder = sortOrder;
        }
        public Item(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Item")){
                item = ch.luca008.Items.Item.fromJson(((JSONObject)json.get("Item")).toJSONString());
            }else return;
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json, "Count");
            }else count = 64;
            if(json.containsKey("Increment")){
                increment = JsonUtils.getInt(json, "Increment");
            }else increment = 0;
            if(json.containsKey("SortOrder")){
                sortOrder = JsonUtils.getInt(json, "SortOrder");
            }
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            if(sortOrder>=0)j.put("SortOrder", sortOrder);
            if(item!=null){
                j.put("Item",item.toJson());
                j.put("Count",count);
                if(increment>0)j.put("Increment",increment);
            }
            return j;
        }
        public UUID getUuid(){
            return uuid;
        }
        @Override
        public String toString(){
            return "Item(super Items){UUID:"+uuid.toString()+",Item:"+item.toString()+",Count:"+count+",Increment:"+increment+",SortOrder:"+sortOrder+"}";
        }
        public ch.luca008.Items.Item getItem(){
            return item;
        }
        public int getCount(){
            return count;
        }
        public int getIncrement(){
            return increment;
        }
        public int getSortOrder(){
            return sortOrder;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item)) return false;
            Item item = (Item) o;
            return uuid.equals(item.uuid);
        }
        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }
    }

}
