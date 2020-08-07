/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package network.nerve.quotation.rpc.querier.impl;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;
import network.nerve.quotation.model.bo.Chain;
import network.nerve.quotation.rpc.querier.Querier;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static network.nerve.quotation.constant.QuotationConstant.TIMEOUT_MILLIS;

/**
 * 去DEX获取价格
 *
 * @author: Loki
 * @date: 2020/6/16
 */
@Component
public class DexQuerier implements Querier {

    /**
     * 获取交易对价格接口
     */
    private final String CMD = "/coin/trading/price/";

    @Override
    public BigDecimal tickerPrice(Chain chain, String baseurl, String anchorToken) {
        String symbol = anchorToken.toUpperCase();
        String url = baseurl + CMD + symbol;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                    .build();
            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                chain.getLogger().error("Bitz 调用{}接口, 获取价格失败, anchorToken:{}, status:{}",
                        url,
                        anchorToken,
                        response.statusCode());
                return null;
            }
            Map<String, Object> body = JSONUtils.json2map(response.body());
            Map data = (Map) body.get("data");
            boolean success = (Boolean) data.get("success");
            if (!success) {
                chain.getLogger().warn("Dex没有获取到交易对[{}]价格", symbol);
                return null;
            }
            Map result = (Map) data.get("result");
            BigDecimal res = new BigDecimal(result.get("price").toString());
            chain.getLogger().info("Dex 获取到交易对[{}]价格:{}", symbol, res);
            return res;
        } catch (HttpConnectTimeoutException e) {
            chain.getLogger().error("Dex, 调用接口 {}, anchorToken:{} 超时", url, anchorToken);
            return null;
        } catch (Throwable e) {
            chain.getLogger().error("Dex, 调用接口 {}, anchorToken:{} 获取价格失败", url, anchorToken);
            return null;
        }
    }
}
