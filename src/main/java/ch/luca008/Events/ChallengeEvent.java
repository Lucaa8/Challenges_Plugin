package ch.luca008.Events;

import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Required.CompletableResult;
import ch.luca008.UniPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;

public class ChallengeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private CompletableResult result;

    public ChallengeEvent(CompletableResult result){
        this.result = result;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCompleted(){
        return result.isCompleted();
    }

    public UniPlayer getPlayer(){
        return result.getPlayer();
    }

    public Challenge getChallenge(){
        return result.getChallenge();
    }

    public Object getMessage(){
        return result.getMessage();
    }

    public void execute(){
        Optional<Storage> s = getPlayer().getIslandStorage();
        if(s.isPresent()){
            Storage storage = s.get();
            Storage.ChallengeStorage challengeStorage = storage.getStorage(getChallenge().getUuid());
            challengeStorage.addCompletion();
            challengeStorage.setCompletable(null);
            if(getChallenge().getType()== Challenge.ChallengeType.STAT) storage.setStatisticChallengeActive(null);
            getPlayer().addChallengeCompletion(getChallenge().getUuid(), 1);
            getChallenge().reward(getPlayer());
        }
    }

}
