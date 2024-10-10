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
package ca.craigthomas.nlmt.datatypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.io.File;

import org.apache.commons.io.FileUtils;

public class DocumentReader {
    // The list of documents, each one a list of strings
    List<List<String>> documents;

    /**
     * The default constructor for a DocumentReader takes a file path
     * to where a list of text documents exist. Each text document is read
     * in, punctuation is removed, and stored in the document list.
     *
     * @param documentFilePath a String describing where to read files from
     * @param extension the file extension to read
     * @param encoding the encoding of the text files
     */
    public DocumentReader(String documentFilePath, String extension, String encoding) throws IOException {
        File path = new File(documentFilePath);
        String[] extensions = {extension};
        documents = new ArrayList<>();

        // Iterate over the files in the directory, read the contents, remove non-characters, and add to list
        for (Iterator<File> it = FileUtils.iterateFiles(path, extensions, false); it.hasNext(); ) {
            File file = it.next();
            String contents = FileUtils.readFileToString(file, encoding);
            documents.add(Arrays.asList(contents.toLowerCase().replaceAll("\\n", " ").replaceAll("\\p{P}", " ").split("\\s+")));
        }
    }

    /**
     * Returns the list of parsed documents.
     */
    public List<List<String>> getDocuments() {
        return documents;
    }
}
