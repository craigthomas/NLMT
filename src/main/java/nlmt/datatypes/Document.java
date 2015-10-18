/*
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
package nlmt.datatypes;

import java.io.Serializable;
import java.util.*;

/**
 * The Document class performs two important functions. First,
 * it maps each word into the vocabulary, and keeps track of
 * each vocabulary index instead of the raw string. Second,
 * it keeps track of what topics have been assigned to each
 * word in the document.
 */
public class Document implements Serializable
{
    // The vocabulary words along with topic assignments
    private List<Word> wordArray;

    // The Vocabulary that provides the mapping from word
    // Strings to numbers
    private IdentifierObjectMapper<String> vocabulary;

    public Document(IdentifierObjectMapper<String> vocabulary) {
        if (vocabulary == null) {
            throw new IllegalArgumentException("vocabulary cannot be null");
        }
        wordArray = new ArrayList<>();
        this.vocabulary = vocabulary;
    }

    /**
     * Each document is a List of Strings that represent the
     * words in the document. For each word, add it to the vocabulary,
     * and then add the vocabulary number assigned to the word to
     * the wordArray for the document. If <code>addWordsToVocabulary</code> is
     * <code>true</code>, then any previously unseen words will be added to
     * the vocabulary.
     *
     * @param words the list of words in the document
     * @param addWordsToVocabulary if true, then previously unseen words will be added to the vocabulary
     */
    public void readDocument(List<String> words, boolean addWordsToVocabulary) {
        for (String word : words) {
            boolean vocabularyContainsWord = vocabulary.contains(word);
            if ((vocabularyContainsWord) || (!vocabularyContainsWord && addWordsToVocabulary)) {
                vocabulary.addObject(word);
                wordArray.add(new Word(word, vocabulary.getIndexFromObject(word)));
            }
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
        return wordArray.stream().mapToInt(Word::getVocabularyId).toArray();
    }

    /**
     * Returns the original set of strings representing the document
     * from the vocabulary.
     *
     * @return the original array of Strings
     */
    public String [] getRawWords() {
        return wordArray.stream().map(Word::getRawWord).toArray(String[]::new);
    }

    /**
     * Returns what topic is assigned to each word. Each word is assigned
     * exactly one topic.
     *
     * @return the list of topics
     */
    public int [] getTopicArray() {
        return wordArray.stream().mapToInt(Word::getTopic).toArray();
    }

    /**
     * Assigns a topic to the specified word.
     *
     * @param wordIndex the index of the word
     * @param topicIndex the topic to assign
     */
    public void setTopicForWord(int wordIndex, int topicIndex) {

        if ((wordIndex > wordArray.size() - 1) || (wordIndex < 0)) {
            throw new IllegalArgumentException("wordIndex must be >= 0 or <= " + (wordArray.size() - 1));
        }
        wordArray.get(wordIndex).setTopic(topicIndex);
    }

    /**
     * Sets all of the topics to -1.
     */
    public void clearTopics() {
        wordArray.stream().forEach(word -> word.setTopic(-1));
    }

    /**
     * Returns the set of all words used in the document.
     *
     * @return the set of all words
     */
    public Set<Integer> getWordSet() {
        Set<Integer> result = new HashSet<>();
        wordArray.stream().mapToInt(Word::getVocabularyId).forEach(result::add);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        return Arrays.equals(getWordArray(), document.getWordArray()) && vocabulary.equals(document.vocabulary);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getWordArray());
        result = 31 * result + vocabulary.hashCode();
        return result;
    }
}
