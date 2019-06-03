package org.web3j.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.web3j.spring.autoconfigure.QuorumProperties.QUORUM_PREFIX;

/**
 * web3j property container.
 */
@ConfigurationProperties(prefix = QUORUM_PREFIX)
public class QuorumProperties {

    public static final String QUORUM_PREFIX = "quorum";

    private String clientAddress;

    private Boolean adminClient;
    
    private String networkId;

    private Long httpTimeoutSeconds;

    private Long pollingInterval;

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public Boolean isAdminClient() {
        return adminClient;
    }

    public void setAdminClient(Boolean adminClient) {
        this.adminClient = adminClient;
    }
    
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Long getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }

    public void setHttpTimeoutSeconds(Long httpTimeoutSeconds) {
        this.httpTimeoutSeconds = httpTimeoutSeconds;
    }

    public Long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
