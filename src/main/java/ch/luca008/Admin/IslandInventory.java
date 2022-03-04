package ch.luca008.Admin;

import NBT.NBTTag;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.Inventory.Utils;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.Items.Item;
import ch.luca008.Items.ItemBuilder;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IslandInventory {

    private UUID island;
    public UUID getIsland(){
        return island;
    }
    private Storage storage;
    private Player admin;
    private OfflinePlayer target;
    private Category currentlyWatching;
    private int currentPage;
    private boolean forceClose = true; //si true alors la session se détruit lorsque l'inventaire est fermé par le joueur
    public boolean doDestroyOnClose(){
        return forceClose;
    }
    public void setDestroyOnClose(boolean forceClose){
        this.forceClose = forceClose;
    }

    public IslandInventory(Player admin, UUID island, OfflinePlayer target){
        this.island = island;
        this.admin = admin;
        this.target = target;
        this.currentlyWatching = null;
        this.currentPage = 1;
        Optional<Storage> optStorage = Challenges.getManager().retrieveStorageByUUID(island);
        if(optStorage.isPresent()){//fonctionne ps avec le orElse
            storage = optStorage.get();
        }else storage = Challenges.getManager().loadStorage(island);
        display();
    }

    public boolean isUsable(){
        return admin!=null&&storage!=null;
    }

    public void unload(){
        Challenges.getManager().unloadStorageSafe(island);
    }

    private Challenge getChallenge(UUID challenge){
        return Challenges.getManager().retrieveChallengeByUUID(challenge).orElse(null);
    }

    private Category getCategory(UUID category){
        return Challenges.getManager().retrieveCategoryByUUID(category).orElse(null);
    }

    public Storage getStorage(){
        return storage;
    }

    public OfflinePlayer getTarget(){
        return target;
    }

    public void display(){
        update(currentlyWatching, currentPage);
    }
    public void displayNext(){
        currentPage++;
        display();
    }
    public void displayPrev(){
        if(currentPage>1){
            currentPage--;
            display();
        }
    }
    public void display(Category c, int page){
        if(c==null&&currentlyWatching!=null&&page==-1){
            this.currentPage = currentlyWatching.getPage();
            this.currentlyWatching = null;
            display();
            return;
        }
        if(page>=1){
            this.currentlyWatching = c;
            this.currentPage = page;
            display();
        }
    }

    private void update(Category category, int page){
        if(!admin.getOpenInventory().getTitle().startsWith("§cÎle de §9")){
            admin.openInventory(Bukkit.createInventory(null, 54, "§cÎle de §9"+target.getName()));
        }
        Inventory inv = admin.getOpenInventory().getTopInventory();
        inv.clear();
        inv.setItem(48, new ItemBuilder().setMaterial(Material.ARROW).setName("§aPrécédent").setCustomData(2).createItem().toItemStack(1));
        inv.setItem(50, new ItemBuilder().setMaterial(Material.ARROW).setName("§aSuivant").setCustomData(1).createItem().toItemStack(1));
        if(category==null){
            List<Category> categoryList = Challenges.getManager().getCategories();
            int unlocked = 0;
            for(Category c : categoryList){
                boolean isUnlocked = storage.isUnlocked(c).size()==0;
                if(isUnlocked)unlocked++;
                if(c.getPage()==page){
                    ItemBuilder itemB = new ItemBuilder();
                    if (isUnlocked) {
                        Item icon = c.getIcon();
                        itemB.setMaterial(icon.getMaterial());
                        if(icon.getCustomData()>0){
                           itemB.setCustomData(icon.getCustomData());
                        }
                        if(icon.hasMeta()){
                            itemB.setMeta(icon.getMeta());
                        }
                        itemB.setLore(StringUtils.asLore("§4Clic droit pour reset la catégorie"));
                    }else{
                        itemB.setMaterial(Material.RED_STAINED_GLASS_PANE);
                    }
                    itemB.setName("§a"+c.getName());
                    itemB.setUid("Category_"+c.getUuid().toString());
                    inv.setItem(Utils.getCategoryRealSlot(c.getSlot()), new NBTTag(itemB.createItem().toItemStack(1)).setTag("HideFlags",63).getBukkitItem());
                }
            }
            inv.setItem(4, new ItemBuilder().setMaterial(Material.OAK_SIGN).setName("§aGérer les catégories").setLore(StringUtils.asLore("§eCatégories débloquées: §6"+unlocked+"§e/§6"+categoryList.size()+"\n§eNether: §6"+(storage.canAccess(Storage.AccessType.NETHER)?"Oui":"Non")+"\n§eEnd: §6"+(storage.canAccess(Storage.AccessType.END)?"Oui":"Non")+"\n§cClic gauche pour modifier les accès aux mondes\n§4§lClic droit pour tout réinitialiser")).setUid("MainManager").createItem().toItemStack(1));
            Utils.round(inv, new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).createItem());
        }else{
            List<Challenge> challengesList = Challenges.getManager().getIndex().getChallenges(category);
            Optional<Challenge> activeStatChallengeOpt = storage.getActiveStatChallenge();
            int unlocked = 0;
            for(Challenge cha : challengesList){
                boolean isStatActive = activeStatChallengeOpt.isPresent()&&activeStatChallengeOpt.get().equals(cha);
                boolean isUnlocked = storage.isUnlocked(cha).size()==0;
                if(isUnlocked)unlocked++;
                if(cha.getPage()==page){
                    ItemBuilder itemB = new ItemBuilder();
                    if(isUnlocked){
                        Item icon = cha.getIcon();
                        itemB.setMaterial(icon.getMaterial());
                        if(icon.getCustomData()>0){
                            itemB.setCustomData(icon.getCustomData());
                        }
                        if(icon.hasMeta()){
                            itemB.setMeta(icon.getMeta());
                        }
                        Storage.ChallengeStorage cstr = storage.getStorage(cha.getUuid());
                        String lore = (isStatActive?"§eCe challenge est actuellement actif sur l'île\n§4S'il est reset, le challenge ne sera plus actif.\n":"")+"\n§eComplété §6"+cstr.getTotalCompleted()+"§e/§6"+(cha.getRedoneLimit()==-1?"∞":cha.getRedoneLimit())+" §efois"+"\n§eComplété §6"+cstr.getDailyCompleted()+" §efois aujourd'hui\n\n§cClic gauche pour modifier le nombre de fois complété\n§4Clic droit pour reset le challenge";
                        itemB.setLore(StringUtils.asLore(lore));
                        if(cstr.getTotalCompleted()>0){
                            itemB.setGlowing(true);
                        }
                    }else{
                        itemB.setMaterial(Material.RED_STAINED_GLASS_PANE);
                    }
                    itemB.setName("§a"+cha.getName());
                    itemB.setUid("Challenge_"+cha.getUuid().toString());
                    inv.setItem(Utils.getCategoryRealSlot(cha.getSlot()), new NBTTag(itemB.createItem().toItemStack(1)).setTag("HideFlags",63).getBukkitItem());
                }
            }
            inv.setItem(4, new ItemBuilder().setMaterial(category.getIcon().getMaterial()).setName("§aGérer les challenges").setLore(StringUtils.asLore("§eChallenges débloqués: §6"+unlocked+"§e/§6"+challengesList.size())).createItem().toItemStack(1));
            inv.setItem(53, new ItemBuilder().setMaterial(Material.OAK_DOOR).setName("§cRetour").createItem().toItemStack(1));
            Utils.round(inv, new ItemBuilder().setMaterial(category.getColor()).createItem());
        }
    }
}
