package ch.luca008.ChallengesManager.Required;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.StatisticCompletable;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.Broadcast;
import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.StringUtils;
import com.songoda.skyblock.api.island.Island;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.*;

public class Stats implements Required{

    private List<Stat> statsList;

    public Stats(JSONObject json){
        this.statsList = new ArrayList<>();
        if(json.containsKey("Statistics")){
            JSONArray jarr = (JSONArray) json.get("Statistics");
            for (Object o : jarr) {
                statsList.add(new Stat((JSONObject) o));
            }
        }
    }
    public Stats(Stat...stats){
        this.statsList = new ArrayList<>();
        Collections.addAll(this.statsList, stats);
    }

    public List<Stat> getStats(){
        return statsList;
    }

    //Called only to fix the time since death bug (see ChallengeRelatedEvents class to get more informations about this fix)
    public static void updateStat(Player p, Statistic statistic){
        Island island = Challenges.getFabledApi().getIsland(p);
        if(island!=null){
            Storage s = Challenges.getManager().retrieveStorageByUUID(island.getIslandUUID()).orElse(null);
            if(s!=null){
                Challenge c = s.getActiveStatChallenge().orElse(null);
                if(c!=null){
                    Required r = c.getRequired();
                    if(r instanceof Stats){
                        Stats stats = (Stats)r;
                        Storage.ChallengeStorage cstr = s.getStorage(c.getUuid());
                        if(cstr.hasCompletable()){
                            StatisticCompletable completable = (StatisticCompletable) cstr.getCompletable();
                            for(Stat stat : stats.getStats()){
                                if(stat.statistic == statistic){
                                    completable.resetCompletableCount(stat, p);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        JSONArray jarr = new JSONArray();
        for (Stat stat : statsList) {
            jarr.add(stat.toJson());
        }
        j.put("Statistics", jarr);
        return j;
    }

    @Override
    public String toLore(Storage.ChallengeStorage storage, UniPlayer player) {
        Optional<Island> optIs = player.getIsland();
        if(optIs.isPresent()){
            String lore = "";
            Map<Stat,Integer> optValues = getValues(optIs.get());
            if(optValues==null){
                for(Stat s : statsList){
                    lore+="\n"+s.toLore(player,-1);
                }
            }else{
                for(Stat s : statsList){
                    lore+="\n"+s.toLore(player,optValues.getOrDefault(s,0));
                }
            }
            return lore+"\n";
        }
        return "No island?";
    }

    @Override
    public String toString(){
        String i = "";
        if(statsList!=null&&!statsList.isEmpty()){
            i="{";
            for (Stat s : statsList) {
                i+=s.toString()+",";
            }
            i = i.substring(0,i.length()-1)+"}";
        }
        return "Stats{"+(i.isEmpty()?"NULL":i)+"}";
    }

    @Override
    public RequiredType getType() {
        return RequiredType.Stats;
    }

    //Retourne null si island=null ou que cette classe de requis ne gère pas le challenge actuellement actif de l'île.
    @Nullable
    private Map<Stat,Integer> getValues(@Nullable Island island){
        if(island==null)return null;
        Optional<Storage> optIslandStorage = Challenges.getManager().retrieveStorageByUUID(island.getIslandUUID());
        if(optIslandStorage.isPresent()){
            Storage islandStorage = optIslandStorage.get();
            Optional<Challenge> optStartedChallenge = islandStorage.getActiveStatChallenge();
            if(optStartedChallenge.isPresent()) {
                Challenge startedChallenge = optStartedChallenge.get();
                Required r = startedChallenge.getRequired();
                if(r instanceof Stats){
                    Stats stats = (Stats)r;
                    if(stats.equals(this)){
                        StatisticCompletable completable = (StatisticCompletable) islandStorage.getStorage(startedChallenge.getUuid()).getCompletable();
                        if(completable!=null){
                            Map<Stat,Integer> map = new HashMap<>();
                            List<OfflinePlayer> islandPlayers = Challenges.getFabledApi().getPlayersOnIsland(island,false,false);
                            for(Stat s : statsList){
                                int done = 0;
                                for(OfflinePlayer islandPlayer : islandPlayers){
                                    done+=completable.getCompletableCount(s, islandPlayer);
                                }
                                map.put(s, Math.min(done, s.getCount()));
                            }
                            return map;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CompletableResult complete(Challenge c, UniPlayer p) {
        Optional<Storage> optStorage = p.getIslandStorage();
        if(optStorage.isPresent()){
            Storage storage = optStorage.get();
            Optional<Challenge> optStarted = storage.getActiveStatChallenge();
            String msg = "";
            Object bcMsg = null;
            boolean isCompleted = false;
            boolean hasProgressed = false; //only to update inventory after someone started a challenge
            if(optStarted.isPresent()){
                Challenge started = optStarted.get();
                if(started.getUuid().equals(c.getUuid())){
                    isCompleted = true;
                    Map<Stat,Integer> values = getValues(p.getIsland().orElse(null)); //Permis grace a if(started.getUuid().equals(c.getUuid()))
                    String valuesMsg = "";
                    int uncompleted = 0;
                    for(Map.Entry<Stat,Integer> val : values.entrySet()){
                        if(val.getValue()<val.getKey().getCount()){
                            if(isCompleted){
                                isCompleted = false;
                                msg = p.getMessage("Challenge-Completion-Unable", Map.entry("{0}",c.getName()));
                            }
                            uncompleted++;
                            Stat s = val.getKey();
                            valuesMsg+="§7- §a"+ StringUtils.enumName(s.getStatistic())+ (s.isSubstatistic()?"§b("+StringUtils.enumName(s.getSubstatistic())+")":"")+ " §9"+val.getValue()+"/"+s.getCount()+"\n";
                        }
                    }
                    if(!valuesMsg.isEmpty()){
                        msg+=p.getMessage("Challenge-Completion-Stat-Missing-"+(uncompleted==1?"Singular":"Plural"), Map.entry("{0}", valuesMsg));
                    }
                }else{
                    msg = p.getMessage("Challenge-Completion-Stat-Already-Started", Map.entry("{0}",started.getName()));
                }
            }else{
                storage.setStatisticChallengeActive(c);
                storage.getStorage(c.getUuid()).setCompletable(StatisticCompletable.forIsland(statsList, Challenges.getFabledApi().getPlayersOnIsland(p.getIsland().get(),false,false)));
                hasProgressed = true;
                bcMsg = new Broadcast() {
                    @Override
                    public String getKeyMessage() {
                        return "Challenge-Completion-Stat-Started";
                    }

                    @Override
                    public Map<String, String> getReplacements() {
                        Player pl = p.getPlayer().orElse(null);
                        return Map.of("{0}",c.getName(),"{1}",(pl==null?"?":pl.getName()));
                    }
                };
            }
            Object finalMsg = bcMsg!=null?bcMsg:msg;
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
                    if(finalIsCompleted)return "";
                    return finalMsg;
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

    public static class Stat{
        private UUID uuid;
        private Statistic statistic;
        private Enum<?> substatistic;
        private int count;
        private boolean isTimeBased = false;
        private boolean isDistanceBased = false;
        public Stat(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Statistic")){
                statistic = Statistic.valueOf((String)json.get("Statistic"));
            }else statistic = Statistic.PLAY_ONE_MINUTE;
            if(statistic.isSubstatistic()&&json.containsKey("Substatistic")){
                Statistic.Type type = statistic.getType();
                if(type==Statistic.Type.BLOCK||type==Statistic.Type.ITEM){
                    substatistic = Material.valueOf((String)json.get("Substatistic"));
                }else if(type==Statistic.Type.ENTITY){
                    substatistic = EntityType.valueOf((String)json.get("Substatistic"));
                }
            }
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json, "Count");
            }else count = 10;
            final String sname = StringUtils.enumName(statistic.name());
            if(sname.contains("One Minute")||sname.contains("Time"))isTimeBased = true;
            else if(sname.contains("One Cm"))isDistanceBased = true;
        }
        public Stat(Statistic stat, Enum<?> substatistic, int count){
            this.uuid = UUID.randomUUID();
            this.statistic = stat==null?Statistic.PLAY_ONE_MINUTE:stat;
            this.substatistic = substatistic;
            this.count = count;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Statistic", statistic.name());
            if(substatistic!=null){
                j.put("Substatistic", substatistic.name());
            }
            j.put("Count", count);
            return j;
        }
        public String toLore(UniPlayer player, int progress){
            String lore = " §f- §a"+player.getStatistic(statistic)+" §b";
            if(isTimeBased){
                lore += StringUtils.getTime(count, player.getLang())+(progress!=-1?(" §7- §9"+StringUtils.getTime(progress,player.getLang())):"");
            }else if(isDistanceBased){
                lore += count + player.getUnit("Meter") + (progress!=-1?(" §7- §9"+progress+"/"+count):"");
            }else{
                lore += count + " §a" + (isSubstatistic()?(StringUtils.enumName(substatistic)):player.getUnit("Times")) +(progress!=-1?(" §7- §9"+progress+"/"+count):"");
            }
            return lore;
        }
        @Override
        public String toString() {
            return "Stat{" +
                    "uuid=" + uuid +
                    ", statistic=" + statistic +
                    ", substatistic=" + substatistic +
                    ", count=" + count +
                    '}';
        }

        public UUID getUuid(){
            return uuid;
        }
        public Statistic getStatistic() {
            return statistic;
        }
        public boolean isSubstatistic(){return substatistic!=null;}
        public Enum<?> getSubstatistic() {
            return substatistic;
        }
        public int getCount() {
            return count;
        }
        public int getCount(OfflinePlayer player){
            int count;
            if(isSubstatistic()){
                Object sub = getSubstatistic();
                if(sub.getClass().equals(Material.class)){
                    count = player.getStatistic(getStatistic(), (Material)sub);
                }else{
                    count = player.getStatistic(getStatistic(), (EntityType) sub);
                }
            }else count = player.getStatistic(getStatistic());
            return isDistanceBased?(count/100):isTimeBased?(count/20/60):count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Stat)) return false;
            Stat stat = (Stat) o;
            return uuid.equals(stat.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }
    }

}
