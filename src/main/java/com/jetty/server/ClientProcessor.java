package com.jetty.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClientProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProcessor.class);
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
