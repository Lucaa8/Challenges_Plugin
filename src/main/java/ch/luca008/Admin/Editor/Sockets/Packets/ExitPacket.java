package ch.luca008.Admin.Editor.Sockets.Packets;

import org.json.simple.JSONObject;

public class ExitPacket implements IPacket{

    private String exitReason = "Non spécifiée";

    public ExitPacket(String reason){
        this.exitReason = reason;
    }

    public ExitPacket(JSONObject json){
        if(json.containsKey("Reason")){
            this.exitReason = (String)json.get("Reason");
        }
    }

    public String getExitReason(){
        return exitReason;
    }

    @Override
    public int getID() {
        return -1;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Reason", exitReason);
        return json;
    }
}
