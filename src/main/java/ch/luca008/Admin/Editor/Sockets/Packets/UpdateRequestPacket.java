package ch.luca008.Admin.Editor.Sockets.Packets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UpdateRequestPacket implements IPacket{

    private String uuid;
    private Target target;
    private JSONObject json;

    public UpdateRequestPacket(JSONObject json){
        uuid = (String)json.get("UUID");
        target = Target.valueOf((String)json.get("Type"));
        try{
            this.json = (JSONObject) new JSONParser().parse((String)json.get("JSON"));
        } catch (ParseException e) {
            System.err.println("Can't receive UpdateRequestPacket because JSON content is wrong. Error;");
            e.printStackTrace();
        }
    }

    public String getUuid(){
        return uuid;
    }

    public Target getType(){
        return target;
    }

    public JSONObject getJson(){
        return json;
    }

    @Override
    public int getID() {
        return -1;
    }

    @Override
    public JSONObject toJson() {
        return new JSONObject();
    }
}

