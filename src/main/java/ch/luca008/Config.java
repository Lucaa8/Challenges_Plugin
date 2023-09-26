package ch.luca008;

import ch.luca008.ChallengesManager.Manager;
import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.PromptPlayer;
import ch.luca008.Utils.SbItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.Dye;
import org.json.simple.JSONObject;

public class Config {

    private SbItem ChallengesMenuIcon = new SbItem(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName("§0-").createItem());
    private String ChallengesDescriptionColor = "§f";
    private String CategoriesDescriptionColor = "§f";
    private String ChallengeNameColorItem = "§f";
    private String CurrencyName = "Coins";
    private boolean doReset = true;
    private int editorPort = 25575;
    private boolean editorLogging = false; //Do log packet in/out ? (Except KeepAlive)
    private int editorKeepAliveTimeout = 60000;
    private int editorWaitingClientTimeout = 60000; //60000/500=120 essais de 500ms

    public Config(){
        reload();
    }

    public void reload(){
        JSONObject json = JsonUtils.readFile(Manager.globalConfigFile);
        if(json!=null&&!json.isEmpty()){
            if(json.containsKey("Challenges-Menu-Icon")){
                ChallengesMenuIcon = SbItem.fromJson(((JSONObject)json.get("Challenges-Menu-Icon")).toJSONString());
            }
            if(json.containsKey("Challenges-Descriptions-Color")){
                JSONObject j = (JSONObject) json.get("Challenges-Descriptions-Color");
                if(j!=null){
                    if(j.containsKey("Challenges"))ChallengesDescriptionColor=(String)j.get("Challenges");
                    if(j.containsKey("Categories"))CategoriesDescriptionColor=(String)j.get("Categories");
                }
            }
            if(json.containsKey("Challenge-Name-Color")){
                ChallengeNameColorItem = (String)json.get("Challenge-Name-Color");
            }
            if(json.containsKey("Currency-Name")){
                CurrencyName = (String)json.get("Currency-Name");
            }
            if(json.containsKey("Do-Reset")){
                doReset = (Boolean)json.get("Do-Reset");
            }
            if(json.containsKey("Prompt-Color")) {
                try {
                    SpigotApi.getPromptApi().promptColor = DyeColor.valueOf(((String) json.get("Prompt-Color")).toUpperCase());
                } catch (Exception ignored) {}
            }
            if(json.containsKey("Prompt-Cancel-CMD")){
                SpigotApi.getPromptApi().cancelCmd = (String)json.get("Prompt-Cancel-CMD");
            }
            if(json.containsKey("Editor-Port")){
                editorPort = JsonUtils.getInt(json, "Editor-Port");
            }
            if(json.containsKey("Editor-Logging")){
                editorLogging = (Boolean)json.get("Editor-Logging");
            }
            if(json.containsKey("Editor-KeepAlive-Timeout")){
                editorKeepAliveTimeout = JsonUtils.getInt(json, "Editor-KeepAlive-Timeout");
            }
            if(json.containsKey("Editor-WaitingClient-Timeout")){
                editorWaitingClientTimeout = JsonUtils.getInt(json, "Editor-WaitingClient-Timeout");
            }
        }
    }

    public SbItem getChallengesMenuIcon(){
        return ChallengesMenuIcon;
    }
    public String getChallengesDescriptionColor(){
        return ChallengesDescriptionColor;
    }
    public String getCategoriesDescriptionColor(){
        return CategoriesDescriptionColor;
    }
    public String getChallengeNameColorItem(){
        return ChallengeNameColorItem;
    }
    public String getCurrencyName(){
        return CurrencyName;
    }
    public boolean doReset(){
        return doReset;
    }
    public DyeColor getPromptColor() {
        return SpigotApi.getPromptApi().promptColor;
    }
    public String getPromptCancelCmd() {
        return SpigotApi.getPromptApi().cancelCmd;
    }
    public int getEditorPort(){
        return editorPort;
    }
    public boolean doEditorLogIO(){
        return editorLogging;
    }
    public int getEditorKeepAliveTimeout(){
        return editorKeepAliveTimeout;
    }
    public int getEditorWaitingAttemps(){
        return editorWaitingClientTimeout/500;
    }
}
