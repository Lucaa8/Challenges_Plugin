package ch.luca008.Utils;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.Comparators.CategoriesSlotComparator;
import ch.luca008.Comparators.ChallengesSlotComparator;
import ch.luca008.Lang;
import ch.luca008.UniPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {

    public static Map<String, Color> colors = new HashMap<String, Color>() {{
        put("blue", Color.BLUE);
        put("aqua", Color.AQUA);
        put("black", Color.BLACK);
        put("yellow", Color.YELLOW);
        put("silver", Color.SILVER);
        put("fuchsia", Color.FUCHSIA);
        put("gray", Color.GRAY);
        put("white", Color.WHITE);
        put("orange", Color.ORANGE);
        put("lime", Color.LIME);
        put("maroon", Color.MAROON);
        put("navy", Color.NAVY);
        put("purple", Color.PURPLE);
        put("green", Color.GREEN);
        put("teal", Color.TEAL);
        put("olive", Color.OLIVE);
        put("red", Color.RED);
    }};

    public static String addCap(String s){
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String enumName(String name){
        if(!name.contains("_"))return addCap(name);
        String finalName = "";
        for(String s : name.split("_")){
            finalName+=addCap(s)+" ";
        }
        return finalName.substring(0,finalName.length()-1);
    }
    public static String enumName(Enum<?> e){
        return enumName(e.name());
    }

    public static Color colorFromString(String str){
        if(colors.containsKey(str.toLowerCase())){
            return colors.get(str.toLowerCase());
        }
        return Color.BLACK;
    }

    public static String buildString(String[] args, int offset){
        String s = "";
        for(int i=offset;i<args.length;i++){
            s+=args[i]+" ";
        }
        if(!s.isEmpty())s=s.substring(0,s.length()-1);
        return s;
    }

    public static boolean equalLists(List<String> a, List<String> b){
        if (a == null && b == null) return true;
        if (((a==null)!=(b==null)) || (a.size() != b.size()))return false;
        return a.equals(b);
    }

    public static boolean doHeadMatches(String name){
        if(name==null)return false;
        Lang l = Challenges.getLangManager();
        for(String lang : l.getAvailableLangs()){
            String reg = l.getMessage(lang, "Challenge-Item-Skull", false).replace("{0}","[a-zA-Z0-9_]*");
            if(name.matches(reg))return true;
        }
        return false;
    }

    /*
    - Catégorie Is Level:
      - Roi
      - Architecte
    - Catégorie Cactus:
      - Cactus I
      - Cactus II
     */
    public static TextComponent missingChallengesAsString(List<Challenge> missing, UniPlayer p){
        Map<Category, List<Challenge>> index = new HashMap<>();
        for(Challenge c : missing){
            Category parent = c.getCategory();
            if(parent!=null){
                List<Challenge> challengeList = index.getOrDefault(parent, new ArrayList<>());
                challengeList.add(c);
                index.put(parent, challengeList);
            }
        }
        String catTrad = p.getMessage("Category");
        TextComponent txtFinal = new TextComponent();
        List<Category> sortedCategories = new ArrayList<>(index.keySet());
        sortedCategories.sort(new CategoriesSlotComparator());
        for(Category category : sortedCategories){
            TextComponent text = new TextComponent("\n §a- "+catTrad+" ");
            TextComponent categoryComponent = new TextComponent("§b"+category.getName()+"§a:");
            String hover = p.getMessage("Challenge-Completion-Not-Unlocked-Hover-Category", Map.entry("{0}",category.getName()));
            for(Challenge c : Challenges.getManager().getIndex().getChallenges(category)){
                hover+="\n §7- §9"+c.getName();
            }
            categoryComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
            text.addExtra(categoryComponent);
            List<Challenge> challenges = index.get(category);
            challenges.sort(new ChallengesSlotComparator());
            for(Challenge challenge : challenges){
                TextComponent text2 = new TextComponent("\n  §7- ");
                TextComponent challengeComponent = new TextComponent("§9"+challenge.getName());
                challengeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(p.getMessage("Challenge-Completion-Not-Unlocked-Hover-Challenge",Map.entry("{0}",challenge.getName()))).create()));
                challengeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/c c "+challenge.getName()));
                text2.addExtra(challengeComponent);
                text.addExtra(text2);
            }
            txtFinal.addExtra(text);
        }
        return txtFinal;
    }

    @Nonnull
    public static List<String> asLore(@Nullable String lore){
        if(lore==null||lore.isEmpty())return new ArrayList<>();
        lore = lore.replace("&","§");
        List<String> loreArray = new ArrayList<>();
        String lastClr = "";
        if(lore.contains("\n")){
            for(String s : lore.split("\\n")){
                String line;
                if(!lastClr.isEmpty()){
                    line=lastClr+s;
                }else line=s;
                loreArray.add(line);
                for(int i=0;i<line.length()-1;i++){
                    char id = line.charAt(i);
                    char code = line.charAt(i+1);
                    if(id=='§'&&code!='§'){
                        if(lastClr.length()>=4){
                            lastClr = ""+id+code;
                        }else{
                            lastClr+=""+id+code;
                        }
                    }
                }
            }
        }else loreArray.add(lore);
        return loreArray;
    }

    public static String getTime(int time, String lang){
        int d = time / 1440;
        int h = time % 1440 / 60;
        int m = time % 1440 % 60;
        String result = "";
        if(d>0) {
            result += String.format("%d"+Challenges.getLangManager().getUnit(lang, "Day")+" ", d);
        }
        if(h>0) {
            result += String.format("%d"+Challenges.getLangManager().getUnit(lang, "Hour")+" ", h);
        }
        if(m>0) {
            result += String.format("%02d"+Challenges.getLangManager().getUnit(lang, "Minute"), m);
        }
        return result.isEmpty()?"0":result;
    }
}
