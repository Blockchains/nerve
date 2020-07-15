/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2020 nerve.network
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.nerve.converter.core.business.impl;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import network.nerve.converter.constant.ConverterConstant;
import network.nerve.converter.core.business.HeterogeneousService;
import network.nerve.converter.core.heterogeneous.docking.interfaces.IHeterogeneousChainDocking;
import network.nerve.converter.core.heterogeneous.docking.management.HeterogeneousDockingManager;
import network.nerve.converter.model.bo.Chain;
import network.nerve.converter.storage.PersistentCacheStroageService;

import static network.nerve.converter.constant.ConverterDBConstant.*;

/**
 * @author: Loki
 * @date: 2020/3/18
 */
@Component
public class HeterogeneousServiceImpl implements HeterogeneousService {



    @Autowired
    private HeterogeneousDockingManager heterogeneousDockingManager;
    @Autowired
    private PersistentCacheStroageService persistentCacheStroageService;

    /**
     * 判断是否需要组装当前网络的主资产补贴异构链交易手续费
     * 异构链是合约类型,并且提现资产不是异构链主资产,才收取当前网络主资产作为手续费补贴
     * @param heterogeneousChainId
     * @param heterogeneousAssetId
     * @return
     */
    @Override
    public boolean isAssembleCurrentAssetFee(int heterogeneousChainId, int heterogeneousAssetId) throws NulsException {
        IHeterogeneousChainDocking heterogeneousDocking = heterogeneousDockingManager.getHeterogeneousDocking(heterogeneousChainId);
        return heterogeneousDocking.isSupportContractAssetByCurrentChain()
                && heterogeneousAssetId != ConverterConstant.ALL_MAIN_ASSET_ID;
    }

    @Override
    public boolean saveExeHeterogeneousChangeBankStatus(Chain chain, Boolean status) {
        chain.getHeterogeneousChangeBankExecuting().set(status);
        return persistentCacheStroageService.saveCacheState(chain, EXE_HETEROGENEOUS_CHANGE_BANK_KEY, status ? 1 : 0);
    }

    @Override
    public boolean saveExeDisqualifyBankProposalStatus(Chain chain, Boolean status) {
        chain.getExeDisqualifyBankProposal().set(status);
        return persistentCacheStroageService.saveCacheState(chain, EXE_DISQUALIFY_BANK_PROPOSAL_KEY, status ? 1 : 0);
    }

    @Override
    public boolean saveResetVirtualBankStatus(Chain chain, Boolean status) {
        chain.getResetVirtualBank().set(status);
        return persistentCacheStroageService.saveCacheState(chain, RESET_VIRTUALBANK_KEY, status ? 1 : 0);
    }
}
