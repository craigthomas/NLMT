/*
 * Copyright 2024 Craig Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.craigthomas.nlmt.commandline;

import com.beust.jcommander.Parameter;

/**
 * A data class that stores the arguments that may be passed to the topic model process.
 */
public class Arguments
{
    @Parameter(names={"--num_topics"}, description="the number of topics to recover (no effect for hlda, default = 10)")
    public Integer numTopics = 10;

    @Parameter(names={"--document_dir"}, description="the path to where text documents are stored", arity=1)
    public String documentDir = "None";

    @Parameter(names={"--num_topic_descriptors"}, description="the number of words used to describe the topic (default = 5)")
    public Integer numTopicDescriptors = 5;

    @Parameter(names={"--iterations"}, description="the number of iterations of sampling to perform (default = 1000)")
    public Integer iterations = 1000;

    @Parameter(names={"--file_extension"}, description="reads only files with the specified extension (default = txt)", arity=1)
    public String extension = "txt";

    @Parameter(names={"--text_encoding"}, description="the text encoding of the files to read (default = utf8)", arity=1)
    public String textEncoding = "utf8";
}