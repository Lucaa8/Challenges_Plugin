package ch.luca008.ChallengesManager.IslandStorage;

import ch.luca008.ChallengesManager.Required.Stats;
import ch.luca008.Utils.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;

import java.util.*;

public class StatisticCompletable implements Completable{

    //          STAT    PLAYER  INITIAL_COUNT
    private Map<UUID, Map<UUID, Integer>> completableCount;

    public StatisticCompletable(JSONObject json){
        completableCount = new HashMap<>();
        if(json!=null&&!json.isEmpty()){
            json.keySet().forEach(k->{
                Map<UUID, Integer> players = new HashMap<>();
                JSONObject stat = (JSONObject) json.get(k);
                for (Object o : stat.keySet()) {
                    players.put(UUID.fromString((String)o), JsonUtils.getInt(stat, o.toString()));
                }
                completableCount.put(UUID.fromString((String)k), players);
            });
        }
    }
    public StatisticCompletable(Map<UUID, Map<UUID, Integer>> map){
        this.completableCount = map;
    }
    public static StatisticCompletable forIsland(List<Stats.Stat> stats, List<OfflinePlayer> players){
        Map<UUID, Map<UUID, Integer>> map = new HashMap<>();
        for(Stats.Stat s : stats){
            Map<UUID, Integer> playersStat = new HashMap<>();
            for(OfflinePlayer p : players){
                playersStat.put(p.getUniqueId(), s.getCount(p));
            }
            map.put(s.getUuid(), playersStat);
        }
        return new StatisticCompletable(map);
    }

    public void addNewStat(Stats.Stat s){
        if(!completableCount.isEmpty()){
            Set<UUID> players = completableCount.values().stream().findAny().map(Map::keySet).get();
            Map<UUID, Integer> statMap = new HashMap<>();
            for(UUID u : players){
                statMap.put(u, s.getCount(Bukkit.getOfflinePlayer(u)));
            }
            completableCount.put(s.getUuid(), statMap);
        }
    }

    //Called only to fix the time since death bug (see ChallengeRelatedEvents class to get more informations about this fix)
    public void resetCompletableCount(Stats.Stat s, OfflinePlayer player){
        if(completableCount.containsKey(s.getUuid())){
            completableCount.get(s.getUuid()).replace(player.getUniqueId(), s.getCount(player));
        }
    }

    public int getCompletableCount(Stats.Stat s, OfflinePlayer player){
        if(completableCount.containsKey(s.getUuid())){
            Map<UUID, Integer> players = completableCount.get(s.getUuid());
            if(players.containsKey(player.getUniqueId())){
                return s.getCount(player)-players.get(player.getUniqueId());
            }
        }
        return 0;
    }

    public boolean containsStat(Stats.Stat stat){
        return completableCount.containsKey(stat.getUuid());
    }

    @Override
    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        completableCount.forEach((U,M)->{
            JSONObject jdata = new JSONObject();
            M.forEach((UU,C)->{
                jdata.put(UU.toString(), C);
            });
            j.put(U.toString(), jdata);
        });
        return j;
    }

    @Override
    public CompletableType getType(){
        return CompletableType.STATISTIC;
    }

    @Override
    public String toString(){
        return "";
    }
}
