//package io.gateway.service;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
//import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.context.ApplicationEventPublisherAware;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Service
//@SuppressWarnings("all")
//public class DynamicRouteServiceImpl implements DynamicRouteService, ApplicationEventPublisherAware {
//
//    private final static Logger log = LoggerFactory.getLogger(DynamicRouteServiceImpl.class);
//
//    /**
//     * 写路由定义
//     */
//    private final RouteDefinitionWriter routeDefinitionWriter;
//    /**
//     * 获取路由定义
//     */
//    private final RouteDefinitionLocator routeDefinitionLocator;
//
//    /**
//     * 事件发布
//     */
//    private ApplicationEventPublisher publisher;
//
//    public DynamicRouteServiceImpl(RouteDefinitionWriter routeDefinitionWriter, RouteDefinitionLocator routeDefinitionLocator, ApplicationEventPublisher publisher) {
//        this.routeDefinitionWriter = routeDefinitionWriter;
//        this.routeDefinitionLocator = routeDefinitionLocator;
//        this.publisher = publisher;
//    }
//
//    @Override
//    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
//        // 完成事件推送句柄的初始化
//        this.publisher = applicationEventPublisher;
//    }
//
//    @Override
//    public String updateRoute(RouteDefinition routeDefinition) {
//        try {
//            log.info("gateway update route: [{}]", routeDefinition);
//            this.routeDefinitionWriter.delete(Mono.just(routeDefinition.getId()));
//        } catch (Exception ex) {
//            return "update fail, not find route routeId: " + routeDefinition.getId();
//        }
//
//        try {
//            this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
//            this.publisher.publishEvent(new RefreshRoutesEvent(this));
//            return "success";
//        } catch (Exception ex) {
//            return "update route fail";
//        }
//    }
//
//    @Override
//    public String batchUpdateRoute(List<RouteDefinition> routeDefinitionList) {
//        log.info("gateway update route: [{}]", routeDefinitionList);
//        // 先拿到当前 Gateway 中存储的路由定义
//        List<RouteDefinition> routeDefinitionsExits =
//                routeDefinitionLocator.getRouteDefinitions().buffer().blockFirst();
//        if (!CollectionUtils.isEmpty(routeDefinitionsExits)) {
//            // 清除掉之前所有的 "旧的" 路由定义
//            routeDefinitionsExits.forEach(rd -> {
//                log.info("delete route definition: [{}]", rd);
//                deleteRouteById(rd.getId());
//            });
//        }
//        // 把更新的路由定义同步到 gateway 中
//        routeDefinitionList.forEach(definition -> updateRoute(definition));
//        return "success";
//    }
//
//    @Override
//    public String addRoute(RouteDefinition routeDefinition) {
//        log.info("gateway add route: [{}]", routeDefinition);
//        // 保存路由配置并发布
//        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
//        // 发布事件通知给 Gateway, 同步新增的路由定义
//        this.publisher.publishEvent(new RefreshRoutesEvent(this));
//        return "success";
//    }
//
//    @Override
//    public String deleteRouteById(String id) {
//
//        try {
//            log.info("gateway delete route id: [{}]", id);
//            this.routeDefinitionWriter.delete(Mono.just(id));
//            // 发布事件通知给 gateway 更新路由定义
//            this.publisher.publishEvent(new RefreshRoutesEvent(this));
//            return "delete success";
//        } catch (Exception ex) {
//            log.error("gateway delete route fail: [{}]", ex.getMessage(), ex);
//            return "delete fail";
//        }
//    }
//}
