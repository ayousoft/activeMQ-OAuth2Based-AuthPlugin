package ir.tiroon.activemq.plugin.oauth;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class TokenAuthenticationBroker extends BrokerFilter {

    private final Logger logger = LoggerFactory.getLogger(TokenAuthenticationBroker.class);

    JedisConnectionFactory jedisConnectionFactory;

    RedisTokenStore redisTokenStore;

    Map<String, String> redisConfig;

    public TokenAuthenticationBroker(Broker next, Map<String, String> redisConfig) {
        super(next);
        this.redisConfig = redisConfig;

        jedisConnectionFactory = new JedisConnectionFactory();

        jedisConnectionFactory.setHostName("localhost");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setDatabase(1);
        jedisConnectionFactory.setPassword("");

        jedisConnectionFactory.afterPropertiesSet();
        String ping = jedisConnectionFactory.getConnection().ping();

        if (!ping.equals("PONG"))
            throw new SecurityException("Redis connection failed:: "+ping);


        redisTokenStore = new RedisTokenStore(jedisConnectionFactory);
    }

    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        //this attributes come from active mq config xml file, which I won't use them in this class.
//        String host = redisConfig.get("host");
//        int port = Integer.parseInt(redisConfig.get("port"));

        String accessToken = info.getPassword();

        String clientId = context.getUserName();

        Collection<OAuth2AccessToken> tokensByClientId = redisTokenStore.findTokensByClientId(clientId);
        OAuth2AccessToken any = tokensByClientId.stream().filter(t -> t.getValue().equals(accessToken)).findAny().orElse(null);

        if(any == null) {
            throw new SecurityException("Token not not found in the data store \n unable to find token:: "+clientId+"::"+accessToken);
        } else {
            logger.debug("Found token [{}] belonging to user: {}. Allowing connection", clientId, accessToken);
            super.addConnection(context, info);
        }
    }
}
