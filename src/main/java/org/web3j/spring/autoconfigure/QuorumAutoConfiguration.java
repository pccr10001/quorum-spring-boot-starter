package org.web3j.spring.autoconfigure;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.quorum.JsonRpc2_0Quorum;
import org.web3j.quorum.Quorum;
import org.web3j.spring.actuate.QuorumHealthIndicator;
import org.web3j.utils.Async;

import java.util.concurrent.TimeUnit;

/**
 * web3j auto configuration for Spring Boot.
 */
@Configuration
@ConditionalOnClass(Quorum.class)
@EnableConfigurationProperties(QuorumProperties.class)
public class QuorumAutoConfiguration {

    private static Log log = LogFactory.getLog(QuorumAutoConfiguration.class);

    @Autowired
    private QuorumProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public Quorum quorum() {
        Web3jService web3jService = buildService(properties.getClientAddress());

        if (properties.getPollingInterval() != null && properties.getPollingInterval() > 0) {
            log.info("Building service for endpoint: " + properties.getClientAddress() + " with polling interval " + properties.getPollingInterval() + " msecs");
            return new JsonRpc2_0Quorum(web3jService, properties.getPollingInterval(), Async.defaultExecutorService());
        }

        log.info("Building service for endpoint: " + properties.getClientAddress());
        return Quorum.build(web3jService);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = QuorumProperties.QUORUM_PREFIX, name = "admin-client", havingValue = "true")
    public Admin admin() {
        Web3jService web3jService = buildService(properties.getClientAddress());
        log.info("Building admin service for endpoint: " + properties.getClientAddress());
        return Admin.build(web3jService);
    }

    private Web3jService buildService(String clientAddress) {
        Web3jService web3jService;

        if (clientAddress == null || clientAddress.equals("")) {
            web3jService = new HttpService(createOkHttpClient());
        } else if (clientAddress.startsWith("http")) {
            web3jService = new HttpService(clientAddress, createOkHttpClient(), false);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            web3jService = new WindowsIpcService(clientAddress);
        } else {
            web3jService = new UnixIpcService(clientAddress);
        }

        return web3jService;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        configureLogging(builder);
        configureTimeouts(builder);
        return builder.build();
    }

    private void configureTimeouts(OkHttpClient.Builder builder) {
        Long tos = properties.getHttpTimeoutSeconds();
        if (tos != null) {
            builder.connectTimeout(tos, TimeUnit.SECONDS);
            builder.readTimeout(tos, TimeUnit.SECONDS);  // Sets the socket timeout too
            builder.writeTimeout(tos, TimeUnit.SECONDS);
        }
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }


    @Bean
    @ConditionalOnBean(Web3j.class)
    QuorumHealthIndicator quorumHealthIndicator(Quorum quorum) {
        return new QuorumHealthIndicator(quorum);
    }
}
