/*
 * Copyright (c) 2011 Lockheed Martin Corporation
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
package org.eurekastreams.web.client.events;

import org.eurekastreams.server.domain.InAppNotificationDTO;
import org.eurekastreams.web.client.events.data.BaseDataResponseEvent;

/**
 * Raised when a single unread notification is marked read or deleted.
 */
public class UnreadNotificationClearedEvent extends BaseDataResponseEvent<InAppNotificationDTO>
{
    /**
     * Constructor.
     *
     * @param inNotification
     *            The notification.
     */
    public UnreadNotificationClearedEvent(final InAppNotificationDTO inNotification)
    {
        super(inNotification);
    }
}
