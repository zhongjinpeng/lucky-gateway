package io.gateway.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.lucky.utils.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
public class DynamicRouteServiceByNacos extends AbstractDynamicRouteService {
    private final static Logger log = LoggerFactory.getLogger(DynamicRouteServiceByNacos.class);

    public static final long DEFAULT_TIMEOUT = 30000;

    @Value("${spring.cloud.nacos.config.data-id}")
    private String dataId;

    @Value("${spring.cloud.nacos.config.group}")
    private String group;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;
    private ConfigService configService;

    public DynamicRouteServiceByNacos(RouteDefinitionWriter routeDefinitionWriter, RouteDefinitionLocator routeDefinitionLocator, ApplicationEventPublisher publisher) {
        super(routeDefinitionWriter, routeDefinitionLocator, publisher);
    }

    @PostConstruct
    public void init() {
        try {
            configService = initConfigService();
            if (configService == null) {
                log.warn("initConfigService fail");
                return;
            }
            String configInfo = configService.getConfig(dataId, group, DEFAULT_TIMEOUT);
            log.info("获取网关当前配置:{}", configInfo);
            List<RouteDefinition> definitionList = GsonUtil.JsonToList(configInfo, RouteDefinition.class);
            for (RouteDefinition definition : definitionList) {
                addRoute(definition);
            }
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
        }
        dynamicRouteByNacosListener(dataId, group);
    }

    /**
     * 监听Nacos下发的动态路由配置
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListener(String dataId, String group) {
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    List<RouteDefinition> definitionList = GsonUtil.GsonToList(configInfo, RouteDefinition.class);
                    for (RouteDefinition definition : definitionList) {
                        updateRoute(definition);
                    }
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            log.error("从nacos接收动态路由配置出错!!!", e);
        }
    }

    /**
     * 初始化网关路由 nacos config
     *
     * @return
     */
    private ConfigService initConfigService() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", serverAddr);
            properties.setProperty("namespace", "");
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
            return null;
        }
    }
}
