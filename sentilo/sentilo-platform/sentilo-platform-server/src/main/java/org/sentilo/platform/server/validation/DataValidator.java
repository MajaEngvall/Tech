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
package org.sentilo.platform.server.validation;

import org.sentilo.platform.common.domain.DataInputMessage;
import org.sentilo.platform.server.exception.MessageValidationException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class DataValidator extends AbstractRequestMessageValidator<DataInputMessage> {

  @Override
  public void validateRequestMessageOnPut(final DataInputMessage requestMessage) throws MessageValidationException {
    Assert.notNull(requestMessage);
    if (CollectionUtils.isEmpty(requestMessage.getObservations())) {
      throw new MessageValidationException("To send data is mandatory to inform the observations");
    }
    super.validateRequestMessageOnPut(requestMessage);
  }

}