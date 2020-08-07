package io.nuls.api.task;

import io.nuls.api.ApiContext;
import io.nuls.api.manager.HeterogeneousChainAssetBalanceManager;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.converter.ConverterService;
import io.nuls.base.api.provider.converter.facade.GetVirtualBankInfoReq;
import io.nuls.base.api.provider.converter.facade.VirtualBankDirectorDTO;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020-06-23 14:55
 * @Description: 功能描述
 */
public class QueryHeterogeneousChainBalanceTask implements Runnable {

    ConverterService converterService = ServiceManager.get(ConverterService.class);

    @Override
    public void run() {
        HeterogeneousChainAssetBalanceManager manager = SpringLiteContext.getBean(HeterogeneousChainAssetBalanceManager.class);
        Map<Integer, Map<String,BigDecimal>> balanceList = manager.getBalanceList();
        Log.info("查询虚拟银行节点异构资产余额");
        try{
            Result<VirtualBankDirectorDTO> virtualBankDirectorDTOResult = converterService.getVirtualBankInfo(new GetVirtualBankInfoReq());
            if(virtualBankDirectorDTOResult.isFailed()){
                Log.error("查询虚拟银行资产信息失败:{}:{}",virtualBankDirectorDTOResult.getStatus(),virtualBankDirectorDTOResult.getMessage());
            }
            if(virtualBankDirectorDTOResult.getList() != null && !virtualBankDirectorDTOResult.getList().isEmpty()){
                virtualBankDirectorDTOResult.getList().forEach(bank->{
                    if(bank.getHeterogeneousAddresses() != null &&  !bank.getHeterogeneousAddresses().isEmpty()){
                        bank.getHeterogeneousAddresses().forEach(address->{
                            balanceList.putIfAbsent(address.getChainId(),new HashMap<>());
                            balanceList.get(address.getChainId()).put(address.getAddress(),new BigDecimal(address.getBalance()).setScale(ApiContext.defaultDecimals, RoundingMode.HALF_DOWN));
                            Log.info("{}地址:{},余额:{}",address.getSymbol(),address.getAddress(),address.getBalance());
                        });
                    }else{
                        Log.warn("地址:{} 未获取到异构链地址",bank.getAgentAddress());
                    }
                });
            }else{
                Log.warn("未获取到银行节点列表");
            }
        }catch (Throwable e){
            Log.error("查询虚拟银行资产余额，出现异常",e);
        }
    }


}
