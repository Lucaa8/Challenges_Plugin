package ch.luca008.ChallengesManager.IslandStorage;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class CompletableLoader {

    public Completable load(JSONObject json){
        try {
            String type = (String)json.get("CompletableType");
            if(type!=null&&!type.isEmpty()){
                return (Completable) Class.forName(this.getClass().getPackage().getName()+"."+type).getConstructor(JSONObject.class).newInstance((JSONObject)json.get("Data"));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Can't load Completable with JSON: \n" + JsonUtils.prettyJson(json));
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject unload(Completable completable){
        JSONObject j = new JSONObject();
        JSONObject data = completable.toJson();
        if(!data.isEmpty()){
            j.put("CompletableType", completable.getType().getClazz());
            j.put("Data", completable.toJson());
        }
        return j;
    }
}
