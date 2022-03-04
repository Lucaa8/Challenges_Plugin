package ch.luca008.Utils;

import NBT.NBTTag;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Categories.CategoryBuilder;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.Challenges.ChallengeBuilder;
import ch.luca008.ChallengesManager.Inventory.Utils;
import ch.luca008.ChallengesManager.IslandStorage.InventoryCompletable;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.Items;
import ch.luca008.Items.Item;
import ch.luca008.Items.Meta.Skull;
import ch.luca008.UniPlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AsyncManager {

    private Manager manager;
    private ExecutorService executor;

    public AsyncManager(Manager manager, int poolSize){
        this.manager = manager;
        if(poolSize>0){
            executor = Executors.newFixedThreadPool(poolSize, Executors.privilegedThreadFactory());
        }else executor = Executors.newCachedThreadPool(Executors.privilegedThreadFactory());
    }

    public void shutdown(){
        if(executor==null)return;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            executor.shutdownNow();
        }
    }

    //INDIVIDUAL ASYNC TASKS (LOAD ONE CHALLENGE/CATEGORY FROM FILE, ETC..), RETURN COMPLETABLEFUTURE

    private CompletableFuture<Void> unloadUniPlayer(UniPlayer player){
        return CompletableFuture.runAsync(player::unload, executor);
    }

    private CompletableFuture<Category> loadCategory(File f){
        return CompletableFuture.supplyAsync(()-> new CategoryBuilder(JsonUtils.readFile(f)).createCategory(), executor);
    }

    private CompletableFuture<Challenge> loadChallenge(File f){
        return CompletableFuture.supplyAsync(()->new ChallengeBuilder(JsonUtils.readFile(f)).createChallenge(), executor);
    }

    private CompletableFuture<Void> unloadCategory(Category c){
        return CompletableFuture.runAsync(c::unload, executor);
    }

    private CompletableFuture<Void> unloadChallenge(Challenge c){
        return CompletableFuture.runAsync(c::unload, executor);
    }

    private CompletableFuture<Void> unloadStorage(Storage storage){
        return CompletableFuture.runAsync(storage::unload, executor);
    }

    private CompletableFuture<Map.Entry<Integer,ItemStack>> buildItem(Storage storage, Category c, UniPlayer player){
        return CompletableFuture.supplyAsync(()-> Map.entry(c.getSlot(),c.toItem(storage,player)),executor);
    }

    private CompletableFuture<Map.Entry<Integer,ItemStack>> buildItem(Storage storage, Challenge c, UniPlayer player){
        return CompletableFuture.supplyAsync(()-> {
            Item i = c.toItem(storage, player);
            i.hideAttributes();
            if(storage.getStorage(c.getUuid()).getTotalCompleted()>0){
                i.glow();
            }
            ItemStack is = i.toItemStack(1);
            //adds a skullowner if item's type = PLAYER_HEAD
            if(i.getMeta() instanceof Skull){
                Skull s = (Skull) i.getMeta();
                if(s.getOwningType()==Skull.SkullOwnerType.PLAYER){
                    is = s.applyOwner(is, player.getOfflinePlayer().getUniqueId());
                }
            }
            return Map.entry(c.getSlot(),ItemUtils.removeNamedColor(new NBTTag(is).setTag("HideFlags",127).getBukkitItem(),c.getName()));
        },executor);
    }

    private CompletableFuture<Void> resetDailyStorage(UUID island){
        return CompletableFuture.runAsync(()->{
            Storage str = manager.retrieveStorageByUUID(island).orElse(manager.loadStorage(island));
            for(Map.Entry<UUID,Storage.ChallengeStorage> challenge : str.getStorages().entrySet()){
                Storage.ChallengeStorage chastr = challenge.getValue();
                chastr.setDailyCompleted(0);
                if(chastr.hasCompletable()){
                    if(chastr.getCompletable() instanceof InventoryCompletable){
                        Optional<Challenge> optChallenge = manager.retrieveChallengeByUUID(challenge.getKey());
                        if(optChallenge.isPresent()){
                            Items required = (Items)optChallenge.get().getRequired();
                            InventoryCompletable c = (InventoryCompletable) chastr.getCompletable();
                            for(Items.Item i : required.getItems()){
                                if(i.getIncrement()>0)c.setCompletableCount(i.getUuid(), 0);
                            }
                        }
                    }
                }
            }
            manager.unloadStorageSafe(str.getIsland());
        },executor);
    }

    @Deprecated
    public CompletableFuture<Optional<Challenge>> retrieveChallengeByUUID(UUID u){
        return CompletableFuture.supplyAsync(()->{
            return manager.retrieveChallengeByUUID(u);
        });
    } 
    
    //COMPLETE ALL INDIVIDUAL TASKS ASYNC THEN WAIT TO RETURN THE FINAL RESULT

    private CompletableFuture<?> join(List<CompletableFuture<?>> asyncTaks){
        return CompletableFuture.allOf(asyncTaks.toArray(new CompletableFuture[0])).thenApply(f->asyncTaks.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    public List<Category> loadCategories() {
        long start = System.currentTimeMillis();
        List<CompletableFuture<Category>> completableFutures = Arrays.stream(Manager.categoriesBaseDirectory.listFiles()).filter(f->!f.getName().equals("index.json")).map(this::loadCategory).collect(Collectors.toList());
        CompletableFuture<List<Category>> allCompleted = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenApply(f-> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try{
            List<Category> categories = allCompleted.get();
            System.out.println("Loaded " + categories.size() + " categories in " + (System.currentTimeMillis()-start) + " milliseconds.");
            return categories;
        }catch(ExecutionException | InterruptedException e){
            System.err.println("Can't load async categories.");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Challenge> loadChallenges(){
        long start = System.currentTimeMillis();
        List<CompletableFuture<Challenge>> completableFutures = Arrays.stream(Manager.challengesBaseDirectory.listFiles()).filter(f->!f.getName().equals("index.json")).map(this::loadChallenge).collect(Collectors.toList());
        CompletableFuture<List<Challenge>> allCompleted = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenApply(f-> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try{
            List<Challenge> challenges = allCompleted.get();
            System.out.println("Loaded " + challenges.size() + " challenges in " + (System.currentTimeMillis()-start) + " milliseconds.");
            return challenges;
        }catch(ExecutionException | InterruptedException e){
            System.err.println("Can't load async challenges.");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Map<Integer,ItemStack> buildItems(ArrayList<Map.Entry<Integer,ItemStack>> list){
        Map<Integer,ItemStack> items = new HashMap<>();
        for(Map.Entry<Integer,ItemStack> entry : list){
            items.put(Utils.getCategoryRealSlot(entry.getKey()), entry.getValue());
        }
        return items;
    }

    public Map<Integer,ItemStack> buildCategoriesItems(List<Category> categories, Storage storage, UniPlayer player){
        try{
            return buildItems((ArrayList<Map.Entry<Integer,ItemStack>>) join(categories.stream().map(c->buildItem(storage,c,player)).collect(Collectors.toList())).get());
        }catch(ExecutionException | InterruptedException e){
            System.err.println("Can't build categories items async.");
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public Map<Integer,ItemStack> buildCategoryItems(List<Challenge> challenges, Storage storage, UniPlayer player){
        try{
            return buildItems((ArrayList<Map.Entry<Integer,ItemStack>>) join(challenges.stream().map(c->buildItem(storage,c,player)).collect(Collectors.toList())).get());
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Can't build category items async.");
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public void resetDailyStorages(List<UUID> storages){
        try{
            join(storages.stream().map(this::resetDailyStorage).collect(Collectors.toList())).get();
        }catch(ExecutionException | InterruptedException e){
            System.err.println("Can't reset daily storages async.");
            e.printStackTrace();
        }
    }

    public void unload(){
        long start = System.currentTimeMillis();
        try{
            join(manager.getLoadedPlayers().stream().map(this::unloadUniPlayer).collect(Collectors.toList())).get();
            join(manager.getLoadedStorages().stream().map(this::unloadStorage).collect(Collectors.toList())).get();
            if(manager.getStats()!=null)manager.getStats().unload();
            if(manager.getIndex()!=null)manager.getIndex().unload();
            join(manager.getChallenges().stream().map(this::unloadChallenge).collect(Collectors.toList())).get();
            join(manager.getCategories().stream().map(this::unloadCategory).collect(Collectors.toList())).get();
            System.out.println("Unloaded players, storages, categories and challenges in " + (System.currentTimeMillis()-start) + " milliseconds.");
        }catch(ExecutionException | InterruptedException e){
            System.err.println("Can't unload async challenges or categories.");
            e.printStackTrace();
        }
    }
}
