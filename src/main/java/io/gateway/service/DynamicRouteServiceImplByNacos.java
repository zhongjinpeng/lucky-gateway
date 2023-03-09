//package io.gateway.service;
//
//import com.alibaba.nacos.api.NacosFactory;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.config.listener.Listener;
//import com.alibaba.nacos.api.exception.NacosException;
//import io.lucky.framework.util.GsonUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.Executor;
//
////@Component
//public class DynamicRouteServiceImplByNacos {
//
//    public static final long DEFAULT_TIMEOUT = 30000;
//
//    @Value("${nacos.config.data-id:}")
//    private String nacosRouteDataId;
//
//    @Value("${nacos.config.group:}")
//    private String nacosRouteGroup;
//
//    @Value("${nacos.config.server-addr:}")
//    private String nacosServerAddr;
//
//    private final static Logger log = LoggerFactory.getLogger(DynamicRouteServiceImplByNacos.class);
//    private final DynamicRouteServiceImpl dynamicRouteService;
//    private ConfigService configService;
//
//
//    public DynamicRouteServiceImplByNacos(DynamicRouteServiceImpl dynamicRouteService) {
//        this.dynamicRouteService = dynamicRouteService;
//    }
//
//    @PostConstruct
//    public void init() {
//        log.info("gateway route init...");
//        try {
//            configService = initConfigService();
//            if (configService == null) {
//                log.warn("initConfigService fail");
//                return;
//            }
//            String configInfo = configService.getConfig(nacosRouteDataId, nacosRouteGroup, DEFAULT_TIMEOUT);
//            log.info("获取网关当前配置:{}", configInfo);
//            List<RouteDefinition> definitionList = GsonUtil.JsonToList(configInfo, RouteDefinition.class);
//            for (RouteDefinition definition : definitionList) {
//                log.info("update route : {}", definition.toString());
//                dynamicRouteService.addRoute(definition);
//            }
//        } catch (Exception e) {
//            log.error("初始化网关路由时发生错误", e);
//        }
//        dynamicRouteByNacosListener(nacosRouteDataId, nacosRouteGroup);
//    }
//
//    /**
//     * 监听Nacos下发的动态路由配置
//     *
//     * @param dataId
//     * @param group
//     */
//    public void dynamicRouteByNacosListener(String dataId, String group) {
//        try {
//            configService.addListener(dataId, group, new Listener() {
//                @Override
//                public void receiveConfigInfo(String configInfo) {
//                    log.info("进行网关更新:{}", configInfo);
//                    List<RouteDefinition> definitionList = GsonUtil.GsonToList(configInfo, RouteDefinition.class);
//                    for (RouteDefinition definition : definitionList) {
//                        log.info("update route : {}", definition.toString());
//                        dynamicRouteService.updateRoute(definition);
//                    }
//                }
//
//                @Override
//                public Executor getExecutor() {
//                    log.info("getExecutor\n\r");
//                    return null;
//                }
//            });
//        } catch (NacosException e) {
//            log.error("从nacos接收动态路由配置出错!!!", e);
//        }
//    }
//
//    /**
//     * 初始化网关路由 nacos config
//     *
//     * @return
//     */
//    private ConfigService initConfigService() {
//        try {
//            Properties properties = new Properties();
//            properties.setProperty("serverAddr", nacosServerAddr);
//            properties.setProperty("namespace", "");
//            return configService = NacosFactory.createConfigService(properties);
//        } catch (Exception e) {
//            log.error("初始化网关路由时发生错误", e);
//            return null;
//        }
//    }
//}
