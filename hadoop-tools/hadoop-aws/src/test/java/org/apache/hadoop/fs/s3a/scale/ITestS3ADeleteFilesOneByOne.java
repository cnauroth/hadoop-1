/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hadoop.fs.s3a.scale;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.s3a.Constants;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests file deletion with multi-delete disabled.
 */
public class ITestS3ADeleteFilesOneByOne extends ITestS3ADeleteManyFiles {

  @Override
  protected Configuration createConfiguration() {
    Configuration configuration = super.createConfiguration();
    configuration.setBoolean(Constants.ENABLE_MULTI_DELETE, false);
    return configuration;
  }

  @Override
  @Test
  public void testOpenCreate() throws IOException {

  }
}
