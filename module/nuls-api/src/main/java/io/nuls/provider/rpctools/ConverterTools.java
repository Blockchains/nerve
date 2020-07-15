package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.model.dto.VirtualBankDirectorDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Component
public class ConverterTools implements CallRpc {


    /**
     * 查询异构链地址
     */
    public Result getHeterogeneousAddress(int chainId, int heterogeneousChainId, String packingAddress) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("chainId", chainId);
        params.put("heterogeneousChainId", heterogeneousChainId);
        params.put("packingAddress", packingAddress);
        try {
            return callRpc(ModuleE.CV.abbr, "cv_get_heterogeneous_address", params, (Function<Map<String, Object>, Result>) res -> new Result(res));
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 虚拟银行成员信息
     * @param chainId
     * @return
     */
    public Result<List<VirtualBankDirectorDTO>> getVirtualBankInfo(int chainId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("chainId", chainId);
        params.put("balance", true);
        try {
            return callRpc(ModuleE.CV.abbr, "cv_virtualBankInfo", params, (Function<Map<String, Object>, Result<List<VirtualBankDirectorDTO>>>) res -> {
                if(res == null){
                    return new Result();
                }
                return new Result(res.get("list"));
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
