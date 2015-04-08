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
package org.sentilo.platform.server.test.parser;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sentilo.platform.server.dto.ErrorMessage;
import org.sentilo.platform.server.parser.ErrorParser;

public class ErrorParserTest {

  private ErrorParser errorParser;

  @Before
  public void setUp() throws Exception {
    errorParser = new ErrorParser();
  }

  @Test
  public void writeInternal() throws Exception {
    final ErrorMessage message = new ErrorMessage(400, "Bad request");
    final ByteArrayOutputStream baos = errorParser.writeInternal(message);

    Assert.assertEquals("{\"code\":400,\"message\":\"Bad request\"}", baos.toString());
  }
}
