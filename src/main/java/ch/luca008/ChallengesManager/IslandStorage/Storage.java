package ch.luca008.ChallengesManager.IslandStorage;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.Stats;
import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Storage {

    public enum AccessType{
        NETHER, END
    }

    private UUID island;
    private Map<UUID, ChallengeStorage> storage;
    private UUID statisticChallengeActive;
    private boolean isNetherUnlocked = false;
    private boolean isEndUnlocked = false;

    public Storage(UUID island){
        this.island = island;
        Map<UUID, ChallengeStorage> storage = new ConcurrentHashMap<>();
        File file = new File(Manager.islandsBaseDirectory, island.toString()+".json");
        if(file.exists()){
            JSONObject json = JsonUtils.readFile(file);
            if(json.containsKey("ActiveStatChallenge"))statisticChallengeActive = UUID.fromString((String)json.get("ActiveStatChallenge"));
            if(json.containsKey("NetherUnlocked"))isNetherUnlocked = (boolean) json.get("NetherUnlocked");
            if(json.containsKey("EndUnlocked"))isEndUnlocked = (boolean) json.get("EndUnlocked");
            if(json.containsKey("ChallengesStorage")){
                JSONObject jdata = (JSONObject) json.get("ChallengesStorage");
                List<UUID> uuidsToLoad = (List<UUID>) jdata.keySet().stream().filter(s -> s instanceof String).map(s -> UUID.fromString((String)s)).collect(Collectors.toList());
                for(UUID u : uuidsToLoad){
                    storage.put(u,new ChallengeStorage((JSONObject) jdata.get(u.toString())));
                }
            }
        }
        this.storage = storage;
        syncStats();
    }

    public void syncStats(){
        Optional<Challenge> active = getActiveStatChallenge();
        if(active.isPresent()){
            Challenge cha = active.get();
            if(cha.getRequired() instanceof Stats&&storage.containsKey(cha.getUuid())){
                ChallengeStorage str = storage.get(cha.getUuid());
                if(cha.getCategory()==null){
                    setStatisticChallengeActive(null);
                    str.setCompletable(null);
                    return;
                }
                if(str.getCompletable() instanceof StatisticCompletable){
                    StatisticCompletable completable = (StatisticCompletable) str.getCompletable();
                    for(Stats.Stat stat : ((Stats)cha.getRequired()).getStats()){
                        if(!completable.containsStat(stat)){
                            completable.addNewStat(stat);
                        }
                    }
                }
            }
        }
    }

    public UUID getIsland() {
        return island;
    }

    public Map<UUID, ChallengeStorage> getStorages(){
        return storage;
    }

    /**
     * Create or get a challenge storage for the specified challenge
     * @param challenge a challenge's uuid
     * @return a non-null ChallengeStorage with x completions, y daily completions and z Completable(can be null, See {@link ChallengeStorage#hasCompletable()}). If new, x and y would be 0, and completable null.
     */
    @Nonnull
    public ChallengeStorage getStorage(UUID challenge){
        if(!storage.containsKey(challenge)){
            storage.put(challenge, new ChallengeStorage());
        }
        return storage.get(challenge);
    }

    public void setStatisticChallengeActive(Challenge challenge){
        if(challenge==null)statisticChallengeActive = null;
        else statisticChallengeActive = challenge.getUuid();
    }
    public Optional<Challenge> getActiveStatChallenge(){
        return Challenges.getManager().retrieveChallengeByUUID(statisticChallengeActive);
    }

    private List<Challenge> _getMissingRequired(List<Challenge> challenges){
        List<Challenge> notUnlocked = new ArrayList<>();
        for(Challenge c : challenges){
            UUID uuid = c.getUuid();
            if(!storage.containsKey(uuid)||storage.get(uuid).getTotalCompleted()<1){
                notUnlocked.add(c);
            }
        }
        return notUnlocked;
    }
    /**
     * Get all challenges still required to unlock this challenge.
     * <p>
     * If the challenge is unlocked return empty list.
     * <p>
     * isUnlocked(Challenge).size==0 return true if the challenge is unlocked
     * @param challenge a non-null challenge
     * @return A list of all missing challenges to unlock the challenge
     */
    @Nonnull
    public List<Challenge> isUnlocked(@Nonnull Challenge challenge){
        return _getMissingRequired(Challenges.getManager().getIndex().getRequiredChallenges(challenge));
    }

    /**
     * Get all challenges still required to unlock this category.
     * <p>
     * If the category is unlocked return empty list.
     * <p>
     * isUnlocked(Category).size==0 return true if the category is unlocked
     * @param category a non-null category
     * @return A list of all missing challenges to unlock the category
     */
    @Nonnull
    public List<Challenge> isUnlocked(@Nonnull Category category){
        return _getMissingRequired(Challenges.getManager().getIndex().getRequiredChallenges(category));
    }

    public boolean canAccess(AccessType type){
        if(type==AccessType.NETHER) return isNetherUnlocked;
        else if(type == AccessType.END) return isEndUnlocked;
        return false;
    }
    public void giveAccess(AccessType type, boolean access){
        if(type==AccessType.NETHER) isNetherUnlocked = access;
        else if(type == AccessType.END) isEndUnlocked = access;
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(statisticChallengeActive!=null){
            j.put("ActiveStatChallenge", statisticChallengeActive.toString());
        }
        if(isNetherUnlocked){
            j.put("NetherUnlocked", true);
        }
        if(isEndUnlocked){
            j.put("EndUnlocked", true);
        }
        JSONObject jdata = new JSONObject();
        for(Map.Entry<UUID, ChallengeStorage> entry : storage.entrySet()){
            JSONObject json = entry.getValue().toJson();
            if(!json.isEmpty())jdata.put(entry.getKey().toString(), json);
        }
        if(!jdata.isEmpty())j.put("ChallengesStorage", jdata);
        return j;
    }

    public void unload(){
        JSONObject json = toJson();
        File f = new File(Manager.islandsBaseDirectory, island.toString()+".json");
        if(json!=null&&!json.isEmpty()){
            JsonUtils.write(f, JsonUtils.prettyJson(json));
        }else{
            if(f.exists())f.delete();
        }
        this.island = null;
        this.storage = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Storage)) return false;
        Storage storage = (Storage) o;
        return island.equals(storage.island);
    }

    @Override
    public int hashCode() {
        return Objects.hash(island);
    }

    public static class ChallengeStorage {
        private Completable completable;
        private int totalCompleted = 0;
        private int dailyCompleted = 0;
        public ChallengeStorage(JSONObject json){
            if(json.containsKey("Completable")){
                completable = new CompletableLoader().load((JSONObject) json.get("Completable"));
            }
            if(json.containsKey("TotalCompleted")){
                totalCompleted = JsonUtils.getInt(json, "TotalCompleted");
            }
            if(json.containsKey("DailyCompleted")){
                dailyCompleted = JsonUtils.getInt(json, "DailyCompleted");
            }
        }
        public ChallengeStorage(){}

        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            if(completable!=null){
                JSONObject json = new CompletableLoader().unload(completable);
                if(!json.isEmpty())j.put("Completable", json);
            }
            if(totalCompleted>0)j.put("TotalCompleted", totalCompleted);
            if(dailyCompleted>0)j.put("DailyCompleted", dailyCompleted);
            return j;
        }

        public Completable setCompletable(Completable completable){
            this.completable = completable;
            return this.completable;
        }
        public boolean hasCompletable(){
            return getCompletable()!=null;
        }

        /**
         * Will be null everytime on new ChallengeStorage(){}
         * <p>
         * Can be null on new ChallengeStorage(JSONObject)
         * <p>
         * Check with the {@link #hasCompletable()} before calling!
         */
        @Nullable
        public Completable getCompletable() {
            return completable;
        }

        public int getTotalCompleted() {
            return totalCompleted;
        }

        public int getDailyCompleted() {
            return dailyCompleted;
        }

        public void setTotalCompleted(int totalCompleted) {
            this.totalCompleted = totalCompleted;
        }

        public void setDailyCompleted(int dailyCompleted) {
            this.dailyCompleted = dailyCompleted;
        }

        public void addCompletion(){
            setTotalCompleted(getTotalCompleted()+1);
            setDailyCompleted(getDailyCompleted()+1);
        }
    }

}
