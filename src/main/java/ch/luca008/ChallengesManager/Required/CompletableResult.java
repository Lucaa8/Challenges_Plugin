package ch.luca008.ChallengesManager.Required;

import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.UniPlayer;

public interface CompletableResult {

    public boolean isCompleted();

    public boolean hasProgressed();

    public Object getMessage(); //Object permet de retourner un textcomponent si besoin

    public UniPlayer getPlayer();

    public Challenge getChallenge();

}
