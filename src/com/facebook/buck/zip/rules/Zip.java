/*
 * Copyright 2014-present Facebook, Inc.
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

package com.facebook.buck.zip.rules;

import com.facebook.buck.event.EventDispatcher;
import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.HasOutputName;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.modern.BuildCellRelativePathFactory;
import com.facebook.buck.rules.modern.Buildable;
import com.facebook.buck.rules.modern.InputDataRetriever;
import com.facebook.buck.rules.modern.InputPath;
import com.facebook.buck.rules.modern.InputPathResolver;
import com.facebook.buck.rules.modern.ModernBuildRule;
import com.facebook.buck.rules.modern.OutputPath;
import com.facebook.buck.rules.modern.OutputPathResolver;
import com.facebook.buck.step.Step;
import com.facebook.buck.util.MoreCollectors;
import com.facebook.buck.util.zip.ZipCompressionLevel;
import com.facebook.buck.zip.ZipStep;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Path;

public class Zip extends ModernBuildRule<Zip> implements HasOutputName, Buildable {
  private final String name;
  private final ImmutableSortedSet<InputPath> sources;
  private final OutputPath output;
  private final boolean flatten;
  private final boolean mergeSourceZips;

  public Zip(
      SourcePathRuleFinder ruleFinder,
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      String outputName,
      ImmutableSortedSet<SourcePath> sources,
      boolean flatten,
      boolean mergeSourceZips) {
    super(buildTarget, projectFilesystem, ruleFinder, Zip.class);

    this.name = outputName;
    this.sources =
        sources.stream().map(InputPath::new).collect(MoreCollectors.toImmutableSortedSet());
    this.output = new OutputPath(name);
    this.flatten = flatten;
    this.mergeSourceZips = mergeSourceZips;
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      EventDispatcher eventDispatcher,
      ProjectFilesystem filesystem,
      InputPathResolver inputPathResolver,
      InputDataRetriever inputDataRetriever,
      OutputPathResolver outputPathResolver,
      BuildCellRelativePathFactory buildCellPathFactory) {
    Path outputPath = outputPathResolver.resolvePath(this.output);

    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    ImmutableSortedSet<SourcePath> sourcePathSources =
        sources
            .stream()
            .map(InputPath::getLimitedSourcePath)
            .collect(MoreCollectors.toImmutableSortedSet());

    Path scratchDir = outputPathResolver.getTempPath();

    FileBundler bundler;
    if (mergeSourceZips) {
      bundler = new SrcZipAwareFileBundler(getBuildTarget());
    } else {
      bundler = new CopyingFileBundler(getBuildTarget());
    }
    bundler.copy(
        filesystem,
        buildCellPathFactory,
        steps,
        scratchDir,
        sourcePathSources,
        inputPathResolver.getLimitedSourcePathResolver());

    steps.add(
        new ZipStep(
            filesystem,
            outputPath,
            ImmutableSortedSet.of(),
            flatten,
            ZipCompressionLevel.DEFAULT_COMPRESSION_LEVEL,
            scratchDir));

    return steps.build();
  }

  @Override
  public SourcePath getSourcePathToOutput() {
    return getSourcePath(output);
  }

  @Override
  public String getOutputName() {
    return name;
  }
}
