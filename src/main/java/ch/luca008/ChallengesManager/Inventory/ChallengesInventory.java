package ch.luca008.ChallengesManager.Inventory;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.InventoryCompletable;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.Items;
import ch.luca008.ChallengesManager.Reward;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.ItemUtils;
import ch.luca008.Utils.SbItem;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;

public class ChallengesInventory {

    private Map<Integer, List<UUID>> categoriesOnPage;
    private Map<UUID, Map<Integer, List<UUID>>> challengesOnCategoryPage;

    public ChallengesInventory(){
        categoriesOnPage = new HashMap<>();
        challengesOnCategoryPage = new HashMap<>();
        for (Category category : Challenges.getManager().getCategories()) {
            List<UUID> categories = categoriesOnPage.getOrDefault(category.getPage(), new ArrayList<>());
            categories.add(category.getUuid());
            categoriesOnPage.put(category.getPage(), categories);
            HashMap<Integer, List<UUID>> challengesOnPage = new HashMap<>();
            for(Challenge challenge : Challenges.getManager().getIndex().getChallenges(category)){
                List<UUID> challenges = challengesOnPage.getOrDefault(challenge.getPage(), new ArrayList<>());
                challenges.add(challenge.getUuid());
                challengesOnPage.put(challenge.getPage(), challenges);
            }
            challengesOnCategoryPage.put(category.getUuid(), challengesOnPage);
        }
    }

    /**
     * Create the main menu inventory at the specified page.
     * <p>
     * Parent of {@link #asInventory(UniPlayer, Category, int)}
     * @param player The player who will get the inventory. Needed to check lang and unlocked categories
     * @param page The page to open. First one: 1
     * @return An inventory with all the categories at the specified page
     */
    public Inventory asInventory(UniPlayer player, int page){
        Optional<Storage> optStorage = player.getIslandStorage();
        if(optStorage.isPresent()){
            Storage storage = optStorage.get();
            Inventory inventory = Bukkit.createInventory(null, 54, player.getChallengeMessage("Inventory-Title"));
            Challenges.getManager().getAsyncManager().buildCategoriesItems(categoriesOnPage(page),storage,player).forEach(inventory::setItem);
            if(getMaxCategoriesPages()>1){
                Utils.addChangePage(inventory, page, player, "Main");
            }
            Utils.round(inventory,null);
            inventory.setItem(49, SpigotApi.getNBTTagApi().getNBT(inventory.getItem(49).clone()).setTag("Challenges","value").setTag("Page",page).getBukkitItem());
            return inventory;
        }
        return Bukkit.createInventory(null, 9);
    }

    /**
     * Create the specified category's inventory at the specified page.
     * <p>
     * Parent of {@link #asInventory(UniPlayer, Challenge, int, int)}
     * <p>
     * Child of {@link #asInventory(UniPlayer, int)}
     * @param player The player who will get the inventory. Needed to check lang and unlocked challenges into the category
     * @param category The category to open
     * @param page The page to open. First one: 1
     * @return An inventory with all the challenges at the specified page for this category
     */
    public Inventory asInventory(UniPlayer player, Category category, int page){
        Optional<Storage> optStorage = player.getIslandStorage();
        if(optStorage.isPresent()){
            Storage storage = optStorage.get();
            Inventory inventory = Bukkit.createInventory(null, 54, player.getChallengeMessage("Category-Name-Inventory-Title",Map.entry("{0}",category.getName())));
            Challenges.getManager().getAsyncManager().buildCategoryItems(challengesOnPage(category,page),storage,player).forEach(inventory::setItem);
            if(getMaxCategoryPages(category)>1){
                Utils.addChangePage(inventory, page, player, "Category");
            }
            inventory.setItem(53, new SbItem(new ItemBuilder().setMaterial(Material.OAK_DOOR).setName(player.getChallengeMessage("Challenge-Item-Door")).setUid("Door").createItem()).toItemStack(1));
            Utils.round(inventory, new SbItem(new ItemBuilder().setMaterial(category.getColor()).setName("-").createItem()));
            inventory.setItem(49, SpigotApi.getNBTTagApi().getNBT(inventory.getItem(49).clone()).setTag("Category",category.getUuid().toString()).setTag("Page",page).getBukkitItem());
            return inventory;
        }
        return Bukkit.createInventory(null, 9);
    }

    /**
     * Create the specified challenge's required/reward inventory at the specified pages.
     * <p>
     * Parent of {@link #asInventory(UniPlayer, UUID, int, int, ShapedRecipe)}
     * <p>
     * Child of {@link #asInventory(UniPlayer, Category, int)}
     * @param player The player who will get the inventory. Needed to check lang
     * @param c The challenge
     * @param reqPage The required items page (first line)
     * @param rewPage The reward items page (second line)
     * @return An inventory with all the required and reward items at their specified page
     */
    public Inventory asInventory(UniPlayer player, Challenge c, int reqPage, int rewPage){
        if(reqPage<0)reqPage=0;
        if(rewPage<0)rewPage=0;
        Category parent = c.getCategory();
        SbItem roundItem = new SbItem(new ItemBuilder().setMaterial(parent==null?Material.BLACK_STAINED_GLASS_PANE:parent.getColor()).setName("-").createItem());
        Inventory inventory = Utils.round(Bukkit.createInventory(null, 45, "§9"+c.getName()), roundItem);
        ItemStack roundItemstack = roundItem.toItemStack(1);
        inventory.setItem(19, roundItemstack);
        inventory.setItem(20, roundItemstack);
        inventory.setItem(24, roundItemstack);
        inventory.setItem(25, roundItemstack);
        int reqMax = 1, rewMax = 1;
        if(c.getRequired() instanceof Items){
            Items req = (Items)c.getRequired();
            ItemStack[] items;
            Storage sto = player.getIslandStorage().orElse(null);
            if(sto!=null&&sto.getStorage(c.getUuid()).hasCompletable()){
                Storage.ChallengeStorage challengeStorage = sto.getStorage(c.getUuid());
                InventoryCompletable completable = (InventoryCompletable) challengeStorage.getCompletable();
                List<Items.Item> missing = new ArrayList<>();
                for(Items.Item i : req.getItems()){
                    int requiredCount = i.getCount()+(i.getIncrement()*challengeStorage.getDailyCompleted());
                    int progressCount = completable.getCompletableCount(i.getUuid());
                    if(progressCount<requiredCount){
                        missing.add(new Items.Item(i.getItem(), requiredCount-progressCount, 0, 0));
                    }
                }
                missing.sort((o1, o2) -> Integer.compare(o1.getCount(),o2.getCount())*-1);
                items = Items.getItems(missing, player, -1);
            }else{
                List<Items.Item> itemReq = req.getItems();
                items = Items.getItems(itemReq, player, -1);
            }
            reqMax = (items.length+6)/7;
            items = ItemUtils.getItemstacks(items, reqPage);
            for(int i = 0; i<(Math.min(items.length, 7)); i++){
                inventory.setItem(i+10,items[i]);
            }
        }
        if(c.getReward()!=null){
            Optional<Storage> optStr = player.getIslandStorage();
            boolean first = optStr.isEmpty() || optStr.get().getStorage(c.getUuid()).getTotalCompleted() == 0;
            Reward._Reward rew = c.getReward().getReward(first);
            if(rew!=null){
                List<Items.Item> itemRew = rew.getItems();
                if(itemRew!=null&&!itemRew.isEmpty()){
                    ItemStack[] items = Items.getItems(itemRew, player, -1);
                    rewMax = (items.length+6)/7;
                    items = ItemUtils.getItemstacks(items, rewPage);
                    for(int i = 0; i<(Math.min(items.length, 7)); i++){
                        inventory.setItem(i+28,items[i]);
                    }
                }
            }
        }
        NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(new ItemBuilder().setMaterial(Material.OAK_SIGN).setName("§a▲"+player.getChallengeMessage("Challenge-Item-Required")+" - "+player.getChallengeMessage("Challenge-Item-Reward")+"§a▼").createItem().toItemStack(1));
        inventory.setItem(22, nbt.setTag("Challenge",c.getUuid().toString()).setTag("ReqPage",reqPage+"").setTag("ReqMax",reqMax+"").setTag("RewPage",rewPage+"").setTag("RewMax",rewMax+"").getBukkitItem());
        ItemStack[] pages = Utils.getSampleInventoryPages(player, reqPage, reqMax, rewPage, rewMax, roundItemstack);
        inventory.setItem(21,pages[0]);
        inventory.setItem(23,pages[1]);
        inventory.setItem(44,new ItemBuilder().setMaterial(Material.OAK_DOOR).setName(player.getChallengeMessage("Challenge-Item-Door")).setUid("Door").createItem().toItemStack(1));
        return inventory;
    }

    /**
     * Create an inventory with the craft of the specified recipe. Used for the sample inventory.
     * <p>
     * Child of {@link #asInventory(UniPlayer, Challenge, int, int)}
     * @param player The player who will get the inventory. Needed to check lang
     * @param challenge The parent challenge of craft item. Needed to create the back door
     * @param reqPage The required items page (first line). Needed to get back on the current page with the back door
     * @param rewPage The reward items page (second line). Needed to get back on the current page with the back door
     * @param recipe The recipe to display into the inventory
     * @return An inventory which display the craft for the specified recipe
     */
    public Inventory asInventory(UniPlayer player, UUID challenge, int reqPage, int rewPage, ShapedRecipe recipe){
        Inventory wb = Bukkit.createInventory(null, 27, "§9"+ StringUtils.enumName(recipe.getResult().getType()));
        wb.setItem(26, new ItemBuilder().setMaterial(Material.OAK_DOOR).setName(player.getChallengeMessage("Challenge-Item-Door")).setUid("Door_"+challenge.toString()).createItem().toItemStack(1));
        wb.setItem(12, new ItemBuilder().setMaterial(Material.ARROW).setCustomData(1).setName(player.getChallengeMessage("Inventory-Craft-Result")).createItem().toItemStack(1));
        wb.setItem(13, SpigotApi.getNBTTagApi().getNBT(recipe.getResult()).setTag("Req",reqPage+"").setTag("Rew",rewPage+"").getBukkitItem());
        ItemStack empty = new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName("§0-").createItem().toItemStack(1);
        for(int i=3;i<wb.getSize()-1;i++){
            wb.setItem(i,empty);
            if(i==8)i+=5;
            if(i==17)i+=3;
        }
        String[] shape = recipe.getShape();
        int i = 0;
        for(int s=0;s<3;s++){
            if(s<shape.length){
                char[] ids = shape[s].toCharArray();
                for(int c=0;c<3;c++){
                    if(c<ids.length){
                        char id = ids[c];
                        ItemStack item = recipe.getIngredientMap().get(id);
                        if(item!=null&&item.getType()!=Material.AIR){
                            wb.setItem(i,item.clone());
                        }
                    }
                    i++;
                }
                if(i==3||i==12)i+=6;
            }
        }
        return wb;
    }

    public List<Category> categoriesOnPage(int page){
        Manager m = Challenges.getManager();
        List<Category> categories = new ArrayList<>();
        for(UUID u : categoriesOnPage.getOrDefault(page, new ArrayList<>())){
            m.retrieveCategoryByUUID(u).ifPresent(categories::add);
        }
        return categories;
    }

    public int getMaxCategoriesPages(){
        return Collections.max(categoriesOnPage.keySet());
    }

    public boolean doCategoriesPageExists(int page){
        return page>0&&page<=getMaxCategoriesPages();
    }

    public List<Challenge> challengesOnPage(Category category, int page){
        Manager m = Challenges.getManager();
        if(challengesOnCategoryPage.containsKey(category.getUuid())){
            return m.retrieveChallengesByUUID(challengesOnCategoryPage.get(category.getUuid()).getOrDefault(page, new ArrayList<>()).toArray(new UUID[0]));
        }
        return new ArrayList<>();
    }

    public int getMaxCategoryPages(Category category){
        if(!challengesOnCategoryPage.containsKey(category.getUuid()))return 0;
        if(challengesOnCategoryPage.get(category.getUuid()).isEmpty())return 1; //categorie vide = 1 page max
        return Collections.max(challengesOnCategoryPage.get(category.getUuid()).keySet());
    }

    public boolean doCategoryPageExists(Category category, int page){
        return page>0&&page<=getMaxCategoryPages(category);
    }
}
