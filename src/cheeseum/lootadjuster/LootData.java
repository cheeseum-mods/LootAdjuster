package cheeseum.lootadjuster;

public class LootData {
    int itemId;
    int itemMeta;
    int minFreq;
    int maxFreq;

    public LootData (int itemId, int itemMeta, int minFreq, int maxFreq) {
        this.itemId = itemId;
        this.itemMeta = itemMeta;
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;
    }
}
