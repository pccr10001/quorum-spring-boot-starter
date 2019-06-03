package org.web3j.spring.actuate;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.util.Assert;
import org.web3j.protocol.Web3j;
import org.web3j.quorum.Quorum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Health check indicator for Web3j
 */
public class QuorumHealthIndicator extends AbstractHealthIndicator {

    private Quorum quorum;

    public QuorumHealthIndicator(Quorum quorum) {
        Assert.notNull(quorum, "Quorum must not be null");
        this.quorum = quorum;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            boolean listening = quorum.netListening().send().isListening();
            if (!listening) {
                builder.down();
            } else {
                builder.up();
                List<CompletableFuture> futures = new ArrayList<>();

                futures.add(quorum.netVersion()
                        .sendAsync()
                        .thenApply(netVersion ->
                                builder.withDetail("netVersion", netVersion.getNetVersion())));

                futures.add(quorum.web3ClientVersion()
                        .sendAsync()
                        .thenApply(web3ClientVersion ->
                                builder.withDetail("clientVersion", web3ClientVersion.getWeb3ClientVersion())));

                futures.add(quorum.ethBlockNumber()
                        .sendAsync()
                        .thenApply(ethBlockNumber ->
                                builder.withDetail("blockNumber", ethBlockNumber.getBlockNumber())));

                futures.add(quorum.ethProtocolVersion()
                        .sendAsync()
                        .thenApply(ethProtocolVersion ->
                                builder.withDetail("protocolVersion", ethProtocolVersion.getProtocolVersion())));

                futures.add(quorum.netPeerCount()
                        .sendAsync()
                        .thenApply(netPeerCount ->
                                builder.withDetail("netPeerCount", netPeerCount.getQuantity())));

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
            }

        } catch (Exception ex) {
            builder.down(ex);
        }
    }
}
