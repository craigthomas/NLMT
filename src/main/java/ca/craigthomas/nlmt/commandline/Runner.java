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

import com.beust.jcommander.JCommander;
import ca.craigthomas.nlmt.topicmodels.LDAModel;
import ca.craigthomas.nlmt.datatypes.DocumentReader;

import java.io.IOException;
import java.util.List;

public class Runner
{
    /**
     * Runs the topic modeler with the specified command line options.
     *
     * @param argv the set of options passed to the runner
     */
    public static void main(String[] argv) throws IOException {
        Arguments args = new Arguments();
        JCommander jCommander = JCommander.newBuilder().addObject(args).build();
        jCommander.setProgramName("nlmt");
        jCommander.parse(argv);

        if (args.documentDir.equals("None")) {
            System.out.println("The --document_dir switch must be specified");
            System.exit(1);
        }

        // Read the list of documents
        System.out.println("--- Reading documents from " + args.documentDir);
        DocumentReader documentReader = new DocumentReader(args.documentDir, args.extension, args.textEncoding);
        List<List<String>> documents = documentReader.getDocuments();
        System.out.println("--- Read " + documents.size() + " documents");

        // Run topic modeling
        System.out.println("--- Building topic model");
        LDAModel model = new LDAModel(args.numTopics);
        model.readDocuments(documents);
        model.doGibbsSampling(args.iterations, true);

        // Print out topic listings
        List<List<String>> topics = model.getTopics(args.numTopicDescriptors);
        System.out.println();
        System.out.println("--- Topic model results:");
        for (int i=0; i < args.numTopics; i++) {
            System.out.println("Topic " + i + ": " + topics.get(i));
        }
    }
}