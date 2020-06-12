/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package network.nerve.pocnetwork.model;

import network.nerve.pocbft.constant.ConsensusConstant;
import network.nerve.pocbft.model.bo.Chain;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.model.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lanjinsheng
 * @date 2019/10/17
 * @description
 */
public class ConsensusNetGroup {
    boolean available = false;
    private int chainId;
    /**
     * KEY 用地址
     */
    private Map<String, ConsensusNet> group = new ConcurrentHashMap<>();

    private boolean allConnected = false;

    public ConsensusNetGroup(int chainId) {
        this.chainId = chainId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public Map<String, ConsensusNet> getGroup() {
        return group;
    }

    public void setGroup(Map<String, ConsensusNet> group) {
        this.group = group;
    }

    /**
     * 当前节点启动后连接上所有未连接的节点则表示该节点共识网络组网成功
     * ，之后将不再广播自己的节点信息给其他新增的共识节点
     * */
    public boolean isAllConnected() {
        if(!allConnected){
            for (ConsensusNet consensusNet : group.values()){
                if(!consensusNet.isHadConnect()){
                    return false;
                }
            }
        }
        this.allConnected = true;
        return true;
    }

    public void setAllConnected(boolean allConnected) {
        this.allConnected = allConnected;
    }

    public void addConsensus(ConsensusNet consensusNet) {
        if (StringUtils.isBlank(consensusNet.getAddress())) {
            consensusNet.setAddress(AddressTool.getStringAddressByBytes(AddressTool.getAddress(consensusNet.getPubKey(), chainId)));
        }
        group.put(consensusNet.getAddress(), consensusNet);

    }

    public List<ConsensusNet> getConsensusHadConnectNetList() {
        List<ConsensusNet> list = new ArrayList<>();
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getNodeId() && entry.getValue().isHadConnect()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public List<ConsensusNet> getAllConsensusConnectNetList() {
        List<ConsensusNet> list = new ArrayList<>();
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getPubKey()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public List<ConsensusNet> getUnConnectConsensusNetList() {
        List<ConsensusNet> list = new ArrayList<>();
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getNodeId() && !entry.getValue().isHadConnect()) {
                //允许进行重连的节点
                if (entry.getValue().getFailTimes() < ConsensusConstant.POC_CONNECT_MAX_FAIL_TIMES) {
                    list.add(entry.getValue());
                }
            }
        }
        return list;
    }

    public boolean hadNullNodeId() {
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null == entry.getValue().getNodeId()) {
                return true;
            }
        }
        return false;
    }

    public ConsensusNet getConsensusNet(String address) {
        return group.get(address);
    }

    public ConsensusNet getConsensusNet(byte[] pubKey) {
        String address = AddressTool.getStringAddressByBytes(AddressTool.getAddress(pubKey, chainId));
        return group.get(address);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean statusChange(int percent, Chain chain) {
        int total = group.size();
        int hadConnect = 0;
        boolean netAvailable = false;
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getNodeId() && entry.getValue().isHadConnect()) {
                hadConnect++;
            }
        }
        if (total > 0) {
            //增加自身节点
            total = total + 1;
            hadConnect = hadConnect + 1;
            int connectPercent = (int) (hadConnect * 100 / total);
            if (connectPercent >= percent) {
                netAvailable = true;
            }
        } else {
            //单节点直接返回
            netAvailable = true;
        }
        if (netAvailable == available) {
            return false;
        } else {
            chain.getLogger().info("net state  change total={} hadConnect={},{}==to=={}", total, hadConnect, available, netAvailable);
            available = netAvailable;
            return true;
        }
    }

    public List<String> getConsensusNetIps() {
        List<String> ips = new ArrayList<>();
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getNodeId()) {
                try {
                    ips.add(entry.getValue().getNodeId().split(":")[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ips;
    }

    public void reCalConsensusNet(Map<String, Integer> map) {
        boolean allDisconnect = false;
        if (map.size() == 0) {
            allDisconnect = true;
        }
        for (Map.Entry<String, ConsensusNet> entry : group.entrySet()) {
            if (null != entry.getValue().getNodeId()) {
                try {
                    if (allDisconnect) {
                        //全部连接不可用
                        entry.getValue().setHadConnect(false);
                    } else if (null == map.get(entry.getValue().getNodeId().split(":")[0])) {
                        //部分连接不可用，判断次数超限直接干掉IP
                        if (entry.getValue().getFailTimes() >= ConsensusConstant.POC_CONNECT_MAX_FAIL_TIMES) {
                            entry.getValue().setNodeId(null);
                        } else {
                            entry.getValue().setFailTimes((entry.getValue().getFailTimes() + 1));
                        }
                        entry.getValue().setHadConnect(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String removeConsensus(String address) {
        ConsensusNet consensusNet = group.get(address);
        if (null == consensusNet) {
            return null;
        }
        group.remove(address);
        return consensusNet.getNodeId();
    }
}
