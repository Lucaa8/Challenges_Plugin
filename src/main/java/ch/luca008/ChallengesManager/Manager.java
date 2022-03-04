package ch.luca008.ChallengesManager;

import ch.luca008.Admin.Editor.Sockets.Packets.IPacket;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Categories.CategoryBuilder;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.Challenges.ChallengeBuilder;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.AsyncManager;
import ch.luca008.Utils.JsonUtils;
import com.songoda.skyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Manager {

    private final AsyncManager asyncTasks;
    public AsyncManager getAsyncManager(){return asyncTasks;}
    @Nonnull
    public static File indexFile = new File(Challenges.Main.getDataFolder(), "index.json");
    @Nonnull
    public static File categoriesBaseDirectory = new File(Challenges.Main.getDataFolder(), "Categories");
    @Nonnull
    public static File challengesBaseDirectory = new File(Challenges.Main.getDataFolder(), "Challenges");
    @Nonnull
    public static File islandsBaseDirectory = new File(Challenges.Main.getDataFolder(), "Islands");
    @Nonnull
    public static File playersBaseDirectory = new File(Challenges.Main.getDataFolder(), "Players");
    @Nonnull
    public static File globalConfigFile = new File(Challenges.Main.getDataFolder(), "config.json");
    @Nonnull
    public static File resetDailyFile = new File(Challenges.Main.getDataFolder(), "reset.json");
    @Nonnull
    public static File challengesStatisticsFile = new File(Challenges.Main.getDataFolder(), "stats.json");
    private List<Category> categories = new ArrayList<>();
    private List<Challenge> challenges = new ArrayList<>();
    private List<Storage> loadedStorages = new ArrayList<>();
    private List<UniPlayer> players = new ArrayList<>();
    private Index index;
    private Statistics stats;

    public Manager(){
        asyncTasks = new AsyncManager(this, 0);
    }

    public List<Category> getCategories(){
        return categories;
    }
    public List<Challenge> getChallenges(){
        return challenges;
    }
    public Index getIndex(){
        return index;
    }
    public Statistics getStats(){
        return stats;
    }

    public void load(){
        categories = asyncTasks.loadCategories();
        challenges = asyncTasks.loadChallenges();
        loadedStorages = new ArrayList<>();
        index = new Index(JsonUtils.readFile(indexFile));
        players = new ArrayList<>();
        stats = new Statistics(JsonUtils.readFile(challengesStatisticsFile));
    }

    public void unload(){
        asyncTasks.unload();
        index = null;
        categories = null;
        challenges = null;
        loadedStorages = null;
        players = null;
    }

    public void addOrReplaceCategory(JSONObject json){
        UUID u = UUID.fromString((String)json.get("uuid"));
        Category c = retrieveCategoryByUUID(u).orElse(null);
        if(c==null){
            c = new CategoryBuilder(json).createCategory();
            c.asChanged = true;
            categories.add(c);
        }else{
            c.flash(json);
        }
        getIndex().getRequiredChallenges(c).clear();
        if(json.containsKey("requiredChallenges")){
            JSONArray jarr = (JSONArray) json.get("requiredChallenges");
            for(Object o : jarr){
                Optional<Challenge> optReqCha = retrieveChallengeByUUID(UUID.fromString((String)o));
                if(optReqCha.isPresent()){
                    getIndex().addRequired(IPacket.Target.CATEGORY, c.getUuid(), optReqCha.get());
                }
            }
        }
        //Needed to update categories and challenges in inventory
        Challenges.updateInventoryManager();
    }

    public void addOrReplaceChallenge(JSONObject json){
        UUID u = UUID.fromString((String)json.get("uuid"));
        Challenge c = retrieveChallengeByUUID(u).orElse(null);
        if(c==null){
            c = new ChallengeBuilder(json).createChallenge();
            c.asChanged = true;
            challenges.add(c);
            if(c.getCategory()!=null){
                getIndex().addChallenge(c.getCategory(), c);
            }
        }else{
            c.flash(json);
        }
        getIndex().getRequiredChallenges(c).clear();
        if(json.containsKey("requiredChallenges")){
            JSONArray jarr = (JSONArray) json.get("requiredChallenges");
            for(Object o : jarr){
                Optional<Challenge> optReqCha = retrieveChallengeByUUID(UUID.fromString((String)o));
                if(optReqCha.isPresent()){
                    getIndex().addRequired(IPacket.Target.CHALLENGE, c.getUuid(), optReqCha.get());
                }
            }
        }
        //Needed to update categories and challenges in inventory
        Challenges.updateInventoryManager();
    }

    public boolean removeCategory(Category category){
        if(categories.contains(category)){
            if(getIndex().removeCategory(category)){
                categories.remove(category);
                File f = new File(categoriesBaseDirectory, category.getUuid().toString()+".json");
                if(f.exists()){
                    f.delete();
                }
                return true;
            }
        }
        return false;
    }

    public void removeChallenge(Challenge challenge){
        if(challenges.contains(challenge)){
            Category parent = challenge.getCategory();
            if(parent!=null){
                getIndex().removeChallenge(parent, challenge);
            }
            for(Category c : categories){
                if(getIndex().getRequiredChallenges(c).contains(challenge)){
                    getIndex().removeRequired(IPacket.Target.CATEGORY, c.getUuid(), challenge);
                }
            }
            for(Challenge c : challenges){
                if(getIndex().getRequiredChallenges(c).contains(challenge)){
                    getIndex().removeRequired(IPacket.Target.CHALLENGE, c.getUuid(), challenge);
                }
            }
            challenges.remove(challenge);
            File f = new File(challengesBaseDirectory, challenge.getUuid().toString()+".json");
            if(f.exists()){
                f.delete();
            }
        }
    }

    public UniPlayer loadPlayer(UUID player){
        File f = new File(Manager.playersBaseDirectory, player.toString()+".json");
        UniPlayer p;
        if(f.exists()){
            p = new UniPlayer(JsonUtils.readFile(f), Bukkit.getOfflinePlayer(player)); //default value in case of file not contains UUID
        }else{
            p = new UniPlayer(player);
        }
        players.add(p);
        return p;
    }

    public void unloadPlayer(UUID player){
        UniPlayer toUnload = null;
        for(UniPlayer p : players){
            if(p.getOfflinePlayer().getUniqueId().equals(player)){
                toUnload = p;
                break;
            }
        }
        if(toUnload!=null){
            players.remove(toUnload);
            toUnload.unload();
        }
    }

    public Storage loadStorage(UUID island){
        long start = System.currentTimeMillis();
        Storage s = new Storage(island);
        loadedStorages.add(s);
        Challenges.getResetManager().addIsland(island);
        System.out.println("Loaded island storage with uuid " + island + " in " + (System.currentTimeMillis()-start) + " milliseconds.");
        return s;
    }

    /**
     * Unload the storage only if no players added on the island are online
     * <p>
     * To force unload, see {@link #unloadStorage(UUID)}
     *
     * @param island UUID of the island you want to unload
     * @return  void
     */
    public void unloadStorageSafe(UUID island){
        Island is = Challenges.getFabledApi().retrieveIslandByUUID(island);
        if(is==null||Challenges.getFabledApi().getOnlinePlayersOnIsland(is,false,false).size()==0){
            unloadStorage(island);
        }
    }

    /**
     * Unload the storage even if island players are online.
     *
     * To safe unload, see {@link #unloadStorageSafe(UUID)}
     *
     * @param island UUID of the island you want to unload
     * @return  void
     */
    public void unloadStorage(UUID island){
        long start = System.currentTimeMillis();
        retrieveStorageByUUID(island).ifPresent(s->{
            loadedStorages.remove(s);//obligé de le mettre avant s.unload() car dans s.unload() on dit attributs de island = nulls, donc la méthode remove de l'arreylist ne peut pas utiliser storage.equals(storage) car la méthode equals utilise les attributs de storage qui ont étés mis a null
            s.unload();
            System.out.println("Unloaded island storage with uuid " + island + " in " + (System.currentTimeMillis()-start) + " milliseconds.");
        });
    }

    public boolean isStorageLoaded(UUID island){
        return retrieveStorageByUUID(island).isPresent();
    }

    public List<Storage> getLoadedStorages(){
        return loadedStorages;
    }

    public List<UniPlayer> getLoadedPlayers(){
        return players;
    }

    public Optional<Storage> retrieveStorageByUUID(UUID storage){
        Storage found = null;
        for(Storage s : loadedStorages){
            if(s.getIsland().equals(storage)){
                found = s;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

    public Optional<Category> retrieveCategoryByUUID(UUID u){
        Category found = null;
        for (Category c : getCategories()) {
            if(u.equals(c.getUuid())){
                found = c;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

    public Optional<Challenge> retrieveChallengeByUUID(UUID u){
        Challenge found = null;
        if(u!=null){
            for (Challenge c : getChallenges()) {
                if(u.equals(c.getUuid())){
                    found = c;
                    break;
                }
            }
        }
        return Optional.ofNullable(found);
    }

    public Optional<Challenge> retrieveChallengeByName(String challenge){
        Challenge found = null;
        for (Challenge c : getChallenges()) {
            if(challenge.equalsIgnoreCase(c.getName())){
                found = c;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

    public Optional<Category> retrieveCategoryByName(String category){
        Category found = null;
        for (Category c : getCategories()) {
            if(category.equalsIgnoreCase(c.getName())){
                found = c;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

    public List<Challenge> retrieveChallengesByUUID(UUID...u){
        List<Challenge> challenges = new ArrayList<>();
        for(UUID uuid : u){
            retrieveChallengeByUUID(uuid).ifPresent(challenges::add);
        }
        return challenges;
    }

    @Nonnull
    public UniPlayer retrieveUniPlayerByUUID(@Nonnull UUID player){
        for(UniPlayer p : players){
            if(p.getOfflinePlayer().getUniqueId().equals(player))return p;
        }
        return loadPlayer(player);
    }
}
