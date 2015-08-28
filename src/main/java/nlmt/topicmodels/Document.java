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

import java.util.List;

/**
 * The Document class performs two important functions. First,
 * it maps each word into the vocabulary, and keeps track of
 * each vocabulary index instead of the raw string. Second,
 * it keeps track of what topics have been assigned to each
 * word in the document.
 */
public class Document
{
    // The vocabulary words as they appear. For example,
    // "the cat sat" may be mapped to the array
    // [90, 5, 108] where 90 = "the", 5 = "cat", and 108 = "sat"
    // in the vocabulary
    private int [] wordArray;

    // The topic assigned to the specific word in the
    // document. For example, [0, 3, 2] means that
    // "the" is assigned to topic 0, "cat" is assigned to
    // topic 3, and "sat" is assigned to topic 2
    private int [] topicArray;

    // The Vocabulary that provides the mapping from word
    // Strings to numbers
    private Vocabulary vocabulary;

    public Document(Vocabulary vocabulary) {
        this.wordArray = new int[0];
        this.topicArray = new int[0];
        this.vocabulary = vocabulary;
    }

    /**
     * Each document is a List of Strings that represent the
     * words in the document. For each word, add it to the vocabulary,
     * and then add the vocabulary number assigned to the word to
     * the wordArray for the document. Assign the topic for the
     * @param words
     */
    public void readDocument(List<String> words) {
        wordArray = new int[words.size()];
        topicArray = new int[words.size()];
        int currentIndex = 0;
        for (String word : words) {
            vocabulary.addWord(word);
            wordArray[currentIndex] = vocabulary.getIndexFromWord(word);
            topicArray[currentIndex] = -1;
            currentIndex++;
        }
    }

    /**
     * Returns the list of words that the document is composed of.
     * Note that the resultant array contains indexes into the
     * vocabulary for each word.
     *
     * @return the list of word indexes
     */
    public int [] getWordArray() {
        return wordArray;
    }

    /**
     * Returns the original set of strings representing the document
     * from the vocabulary.
     *
     * @return the original array of Strings
     */
    public String [] getRawWords() {
        String [] result = new String[wordArray.length];
        for (int wordIndex = 0; wordIndex < wordArray.length; wordIndex++) {
            result[wordIndex] = vocabulary.getWordFromIndex(wordArray[wordIndex]);
        }
        return result;
    }

    /**
     * Returns what topic is assigned to each word. Each word is assigned
     * exactly one topic.
     *
     * @return the list of topics
     */
    public int [] getTopicArray() {
        return topicArray;
    }

    /**
     * Assigns a topic to the specified word.
     *
     * @param wordIndex the index of the word
     * @param topicIndex the topic to assign
     */
    public void setTopicForWord(int wordIndex, int topicIndex) {
        if ((wordIndex > wordArray.length - 1) || (wordIndex < 0)) {
            throw new IllegalArgumentException("wordIndex must be >= 0 or <= " + (wordArray.length - 1));
        }
        topicArray[wordIndex] = topicIndex;
    }
}
