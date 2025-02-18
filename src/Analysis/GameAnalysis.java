package Analysis;

public class GameAnalysis {
    private Double[] l2Dist;
    private Double expUtility;

    public GameAnalysis(Double[] l2Dist, Double expUtility) {
        this.l2Dist = l2Dist;
        this.expUtility = expUtility;
    }

    public Double[] getL2Dist() {
        return l2Dist;
    }

    public Double getExpUtility() {
        return expUtility;
    }
}
