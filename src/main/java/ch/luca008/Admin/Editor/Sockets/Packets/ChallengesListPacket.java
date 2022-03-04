package ch.luca008.Admin.Editor.Sockets.Packets;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

public class ChallengesListPacket implements IPacket{

    private int id = -1;
    private Map<String,String> categories;
    private Map<String, String> challenges;

    public ChallengesListPacket(JSONObject json){
        if(json.containsKey("ID")){
            id = JsonUtils.getInt(json,"ID");
        }
    }

    public ChallengesListPacket(int id, Map<String,String> categories, Map<String, String> challenges){
        this.id = id;
        this.categories = categories;
        this.challenges = challenges;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray cha = new JSONArray();
        JSONArray cat = new JSONArray();
        for(Map.Entry<String,String> challenge : challenges.entrySet()){
            JSONObject j = new JSONObject();
            j.put("UUID", challenge.getKey());
            j.put("Name", challenge.getValue());
            cha.add(j);
        }
        for(Map.Entry<String,String> category : categories.entrySet()){
            JSONObject j = new JSONObject();
            j.put("UUID", category.getKey());
            j.put("Name", category.getValue());
            cat.add(j);
        }
        json.put("Challenges", cha);
        json.put("Categories", cat);
        json.put("ID", id);
        return json;
    }
}

