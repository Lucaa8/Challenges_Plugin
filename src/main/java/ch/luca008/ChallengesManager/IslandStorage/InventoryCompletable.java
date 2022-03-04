package ch.luca008.ChallengesManager.IslandStorage;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryCompletable implements Completable{

    private Map<UUID, Integer> completableCount;

    public InventoryCompletable(JSONObject json){
        completableCount = new HashMap<>();
        if(json!=null&&!json.isEmpty()){
            json.keySet().forEach(k->{
                int count = JsonUtils.getInt(json, k.toString());
                if(count>0)completableCount.put(UUID.fromString((String)k), count);
            });
        }
    }
    public InventoryCompletable(List<UUID> uuids){
        completableCount = new HashMap<>();
        uuids.forEach(u->completableCount.put(u,0));
    }

    public int addCompletableCount(UUID completable, int toAdd){
        int count = getCompletableCount(completable);
        setCompletableCount(completable, count+toAdd);
        return getCompletableCount(completable);
    }

    public void setCompletableCount(UUID completable, int count){
        this.completableCount.put(completable, count);
    }

    public int getCompletableCount(UUID completable){
        return this.completableCount.getOrDefault(completable, 0);
    }

    @Override
    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        completableCount.forEach((U,C)->{
            if(C>0){
                j.put(U.toString(), C);
            }
        });
        return j;
    }

    @Override
    public String toString(){
        return "";
    }

    @Override
    public CompletableType getType(){
        return CompletableType.INVENTORY;
    }
}
