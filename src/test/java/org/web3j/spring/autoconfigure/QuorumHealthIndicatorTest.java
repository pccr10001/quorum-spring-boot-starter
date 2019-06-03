package org.web3j.spring.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.quorum.Quorum;
import org.web3j.spring.autoconfigure.context.SpringApplicationTest;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringApplicationTest.class)
public class QuorumHealthIndicatorTest {


    @Autowired
    HealthIndicator quorumHealthIndicator;

    @Autowired
    Quorum quorum;

    @Test
    public void testHealthCheckIndicatorDown() throws Exception {
        mockWeb3jCalls(false, null, null, null, null, null);
        Health health = quorumHealthIndicator.health();
        assertThat(health.getStatus(), equalTo(Status.DOWN));

    }

    @Test
    public void testHealthCheckIndicatorUp() throws Exception {


        mockWeb3jCalls(true, "23", "ClientVersion",
                new BigInteger("120"), "protocolVersion", new BigInteger("80"));


        Health health = quorumHealthIndicator.health();
        assertThat(health.getStatus(), equalTo(Status.UP));
        assertThat(health.getDetails().get("netVersion"), equalTo("23"));
        assertThat(health.getDetails().get("clientVersion"), equalTo("ClientVersion"));
        assertThat(health.getDetails().get("blockNumber"), equalTo(new BigInteger("120")));
        assertThat(health.getDetails().get("protocolVersion"), equalTo("protocolVersion"));
        assertThat(health.getDetails().get("netPeerCount"), equalTo(new BigInteger("80")));

    }

    private void mockWeb3jCalls(boolean isListening, String netVersion, String clientVersion,
                                BigInteger blockNumber, String protocolVersion, BigInteger netPeer) throws Exception {

        Mockito.when(quorum.netListening().send().isListening()).thenReturn(isListening);
        if (netVersion != null) {
            Mockito.when(quorum.netVersion().sendAsync()).thenReturn(supplyAsync(() -> {
                NetVersion netVersionObject = new NetVersion();
                netVersionObject.setResult(netVersion);
                return netVersionObject;
            }));
        }
        if (clientVersion != null) {
            Mockito.when(quorum.web3ClientVersion().sendAsync()).thenReturn(supplyAsync(() -> {
                Web3ClientVersion web3ClientVersion = new Web3ClientVersion();
                web3ClientVersion.setResult(clientVersion);
                return web3ClientVersion;
            }));
        }
        if (blockNumber != null) {
            Mockito.when(quorum.ethBlockNumber().sendAsync()).thenReturn(supplyAsync(() -> {
                EthBlockNumber ethBlockNumber = new EthBlockNumber();
                ethBlockNumber.setResult(Numeric.encodeQuantity(blockNumber));
                return ethBlockNumber;
            }));
        }
        if (protocolVersion != null) {
            Mockito.when(quorum.ethProtocolVersion().sendAsync()).thenReturn(supplyAsync(() -> {
                EthProtocolVersion ethProtocolVersion = new EthProtocolVersion();
                ethProtocolVersion.setResult(protocolVersion);
                return ethProtocolVersion;
            }));
        }
        if (netPeer != null) {
            Mockito.when(quorum.netPeerCount().sendAsync()).thenReturn(supplyAsync(() -> {
                NetPeerCount netPeerCount = new NetPeerCount();
                netPeerCount.setResult(Numeric.encodeQuantity(netPeer));
                return netPeerCount;
            }));
        }
    }

}