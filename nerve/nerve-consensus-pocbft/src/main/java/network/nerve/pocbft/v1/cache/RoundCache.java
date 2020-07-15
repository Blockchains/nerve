package network.nerve.pocbft.v1.cache;

import io.nuls.core.log.Log;
import network.nerve.pocbft.model.bo.round.MeetingRound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eva
 */
public class RoundCache {

    private MeetingRound currentRound;

    private List<String> keyList = new ArrayList<>();
    private Map<String, MeetingRound> roundMap = new HashMap<>();

    public MeetingRound get(String key) {
        return roundMap.get(key);
    }

    public void put(String key, MeetingRound round) {
        this.roundMap.put(key, round);
        keyList.add(key);
        if (keyList.size() > 50) {
            String oldKey = keyList.remove(0);
            roundMap.remove(oldKey);
        }
    }

    public MeetingRound getCurrentRound() {
        return currentRound;
    }

    public void switchRound(MeetingRound round) {
        this.currentRound = round;
    }

    public void clear() {
        this.roundMap.clear();
    }

    public MeetingRound getRoundByIndex(long roundIndex) {
        for (Map.Entry<String, MeetingRound> entry : roundMap.entrySet()) {
            if (entry.getValue().getIndex() == roundIndex) {
                return entry.getValue();
            }
        }
//        找不到的情况，看看里面都存了啥
        for (Map.Entry<String, MeetingRound> entry : roundMap.entrySet()) {
            Log.info(entry.getKey());
        }
        return null;
    }
}
