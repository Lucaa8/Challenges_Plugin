package ch.luca008.Commands;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.CompletableResult;
import ch.luca008.Events.ChallengeEvent;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.Perms;
import ch.luca008.Utils.Perms.Permission;
import ch.luca008.Utils.SkyblockUtils;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ChallengeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Perms permManager = new Perms(sender);
        if(permManager.isPlayer()&&permManager.hasPermission(Permission.CHALLENGE_USE_COMMAND)){
            Player p = permManager.getPlayer();
            UniPlayer uniPlayer = Challenges.getManager().retrieveUniPlayerByUUID(p.getUniqueId());
            if(args.length==2&&args[0].equalsIgnoreCase("lang")){
                String lang = args[1].toUpperCase();
                if(lang.equalsIgnoreCase("reset")){
                    uniPlayer.resetLang();
                    uniPlayer.sendMessage("Lang-Reset");
                    return false;
                }
                if(Challenges.getLangManager().doLangExist(lang)){
                    uniPlayer.changeLang(lang, true);
                    uniPlayer.sendMessage("Lang-Changed");
                }else{
                    uniPlayer.sendMessage("Lang-Unknown");
                }
                return false;
            }
            if(!Challenges.Main.areChallengesEnabled()){
                if(!permManager.hasPermission(Permission.CHALLENGE_ADMIN_BYPASS)){
                    uniPlayer.sendMessage("Challenges-Disabled");
                    return false;
                }
            }
            if(!SkyblockUtils.hasIsland(p.getUniqueId())){
                uniPlayer.sendMessage("Challenges-No-Island");
                return false;
            }
            if(!SkyblockUtils.isOnIsland(p)){
                uniPlayer.sendMessage("Challenges-Not-On-Island");
                return false;
            }
            if(args.length==0){
                checkLoad(uniPlayer.getIsland().get().getUniqueId());
                uniPlayer.openMainMenuInventory();
            }
            else {
                if(args[0].equalsIgnoreCase("c")||args[0].equalsIgnoreCase("complete")){
                    if(permManager.hasPermission(Permission.CHALLENGE_USE_COMPLETE)){
                        if(args.length==1){
                            sender.sendMessage("§c/challenge complete <challenge>\n/c c <challenge>");
                        }else{
                            String name = StringUtils.buildString(args, 1);
                            Challenge c = Challenges.getManager().retrieveChallengeByName(name).orElse(null);
                            if(c!=null){
                                CompletableResult result = c.complete(uniPlayer);
                                if(result!=null) Bukkit.getServer().getPluginManager().callEvent(new ChallengeEvent(result));
                            }else{
                                uniPlayer.sendMessage("Command-Challenge-Not-Found", Map.entry("{0}",name));
                            }
                        }
                    }else{
                        uniPlayer.sendMessage("Command-Challenge-Completion-No-Perm");
                    }
                }
                else if(args[0].equalsIgnoreCase("cancel")){
                    if(permManager.hasPermission(Permission.CHALLENGE_USE_CANCEL)){
                        if(args.length!=2||!args[1].equals("CONFIRM")){
                            sender.sendMessage("§c/challenge cancel CONFIRM\n/c cancel CONFIRM");
                        }else{
                            Storage str = checkLoad(uniPlayer.getIsland().get().getUniqueId());//bug
                            Optional<Challenge> optStarted = str.getActiveStatChallenge();
                            if(optStarted.isPresent()){
                                Challenge c = optStarted.get();
                                c.reset(str.getIsland());
                                uniPlayer.sendMessage("Command-Challenge-Stat-Cancelled", Map.entry("{0}",c.getName()));
                                return true;
                            }else{
                                uniPlayer.sendMessage("Command-Challenge-Cant-Cancel");
                            }
                        }
                    }else{
                        uniPlayer.sendMessage("Command-Challenge-Cancel-No-Perm");
                    }
                }
                else{
                    String name = StringUtils.buildString(args, 0);
                    Category c = Challenges.getManager().retrieveCategoryByName(name).orElse(null);
                    if(c!=null){
                        if(c.isActive()||permManager.hasPermission(Permission.CHALLENGE_ADMIN_BYPASS)){
                            Storage storage = checkLoad(uniPlayer.getIsland().get().getUniqueId());
                            List<Challenge> requiredCat = storage.isUnlocked(c);
                            if (requiredCat.isEmpty()) {
                                uniPlayer.openCategory(c);
                            }else{
                                p.spigot().sendMessage(Challenge.getMissingChallenges(requiredCat, uniPlayer.getMessage("Challenge-Completion-Category-Not-Unlocked", Map.entry("{0}",c.getName())), uniPlayer));
                            }
                        }else{
                            uniPlayer.sendMessage("Command-Category-Open-Not-Active");
                        }
                    }else{
                        uniPlayer.sendMessage("Command-Category-Not-Found", Map.entry("{0}",name));
                    }
                }
            }
        }else{
            if(!(sender instanceof Player)){
                sender.sendMessage("§cTu dois être un joueur pour exécuter cette commande!");
            }else{
                sender.sendMessage(Perms.defaultNoPermMessage);
            }
        }
        return false;
    }

    private Storage checkLoad(UUID island){
        Manager m = Challenges.getManager();
        if(!m.isStorageLoaded(island)){
            return m.loadStorage(island);
        }
        return m.retrieveStorageByUUID(island).get();
    }
}
