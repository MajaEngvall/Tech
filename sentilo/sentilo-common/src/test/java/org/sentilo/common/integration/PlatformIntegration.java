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
package org.sentilo.common.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sentilo.common.rest.RESTClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/platform-integration-context.xml")
public class PlatformIntegration {

  @Autowired
  private RESTClient httpRestClient;

  @Test
  public void validateGetObservations() {
    // api/permissions
    final String uri = "data/provider35";
    final String response = httpRestClient.get(uri);
    assertTrue("No se ha realizado correctamente la llamada a la plataforma", StringUtils.hasText(response));
  }

  @Test
  public void setInvalidTokenProgrammatically() {
    // api/permissions
    final String invalidToken = "abcdef";
    final String uri = "data/provider35";
    final String response = httpRestClient.get(uri, invalidToken);
    assertTrue("No se ha realizado correctamente la llamada a la plataforma", StringUtils.hasText(response));
  }

}
