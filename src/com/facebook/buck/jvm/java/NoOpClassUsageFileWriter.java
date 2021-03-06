/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.java;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.rules.CellPathResolver;
import javax.tools.StandardJavaFileManager;

public class NoOpClassUsageFileWriter implements ClassUsageFileWriter {
  private static final NoOpClassUsageFileWriter INSTANCE = new NoOpClassUsageFileWriter();

  private NoOpClassUsageFileWriter() {}

  public static NoOpClassUsageFileWriter instance() {
    return INSTANCE;
  }

  @Override
  public StandardJavaFileManager wrapFileManager(StandardJavaFileManager inner) {
    return inner;
  }

  @Override
  public void writeFile(ProjectFilesystem filesystem, CellPathResolver cellPathResolver) {}
}
