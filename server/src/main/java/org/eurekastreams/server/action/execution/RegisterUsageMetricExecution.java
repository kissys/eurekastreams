/*
 * Copyright (c) 2010 Lockheed Martin Corporation
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
package org.eurekastreams.server.action.execution;

import java.io.Serializable;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.eurekastreams.commons.actions.TaskHandlerExecutionStrategy;
import org.eurekastreams.commons.actions.context.Principal;
import org.eurekastreams.commons.actions.context.PrincipalActionContext;
import org.eurekastreams.commons.actions.context.TaskHandlerActionContext;
import org.eurekastreams.commons.date.DayOfWeekStrategy;
import org.eurekastreams.commons.logging.LogFactory;
import org.eurekastreams.commons.server.UserActionRequest;
import org.eurekastreams.server.domain.UsageMetric;
import org.eurekastreams.server.persistence.mappers.DomainMapper;
import org.eurekastreams.server.persistence.mappers.requests.PersistenceRequest;
import org.eurekastreams.server.search.modelview.UsageMetricDTO;

/**
 * Action to create UsageMetric entity and queue up action to persist it.
 * 
 */
public class RegisterUsageMetricExecution implements TaskHandlerExecutionStrategy<PrincipalActionContext>
{
    /**
     * Local logger instance.
     */
    private final Log logger = LogFactory.make();

    /**
     * Mapper to get a person stream scope id.
     */
    private DomainMapper<String, Long> personStreamScopeIdMapper;

    /**
     * Mapper to get a group stream scope id.
     */
    private DomainMapper<String, Long> groupStreamScopeIdMapper;

    /**
     * Strategy to test if a date is a weekday.
     */
    private DayOfWeekStrategy dayOfWeekStrategy;

    /**
     * Constructor.
     * 
     * @param inPersonStreamScopeIdMapper
     *            mapper to get the stream scope id for a person by account id
     * @param inGroupStreamScopeIdMapper
     *            mapper to get the stream scope id for a group by short name
     * @param inDayOfWeekStrategy
     *            strategy to determine if a date is a weekday
     */
    public RegisterUsageMetricExecution(final DomainMapper<String, Long> inPersonStreamScopeIdMapper,
            final DomainMapper<String, Long> inGroupStreamScopeIdMapper, final DayOfWeekStrategy inDayOfWeekStrategy)
    {
        personStreamScopeIdMapper = inPersonStreamScopeIdMapper;
        groupStreamScopeIdMapper = inGroupStreamScopeIdMapper;
        dayOfWeekStrategy = inDayOfWeekStrategy;
    }

    /**
     * Create UserMetric entity and queue up action to persist it.
     * 
     * @param inActionContext
     *            ActionContext.
     * 
     * @return null;
     */
    @Override
    public Serializable execute(final TaskHandlerActionContext<PrincipalActionContext> inActionContext)
    {
        Date date = new Date();

        if (!dayOfWeekStrategy.isWeekday(date))
        {
            logger.info("Ignoring weekend stats");
            return null;
        }

        UsageMetricDTO umdto = (UsageMetricDTO) inActionContext.getActionContext().getParams();
        Principal principal = inActionContext.getActionContext().getPrincipal();
        Long streamScopeId = null;
        String uniqueKey = null;

        String streamJson = umdto.getMetricDetails();
        logger.info("Stream metric received: " + streamJson);

        if (umdto.isStreamView() && streamJson != null && streamJson.startsWith("{"))
        {
            // {"query":{"recipient":[{"type":"GROUP", "name":"woot"}], "sortBy":"date"}}
            try
            {
                JSONObject jsonObj = JSONObject.fromObject(streamJson);

                if (jsonObj.containsKey("query"))
                {
                    jsonObj = jsonObj.getJSONObject("query");
                    if (jsonObj.containsKey("recipient"))
                    {
                        JSONArray recipients = jsonObj.getJSONArray("recipient");
                        if (recipients.size() == 1)
                        {
                            jsonObj = recipients.getJSONObject(0);
                            if (jsonObj.containsKey("type") && jsonObj.containsKey("name"))
                            {
                                uniqueKey = jsonObj.getString("name");
                                if ("PERSON".equals(jsonObj.getString("type")))
                                {
                                    streamScopeId = personStreamScopeIdMapper.execute(uniqueKey);
                                    logger.debug("Found person stream scope id " + streamScopeId + " from account id: "
                                            + uniqueKey);
                                }
                                else if ("GROUP".equals(jsonObj.getString("type")))
                                {
                                    streamScopeId = groupStreamScopeIdMapper.execute(uniqueKey);
                                    logger.debug("Found group stream scope id " + streamScopeId + " from short name: "
                                            + uniqueKey);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                logger.info("Potentially invalid JSON: " + streamJson);
            }
        }

        UsageMetric um = new UsageMetric(principal.getId(), umdto.isPageView(), umdto.isStreamView(), streamScopeId,
                new Date());

        logger.trace("Registering metric for user: " + principal.getAccountId() + " StreamView:" + umdto.isStreamView()
                + " PageView:" + umdto.isPageView() + " MetricDetails: " + umdto.getMetricDetails());

        inActionContext.getUserActionRequests().add(
                new UserActionRequest("persistUserMetricAsyncAction", null, new PersistenceRequest<UsageMetric>(um)));
        return null;
    }
}
