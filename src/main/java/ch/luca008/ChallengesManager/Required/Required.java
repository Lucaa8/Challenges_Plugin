package ch.luca008.ChallengesManager.Required;

import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.UniPlayer;
import org.json.simple.JSONObject;

public interface Required {

    public enum RequiredType{
        Items(Challenge.ChallengeType.INVENTORY),
        Island(Challenge.ChallengeType.ISLAND),
        Stats(Challenge.ChallengeType.STAT),
        Others(Challenge.ChallengeType.OTHER);

        private final Challenge.ChallengeType type;
        RequiredType(Challenge.ChallengeType type){
            this.type=type;
        }
        public boolean match(Challenge.ChallengeType challengeType){
            return type==challengeType;
        }
    }

    public JSONObject toJson();

    public String toLore(Storage.ChallengeStorage storage, UniPlayer player);

    @Override
    public String toString();

    public RequiredType getType();

    public CompletableResult complete(Challenge c, UniPlayer p);

}
