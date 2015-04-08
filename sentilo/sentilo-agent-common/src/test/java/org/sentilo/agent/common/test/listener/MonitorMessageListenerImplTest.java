/*
 * Sentilo
 * 
 * Copyright (C) 2013 Institut Municipal d’Informàtica, Ajuntament de Barcelona.
 * 
 * This program is licensed and may be used, modified and redistributed under the terms of the
 * European Public License (EUPL), either version 1.1 or (at your option) any later version as soon
 * as they are approved by the European Commission.
 * 
 * Alternatively, you may redistribute and/or modify this program under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * 
 * See the licenses for the specific language governing permissions, limitations and more details.
 * 
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along with this program;
 * if not, you may find them at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl http://www.gnu.org/licenses/ and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.sentilo.agent.common.test.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.sentilo.agent.common.listener.MonitorMessageListenerImpl;
import org.sentilo.common.domain.EventMessage;
import org.springframework.data.redis.connection.Message;

public class MonitorMessageListenerImplTest {

  @Spy
  private MonitorMessageListenerImpl listener = new MonitorMessageListenerImpl("TEST");

  @Mock
  private Message message;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void doWithMessage() {
    final String topic = "/TEST_PATTERN";
    listener.onMessage(message, topic.getBytes());

    verify(listener).doWithMessage(any(Message.class), any(EventMessage.class));
  }
}
