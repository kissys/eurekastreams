/*
 * Copyright (c) 2010-2011 Lockheed Martin Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurekastreams.server.persistence.mappers.db;

import java.util.List;

import javax.persistence.Query;

import org.eurekastreams.server.action.request.SharedResourceRequest;
import org.eurekastreams.server.domain.stream.SharedResource;
import org.eurekastreams.server.domain.stream.StreamScope;
import org.eurekastreams.server.domain.stream.StreamScope.ScopeType;
import org.eurekastreams.server.persistence.mappers.BaseArgDomainMapper;

/**
 * DB Mapper to get a shared resource by unique key.
 */
public class FindOrInsertSharedResourceByUniqueKeyDbMapper extends
        BaseArgDomainMapper<SharedResourceRequest, SharedResource>
{
    /**
     * Find or create a shared resource by type and key.
     * 
     * @param inSharedResourceRequest
     *            a SharedResource to use as a request object, containing the resource type and unique key - unique key
     *            is lowercased
     * @return the existing resource or newly inserted one
     */
    @SuppressWarnings("unchecked")
    @Override
    public SharedResource execute(final SharedResourceRequest inSharedResourceRequest)
    {
        if (inSharedResourceRequest.getUniqueKey() == null)
        {
            return null;
        }
        String uniqueKey = inSharedResourceRequest.getUniqueKey().toLowerCase();
        Query q = getEntityManager().createQuery("FROM SharedResource WHERE uniqueKey = :uniqueKey");
        q.setParameter("uniqueKey", uniqueKey);
        List<SharedResource> resources = q.getResultList();
        if (resources == null || resources.size() == 0)
        {
            SharedResource sr = new SharedResource();
            sr.setUniqueKey(uniqueKey);

            // find or create the StreamScope
            List<StreamScope> scopes = getEntityManager().createQuery(
                    "From StreamScope WHERE scopeType=:scopeType AND uniqueKey=:uniqueKey").setParameter("scopeType",
                    ScopeType.RESOURCE).setParameter("uniqueKey", uniqueKey).getResultList();

            if (scopes.size() == 1)
            {
                sr.setStreamScope(scopes.get(0));
            }
            else
            {
                sr.setStreamScope(new StreamScope(ScopeType.RESOURCE, uniqueKey));
            }

            getEntityManager().persist(sr);

            if (sr.getStreamScope().getDestinationEntityId() == null)
            {
                sr.getStreamScope().setDestinationEntityId(sr.getId());
            }

            return sr;
        }
        return resources.get(0);
    }
}
