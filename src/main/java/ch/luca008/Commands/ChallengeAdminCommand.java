package ch.luca008.Commands;

import ch.luca008.Admin.Editor.SessionManager;
import ch.luca008.Admin.IslandInventory;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.Utils.Perms;
import ch.luca008.Utils.Perms.Permission;
import ch.luca008.Utils.PluginStatisticsUtils;
import ch.luca008.Utils.StringUtils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChallengeAdminCommand implements CommandExecutor {

    private Challenges main;

    private static Map<UUID, IslandInventory> islandSessions = new HashMap<>();

    /**
     * Get the current session for the given online player.
     * <p>
     * See {@link #createIslandSession(Player, UUID, String)} to create one
     * <p>
     * See {@link #deleteIslandSession(UUID)} to delete one
     * @param admin the player
     * @return the player's already created session or null if none
     */
    @Nullable
    public static IslandInventory getIslandSession(Player admin){
        if(admin!=null&&admin.isOnline()&&islandSessions.containsKey(admin.getUniqueId())){
            return islandSessions.get(admin.getUniqueId());
        }
        return null;
    }
    @Nullable
    public static IslandInventory createIslandSession(Player admin, UUID island, String targetPlayer){
        if(hasIslandSession(island))return null;
        if(admin!=null&&admin.isOnline()&&island!=null){
            if(getIslandSession(admin)==null){
                islandSessions.put(admin.getUniqueId(), new IslandInventory(admin, island, Bukkit.getOfflinePlayer(targetPlayer)));
                System.out.println("New island session created for player " + admin.getName() + " with island uuid " + island);
                return getIslandSession(admin);
            }
        }
        return null;
    }
    public static void deleteIslandSession(UUID admin){
        if(admin!=null){
            if(islandSessions.containsKey(admin)){
                islandSessions.get(admin).unload();
                islandSessions.remove(admin);
                Challenges.getAdminPrompt().removePlayer(admin);
                System.out.println("Deleted island session used by player " + admin.toString());
            }
        }
    }
    public static Map<UUID, IslandInventory> getIslandSessions(){
        return islandSessions;
    }
    public static boolean hasIslandSession(UUID island){
        for(IslandInventory sess : getIslandSessions().values()){
            if(sess.getIsland().equals(island)){
                return true;
            }
        }
        return false;
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("'Le' dd/MM/yy 'à' HH:mm", Locale.FRENCH);

    public ChallengeAdminCommand(Challenges main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Perms permManager = new Perms(sender);
        if(permManager.hasPermission(Permission.CADMIN_COMMAND_USE)){
            if(args.length==0){
                sender.sendMessage("§9--- §cChallenges-Admin §9---");
                sender.sendMessage("§7Ici tu trouveras de l'aide pour configurer les challenges.");
                sender.sendMessage(" - §6/cadmin §e<enable>§6|§e<disable>§6|§e<statut>");
                sender.sendMessage(" - §6/cadmin reload §e<lang>§6|§e<config>");
                sender.sendMessage(" - §6/cadmin cat§e|§6cha §e<nom>");
                sender.sendMessage(" - §6/cadmin toggle cat§e|§6cha §e<nom>");
                sender.sendMessage(" - §6/cadmin island §e<joueur>");
                sender.sendMessage(" - §6/cadmin editor §e<new>§6|§e<kill [reason]>");
                sender.sendMessage("§9----------------------");
            }
            else{
                String sub = args[0];
                if(sub.equalsIgnoreCase("enable")||sub.equalsIgnoreCase("disable")){
                    if(permManager.hasPermission(Permission.CADMIN_TOGGLE_ALL)){
                        if(sub.equalsIgnoreCase("enable")){
                            if(!Challenges.Main.areChallengesEnabled()){
                                main.setChallengesEnabled(true, true);
                                sender.sendMessage("§2Les challenges sont maintenant §aactivés§2.");
                            }else{
                                sender.sendMessage("§cLes challenges sont déjà activés.");
                            }
                        }else{
                            if(Challenges.Main.areChallengesEnabled()){
                                long start = System.currentTimeMillis();
                                main.setChallengesEnabled(false, true);
                                sender.sendMessage("§2Les challenges sont maintenant §cdésactivés§2.");
                                sender.sendMessage("§2Les stockages d'île ont été déchargés. §2(§a" + (System.currentTimeMillis()-start) + "ms§2)");
                            }else{
                                sender.sendMessage("§cLes challenges sont déjà désactivés.");
                            }
                        }
                    }else{
                        sender.sendMessage(Perms.defaultNoPermMessage);
                    }
                }
                else if(sub.equalsIgnoreCase("statut")){
                    sender.sendMessage("§9--- §cChallenges-Statut §9---");
                    sender.spigot().sendMessage(new ComponentBuilder(" - §6Activés: ").append(new ComponentBuilder("§"+(Challenges.Main.areChallengesEnabled()?"aOui":"cNon")).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin "+(Challenges.Main.areChallengesEnabled()?"disable":"enable"))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour inverser l'état actuel").create())).create()).create());
                    sender.spigot().sendMessage(new ComponentBuilder(" - §6Catégories chargées: §e"+PluginStatisticsUtils.getLoadedCategoriesCount()+" ").append(new ComponentBuilder("§8(§a"+PluginStatisticsUtils.getActiveCategoriesCount()+" actives§8)").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PluginStatisticsUtils.getInactiveCategories()).create())).create()).create());
                    sender.spigot().sendMessage(new ComponentBuilder(" - §6Challenges chargés: §e"+PluginStatisticsUtils.getLoadedChallengesCount()+" ").append(new ComponentBuilder("§8(§a"+PluginStatisticsUtils.getActiveChallengesCount()+" actifs§8)").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PluginStatisticsUtils.getInactiveChallenges()).create())).create()).create());
                    sender.sendMessage(" - §6Joueurs chargés: §e"+ PluginStatisticsUtils.getLoadedPlayers());
                    sender.sendMessage(" - §6Îles chargées: §e"+ PluginStatisticsUtils.getLoadedIslandStorages());
                    sender.sendMessage(" - §6Îles à reset§e(0h)§6: §e"+ PluginStatisticsUtils.getIslandsToResetCount());
                    sender.sendMessage(" - §6Langues actives: "+ PluginStatisticsUtils.getLoadedLangs().toString().replace("[","§8[§e").replace(",","§6,§e").replace("]","§8]"));
                    sender.sendMessage(" - §6Session d'édition: "+ PluginStatisticsUtils.getCurrentEditorSession());
                    sender.sendMessage("§9----------------------");
                }
                else{
                    if(sub.equalsIgnoreCase("reload")||sub.equalsIgnoreCase("rl")){
                        if(args.length==1){
                            sender.sendMessage("§9--- §cChallenges-Reload §9---");
                            sender.sendMessage(" - §6/cadmin reload §e<lang>§6|§e<config>");
                            sender.sendMessage(" §ePermet de recharger les changements effectués dans les fichiers de langues ou la config globale.");
                            sender.sendMessage("§9-----------------------");
                        }else{
                            if(args[1].equalsIgnoreCase("lang")){
                                if(permManager.hasPermission(Permission.CADMIN_RELOAD_LANG)){
                                    Challenges.reloadLang();
                                    sender.sendMessage("§aLes fichiers de langues ont bien été rechargés.");
                                }else{
                                    sender.sendMessage(Perms.defaultNoPermMessage);
                                }
                            }else if(args[1].equalsIgnoreCase("config")){
                                if(permManager.hasPermission(Permission.CADMIN_RELOAD_CONFIG)){
                                    Challenges.reloadGlobalConfig();
                                    sender.sendMessage("§aLe fichier de configuration global a bien été rechargé.");
                                }else{
                                    sender.sendMessage(Perms.defaultNoPermMessage);
                                }
                            }
                        }
                    }
                    else if(sub.equalsIgnoreCase("toggle")){
                        if(args.length == 1 || args.length == 2){
                            sender.sendMessage("§9--- §cChallenges-Toggle §9---");
                            sender.sendMessage(" - §6/cadmin toggle cat §e<catégorie>");
                            sender.sendMessage(" - §6/cadmin toggle cha §e<challenge>");
                            sender.sendMessage(" §ePermet d'inverser l'état actuel d'un challenge ou d'une catégorie (Actif ou non)");
                            sender.sendMessage("§9-------------------------");
                        }else{
                            String c = StringUtils.buildString(args, 2);
                            if(args[1].equalsIgnoreCase("cat")){
                                if(permManager.hasPermission(Permission.CADMIN_TOGGLE_CATEGORY)){
                                    Optional<Category> optCat = Challenges.getManager().retrieveCategoryByName(c);
                                    if(optCat.isPresent()){
                                        Category cat = optCat.get();
                                        cat.setActive(!cat.isActive());
                                        cat.asChanged = true;
                                        sender.sendMessage("§eÉtat actuel de la catégorie §6" + cat.getName() + "§e: "+(cat.isActive()?"§aActivée":"§cDésactivée"));
                                    }else{
                                        sender.sendMessage("§4Erreur: §cLa catégorie §b"+c+" §cn'est pas reconnue.");
                                    }
                                }else{
                                    sender.sendMessage(Perms.defaultNoPermMessage);
                                }
                            }
                            else if(args[1].equalsIgnoreCase("cha")){
                                if(permManager.hasPermission(Permission.CADMIN_TOGGLE_CHALLENGE)){
                                    Optional<Challenge> optCha = Challenges.getManager().retrieveChallengeByName(c);
                                    if(optCha.isPresent()){
                                        Challenge cha = optCha.get();
                                        cha.setActive(!cha.isActive());
                                        cha.asChanged = true;
                                        sender.sendMessage("§eÉtat actuel du challenge §6" + cha.getName() + "§e: "+(cha.isActive()?"§aActivé":"§cDésactivé"));
                                    }else{
                                        sender.sendMessage("§4Erreur: §cLe challenge §b"+c+" §cn'est pas reconnu.");
                                    }
                                }else{
                                    sender.sendMessage(Perms.defaultNoPermMessage);
                                }
                            }else{
                                sender.sendMessage("§cSous-commande inconnue.");
                            }
                        }
                    }
                    else if(sub.equalsIgnoreCase("cat")||sub.equalsIgnoreCase("catégorie")||sub.equalsIgnoreCase("category")){
                        if(args.length==1){
                            sender.sendMessage("§9--- §cChallenges-Category §9---");
                            sender.sendMessage(" - §6/cadmin cat §e<catégorie>");
                            sender.sendMessage(" §eDonne des informations sur la catégorie");
                            sender.sendMessage("§9-------------------------");
                        }else{
                            String name = StringUtils.buildString(args, 1);
                            Category category = Challenges.getManager().retrieveCategoryByName(name).orElse(null);
                            if(category!=null){
                                sender.sendMessage("§9--- §cCatégorie-"+category.getName()+" §9---");
                                sender.spigot().sendMessage(uuidAsClickable(category.getUuid().toString()));
                                sender.sendMessage(" - §6Dernière modification: §e"+dateFormat.format(category.getLastEdited()));
                                ComponentBuilder txtToggleBase = new ComponentBuilder(" - §6Active: ");
                                BaseComponent[] txtToggle = new ComponentBuilder(category.isActive()?"§aOui":"§cNon").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin toggle cat "+category.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour inverser l'état actuel").create())).create();
                                txtToggleBase.append(txtToggle);
                                sender.spigot().sendMessage(txtToggleBase.create());
                                sender.sendMessage(" - §6Challenges;");
                                for(Challenge c : Challenges.getManager().getIndex().getChallenges(category)){
                                    sender.spigot().sendMessage(new ComponentBuilder("  - §e"+c.getName()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Clique pour voir les infos du challenge").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin cha "+c.getName())).create());
                                }
                                sender.sendMessage(" - §6Challenges requis;");
                                if(Challenges.getManager().getIndex().getRequiredChallenges(category).isEmpty()){
                                    sender.sendMessage("  - §cAucun");
                                }else{
                                    for(Challenge c : Challenges.getManager().getIndex().getRequiredChallenges(category)){
                                        sender.spigot().sendMessage(new ComponentBuilder("  - §e"+c.getName()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Clique pour voir les infos du challenge").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin cha "+c.getName())).create());
                                    }
                                }
                                sender.sendMessage("§9-----------------------");
                            }else{
                                sender.sendMessage("§4Erreur: §cLa catégorie §b"+name+" §cn'est pas reconnue.");
                            }
                        }
                    }
                    else if(sub.equalsIgnoreCase("cha")||sub.equalsIgnoreCase("challenge")){
                        if(args.length==1){
                            sender.sendMessage("§9--- §cChallenges-Challenge §9---");
                            sender.sendMessage(" - §6/cadmin cha §e<challenge>");
                            sender.sendMessage(" §eDonne des informations sur le challenge");
                            sender.sendMessage("§9-------------------------");
                        }else{
                            String name = StringUtils.buildString(args, 1);
                            Challenge challenge = Challenges.getManager().retrieveChallengeByName(name).orElse(null);
                            if(challenge!=null){
                                sender.sendMessage("§9--- §cChallenge-"+challenge.getName()+" §9---");
                                sender.spigot().sendMessage(uuidAsClickable(challenge.getUuid().toString()));
                                sender.sendMessage(" - §6Dernière modification: §e"+dateFormat.format(challenge.getLastEdited()));
                                ComponentBuilder txtCategoryBase = new ComponentBuilder(" - §6Catégorie: ");
                                BaseComponent[] txtCategory = new ComponentBuilder(challenge.getCategory()!=null?"§e"+challenge.getCategory().getName():"§cAucune").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (challenge.getCategory()!=null?"/cadmin cat "+challenge.getCategory().getName():"/cadmin cha "+challenge.getName()))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour accéder à la catégorie").create())).create();
                                txtCategoryBase.append(txtCategory);
                                sender.spigot().sendMessage(txtCategoryBase.create());
                                ComponentBuilder txtToggleBase = new ComponentBuilder(" - §6Actif: ");
                                BaseComponent[] txtToggle = new ComponentBuilder(challenge.isActive()?"§aOui":"§cNon").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin toggle cha "+challenge.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour inverser l'état actuel").create())).create();
                                txtToggleBase.append(txtToggle);
                                sender.spigot().sendMessage(txtToggleBase.create());
                                sender.sendMessage(" - §6Type: §e"+StringUtils.enumName(challenge.getType()));
                                sender.sendMessage(" - §6Peut être refait: "+(challenge.getRedoneLimit()==-1?"§eà l'infini":challenge.getRedoneLimit()==1?"§cNon":"§e"+challenge.getRedoneLimit()+" §6fois"));
                                sender.sendMessage(" - §6Complété §e"+Challenges.getManager().getStats().getStatistic(challenge).getCompleted() + " §6fois");
                                sender.sendMessage(" - §6Challenges requis;");
                                if(Challenges.getManager().getIndex().getRequiredChallenges(challenge).isEmpty()){
                                    sender.sendMessage("  - §cAucun");
                                }else{
                                    for(Challenge c : Challenges.getManager().getIndex().getRequiredChallenges(challenge)){
                                        sender.spigot().sendMessage(new ComponentBuilder("  - §e"+c.getName()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Clique pour voir les infos du challenge").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cadmin cha "+c.getName())).create());
                                    }
                                }
                                sender.sendMessage("§9-----------------------");
                            }else{
                                sender.sendMessage("§4Erreur: §cLe challenge §b"+name+" §cn'est pas reconnu.");
                            }
                        }
                    }
                    else if(sub.equalsIgnoreCase("island")||sub.equalsIgnoreCase("is")){
                        if(args.length==1){
                            sender.sendMessage("§9--- §cChallenges-Island §9---");
                            sender.sendMessage(" - §6/cadmin island §e<joueur>");
                            sender.sendMessage(" §ePermet de voir l'avancement de l'île du joueur ainsi que de le modifier via un inventaire.");
                            sender.sendMessage("§9-----------------------");
                        }else{
                            //only for command sender (can be executed on "command" rewards.) /cadmin island acs world player state
                            if(sender instanceof ConsoleCommandSender&&args.length==5&&args[1].equalsIgnoreCase("acs")){
                                try{
                                    Storage.AccessType type = Storage.AccessType.valueOf(args[2].toUpperCase());
                                    OfflinePlayer op = Bukkit.getOfflinePlayer(args[3]);
                                    if(op.hasPlayedBefore()){
                                        UUID island = Challenges.getFabledApi().getIslandUUID(op);
                                        if(island!=null){
                                            Optional<Storage> optStorage = Challenges.getManager().retrieveStorageByUUID(island);
                                            Storage storage;
                                            if(optStorage.isPresent()){//fonctionne ps avec le orElse
                                                storage = optStorage.get();
                                            }else storage = Challenges.getManager().loadStorage(island);
                                            storage.giveAccess(type, Boolean.parseBoolean(args[4]));//parse accepts all for false and only TrUe for true
                                            Challenges.getManager().unloadStorageSafe(storage.getIsland());
                                        }
                                    }
                                }catch(Exception e){
                                    System.err.println("Failed to give/revoke access to world " + args[2].toUpperCase() + " for player "+args[3]);
                                    e.printStackTrace();
                                }
                                return false;
                            }
                            if(permManager.hasPermission(Permission.CADMIN_EDITOR_ISLAND)){
                                if(Challenges.getFabledApi()!=null){
                                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                                    UUID island = Challenges.getFabledApi().getIslandUUID(player);
                                    if(island!=null){
                                        if(sender instanceof Player){
                                            Player admin = (Player)sender;
                                            IslandInventory manager = getIslandSession(admin);
                                            if(manager!=null){
                                                deleteIslandSession(admin.getUniqueId());
                                            }
                                            manager = createIslandSession(admin, island, player.getName());
                                            if(manager==null||!manager.isUsable()){
                                                admin.closeInventory();
                                                admin.sendMessage("§4Erreur: §cLa session n'a pas pu être créé! Veuillez réessayer ultérieurement.");
                                                deleteIslandSession(admin.getUniqueId());
                                            }
                                        }else{
                                            sender.sendMessage("§4Erreur: §cCette commande est réservée aux joueurs !");
                                        }
                                    }else{
                                        sender.sendMessage("§4Erreur: §cLe joueur §b"+player.getName()+" §cn'a pas d'île pour l'instant!");
                                    }
                                }else{
                                    sender.sendMessage("§4Erreur: §cCommande refusée! L'API FabledSkyBlock ne répond pas.");
                                }
                            }else{
                                sender.sendMessage(Perms.defaultNoPermMessage);
                            }
                        }
                    }
                    else if(sub.equalsIgnoreCase("editor")){
                        if(args.length==1){
                            sender.sendMessage("§9--- §cChallenges-Editor §9---");
                            sender.sendMessage(" - §6/cadmin editor §e<new>§6|§e<kill [reason]>");
                            sender.sendMessage(" §ePermet de créer une session d'édition ou d'en supprimer une (raison optionelle)");
                            sender.sendMessage("§9-----------------------");
                        }else{
                            if(permManager.hasPermission(Permission.CADMIN_EDITOR_APP)){
                                if(args[1].equalsIgnoreCase("new")){
                                    if(sender instanceof Player){
                                        Player owner = (Player)sender;
                                        if(Challenges.getFabledApi()!=null){
                                            if(!createSession(owner)){
                                                owner.sendMessage("§cUne session existe déjà. /cadmin statut pour plus d'informations!");
                                            }
                                        }else{
                                            sender.sendMessage("§cIndisponible pour le moment.");
                                        }
                                    }else{
                                        sender.sendMessage("§cSeuls les joueurs connectés peuvent créer une nouvelle session et y accéder.");
                                    }
                                }
                                else if(args[1].equalsIgnoreCase("kill")){
                                    SessionManager editor = Challenges.getEditor();
                                    if(editor!=null&&editor.getCurrent()!=null){
                                        String reason = "Session stoppée par "+sender.getName()+".";
                                        editor.stopSession(reason+(args.length>2?" ("+StringUtils.buildString(args,2)+")":""));
                                        sender.sendMessage("§aLa session courante va être stoppée.");
                                    }else{
                                        sender.sendMessage("§cAucune session active n'a pu être trouvée.");
                                    }
                                }
                            }else{
                                sender.sendMessage(Perms.defaultNoPermMessage);
                            }
                        }
                    }
                }
            }
        }else{
            sender.sendMessage(Perms.defaultNoPermMessage);
        }
        return false;
    }

    private BaseComponent[] uuidAsClickable(String uuid){
        ComponentBuilder txtBase = new ComponentBuilder(" - §6UUID: ");
        BaseComponent[] txtUuid = new ComponentBuilder("§e"+uuid)
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,uuid))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour copier").create())).create();
        txtBase.append(txtUuid);
        return txtBase.create();
    }

    public static void unloadSessions(){
        for(UUID islandsSession : getIslandSessions().keySet()){
            deleteIslandSession(islandsSession);
        }
    }

    private boolean createSession(Player p){
        if(Challenges.getEditor().createSession(p)){
            String k = Challenges.getEditor().getCurrent().getKey();
            ComponentBuilder msg = new ComponentBuilder();
            msg.append("§aSession crée! Tu as §7" + (Challenges.getGlobalConfig().getEditorWaitingAttemps()*0.5) + " §asecondes pour y accéder avant qu'elle ne soit supprimée!\n§7Clé: §r");
            msg.append(new ComponentBuilder("§e"+k).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Clique pour copier").create())).event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,k)).create());
            p.spigot().sendMessage(msg.create());
            return true;
        }
        return false;
    }
}
