/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode_examples.luceneSpatial;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.lucene.LuceneQueryException;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.geode.cache.lucene.LuceneServiceProvider;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExampleFindDistanceBetweenTwoLocation {
    public static void main(String[] args) throws InterruptedException, LuceneQueryException {
        // connect to the locator using default port 10334
        ClientCache cache = new ClientCacheFactory().addPoolLocator("127.0.0.1", 10334)
                .set("log-level", "WARN").create();

        // create a local region that matches the server region
        Region<String, TrainStop> region =
                cache.<String, TrainStop>createClientRegionFactory(ClientRegionShortcut.PROXY)
                        .create("example-region-find-distance");
        LuceneService luceneService = LuceneServiceProvider.get(cache);
        // Add some entries into the region
        putEntries(luceneService, region);
        findDistance(region);
        cache.close();
    }

    public static void putEntries(LuceneService luceneService, Map<String, TrainStop> region)
            throws InterruptedException {
        region.put("McD1", new TrainStop("McD1", -78.78318, 35.91112));
        region.put("McD2", new TrainStop("McD2", -78.78217354060413, 35.91045305));
        region.put("McD3", new TrainStop("McD3", -82.54409, 40.64817));
        // Lucene indexing happens asynchronously, so wait for
        // the entries to be in the lucene index.
        luceneService.waitUntilFlushed("simpleIndex1", "example-region-find-distance", 1, TimeUnit.MINUTES);
    }

    public static void findDistance(Region<String, TrainStop> region) {
        double sourceLat = 36.8738;
        double sourceLong = -78.78412;
        Set<String> keySet = region.keySetOnServer();
        for (String s : keySet) {
            double distance = SpatialHelper.getDistanceInMiles(sourceLat, sourceLong,
                    region.get(s).getLongitude(), region.get(s).getLatitude());
            System.out.println("Distance between the source and destination is : " + distance);
        }
    }
}
