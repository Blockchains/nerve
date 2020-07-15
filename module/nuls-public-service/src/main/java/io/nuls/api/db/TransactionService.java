package io.nuls.api.db;

import io.nuls.api.model.po.*;
import io.nuls.api.model.po.mini.MiniCrossChainTransactionInfo;
import io.nuls.api.model.po.mini.MiniTransactionInfo;

import java.util.List;
import java.util.Set;

public interface TransactionService {

    void saveTxList(int chainId, List<TransactionInfo> txList);

    void saveCoinDataList(int chainId, List<CoinDataInfo> coinDataList);

    void saveTxRelationList(int chainId, Set<TxRelationInfo> relationInfos);

    PageInfo<MiniCrossChainTransactionInfo> getCrossChainTxList(int chainId, String address, int pageIndex, int pageSize,int... types);

    PageInfo<MiniTransactionInfo> getInnerChainTxList(int chainId, String address, int pageIndex, int pageSize, boolean isHidden, int... typeList);

//    PageInfo<MiniTransactionInfo> getTxList(int chainId, int pageIndex, int pageSize, boolean isHidden, int... type);

    List<TxHexInfo> getUnConfirmList(int chainId);

    void deleteTxs(int chainId);

    PageInfo<MiniTransactionInfo> getBlockTxList(int chainId, int pageIndex, int pageSize, long blockHeight, int type);

    TransactionInfo getTx(int chainId, String txHash);

    void rollbackTxRelationList(int chainId, Set<TxRelationInfo> relationInfos);

    void rollbackTx(int chainId, List<String> txHashList);

    void saveUnConfirmTx(int chainId, TransactionInfo tx, String txHex);

    void deleteUnConfirmTx(int chainId, String txHash);
}
