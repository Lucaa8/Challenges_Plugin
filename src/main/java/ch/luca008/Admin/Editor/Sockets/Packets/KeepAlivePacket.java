package ch.luca008.Admin.Editor.Sockets.Packets;

import org.json.simple.JSONObject;

public class KeepAlivePacket implements IPacket{

    public KeepAlivePacket(JSONObject json){

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

