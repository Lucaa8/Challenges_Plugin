package ch.luca008.ChallengesManager.Required;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class RequiredLoader {

    public Required load(JSONObject json){
        try {
            String type = (String)json.get("RequiredType");
            if(type!=null&&!type.isEmpty()){
                return (Required)Class.forName(this.getClass().getPackage().getName()+"."+type).getConstructor(JSONObject.class).newInstance((JSONObject)json.get("Required"));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Can't load Required with JSON: \n" + JsonUtils.prettyJson(json));
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject unload(Required required){
        JSONObject j = new JSONObject();
        j.put("RequiredType", required.getType().toString());
        j.put("Required", required.toJson());
        return j;
    }
}
