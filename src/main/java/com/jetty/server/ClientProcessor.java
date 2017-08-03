package com.jetty.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2017, DATAVISOR, INC.
 * All rights reserved.
 * __________________
 *
 * NOTICE: All information contained herein is, and remains the property
 * of DataVisor, Inc.  The intellectual and technical concepts contained
 * herein are proprietary to DataVisor, Inc. and may be covered by
 * U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law.  Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from DataVisor, Inc.
 *
 */
public class ClientProcessor {
    private static final String uriPath = "/";

    public List<String> processingURI(String uri) {
        if (uri == null)
            return null;

        List<String> clients = new ArrayList<>();
        for (String client : uri.split(uriPath)) {
            if (client.equalsIgnoreCase("jetty") || StringUtils.isEmpty(client))
                continue;
            clients.add(client);
        }
        return clients;
    }

    public static void main(String args[]) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181,localhost:2182,localhost:2183",
                new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        try {
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
            String znodePath = "/test_node";
            String originalData = new String(curatorFramework.getData().forPath(znodePath));
            int i = 0;
            System.out.println("======" + i + "=======" + originalData);
            NodeCache nodeCache = new NodeCache(curatorFramework, znodePath);
            nodeCache.getListenable().addListener(
                    new NodeCacheListener() {
                        @Override
                        public void nodeChanged() {
                            String newData = new String(nodeCache.getCurrentData().getData());
                            System.out.println("=============" + newData);
                        }
                    });
            nodeCache.start();
            while (true) ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
