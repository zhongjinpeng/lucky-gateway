package io.gateway.service;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.List;

public interface DynamicRouteService extends ApplicationEventPublisherAware {

    /**
     * 更新路由信息
     *
     * @param routeDefinition
     * @return
     * @throws Exception
     */
    String updateRoute(RouteDefinition routeDefinition);

    /**
     * 批量更新路由信息
     *
     * @param routeDefinitionList
     * @return
     */
    String batchUpdateRoute(List<RouteDefinition> routeDefinitionList);

    /**
     * 添加路由信息
     *
     * @param routeDefinition
     * @return
     * @throws Exception
     */
    String addRoute(RouteDefinition routeDefinition);

    /**
     * 删除路由信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    String deleteRouteById(String id);
}
