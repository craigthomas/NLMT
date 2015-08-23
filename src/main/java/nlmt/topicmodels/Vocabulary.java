/**
 * Copyright 2015 Craig Thomas
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
package nlmt.topicmodels;

import java.util.HashMap;
import java.util.Map;

/**
 * The Vocabulary class keeps track of all of the words that have been
 * seen across every document. It maps each word to a number. Numbers
 * are used to represent words inside of documents.
 */
public class Vocabulary {

    // Maps a string word to a number
    private Map<String, Integer> wordIndexMap;

    // The inverse mapping of wordIndexMap
    private Map<Integer, String> indexWordMap;

    // The next number to assign to the next word seen
    private int nextIndex;

    public Vocabulary() {
        wordIndexMap = new HashMap<>();
        indexWordMap = new HashMap<>();
    }

    /**
     * Adds a word to the vocabulary if it isn't already in it.
     *
     * @param word the word to add to the vocabulary
     */
    public void addWord(String word) {
        if (!wordIndexMap.containsKey(word)) {
            wordIndexMap.put(word, nextIndex);
            indexWordMap.put(nextIndex, word);
            incrementNextIndex();
        }
    }

    /**
     * Gets the word associated with the index number. If the index does
     * not appear in the mapping, will return the empty string.
     *
     * @param index the index to get
     * @return the word at the specified index
     */
    public String getWordFromIndex(int index) {
        if (indexWordMap.containsKey(index)) {
            return indexWordMap.get(index);
        }
        return "";
    }

    /**
     * Gets the index for the specified word. If the word is not in the
     * vocabulary, will return -1.
     *
     * @param word the word to check
     * @return the index of the word, or -1 if it does not exist
     */
    public int getIndexFromWord(String word) {
        if (wordIndexMap.containsKey(word)) {
            return wordIndexMap.get(word);
        }
        return -1;
    }

    /**
     * Bumps the nextIndex counter up by 1.
     */
    private void incrementNextIndex() {
        nextIndex++;
    }

    /**
     * Returns the total size of the vocabulary. This is the number
     * of unique words in the vocabulary.
     *
     * @return the number of words in the vocabulary
     */
    public int size() {
        return nextIndex;
    }
}
