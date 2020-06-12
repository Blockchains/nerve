package network.nerve.dex.model.bean;

public class AssetInfo {

    private int assetChainId;

    private int assetId;

    private int decimal;

    private String symbol;

    private int status;

    public AssetInfo() {

    }

    public AssetInfo(int assetChainId, int assetId, int decimal) {
        this.assetChainId = assetChainId;
        this.assetId = assetId;
        this.decimal = decimal;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getKey() {
        return this.assetChainId + "-" + this.assetId;
    }

    public static String toKey(int assetChainId, int assetId) {
        return assetChainId + "-" + assetId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetInfo)) return false;

        AssetInfo assetInfo = (AssetInfo) o;

        if (assetChainId != assetInfo.assetChainId) return false;
        return assetId == assetInfo.assetId;
    }

    @Override
    public int hashCode() {
        int result = assetChainId;
        result = 31 * result + assetId;
        return result;
    }
}
