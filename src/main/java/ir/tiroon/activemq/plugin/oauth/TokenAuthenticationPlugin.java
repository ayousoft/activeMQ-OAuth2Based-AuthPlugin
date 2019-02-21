package ir.tiroon.activemq.plugin.oauth;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.util.UDPTraceBrokerPlugin;

import java.util.Map;

public class TokenAuthenticationPlugin implements BrokerPlugin {

    Map<String, String> redisConfig;

    public Broker installPlugin(Broker broker) throws Exception {
        return new TokenAuthenticationBroker(broker, redisConfig);
    }

    public Map<String, String> getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(Map<String, String> redisConfig) {
        this.redisConfig = redisConfig;
    }
}
