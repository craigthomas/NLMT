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
import java.util.stream.Collectors;

/**
 * A SparseDocument contains only the count of the unique words in a document,
 * as well as the words themselves.
 */
public class SparseDocument implements Serializable
{
    // Maps the ID of a word to a Word object
    private Map<Integer, Word> wordMap;

    // The Vocabulary that provides the mapping from word
    // Strings to numbers
    private IdentifierObjectMapper<String> vocabulary;

    public SparseDocument(IdentifierObjectMapper<String> vocabulary) {
        if (vocabulary == null) {
            throw new IllegalArgumentException("vocabulary cannot be null");
        }
        this.vocabulary = vocabulary;
        wordMap = new HashMap<>();
    }

    /**
     * Each document is a List of Strings that represent the
     * words in the document. For each word, add it to the vocabulary,
     * and then add the vocabulary number assigned to the word to
     * the wordArray for the document.
     *
     * @param words the list of words in the document
     */
    public void readDocument(List<String> words) {
        Map<String, Long> wordCounts = words.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (String key : wordCounts.keySet()) {
            Word newWord = new Word(key, vocabulary.addObject(key));
            newWord.setTotalCount(wordCounts.get(key).intValue());
            wordMap.put(newWord.getVocabularyId(), newWord);
        }
    }

    /**
     * Sets topic for the vocabulary word.
     *
     * @param vocabularyWord the word to set
     * @param topic the topic number to set
     */
    public void setTopicForWord(int vocabularyWord, int topic) {
        if (wordMap.containsKey(vocabularyWord)) {
            Word word = wordMap.get(vocabularyWord);
            word.setTopic(topic);
        }
    }

    /**
     * Gets the topic for the vocabulary word. Will return -1 if the word is not
     * found, or if a topic is not set.
     *
     * @param vocabularyWord the vocabulary word to look up
     * @return the topic for the word or -1 if not set or not found
     */
    public int getTopicForWord(int vocabularyWord) {
        return (wordMap.containsKey(vocabularyWord)) ? wordMap.get(vocabularyWord).getTopic() : -1;
    }

    /**
     * Gets the count of this vocabulary word in this document. Will return 0
     * if the word does not occur in this document.
     *
     * @param vocabularyWord the word count to get
     * @return the count of the number of times this word appears
     */
    public int getWordCount(int vocabularyWord) {
        return (wordMap.containsKey(vocabularyWord)) ? wordMap.get(vocabularyWord).getTotalCount() : 0;
    }

    /**
     * Returns the Set of Word objects that appears in the document.
     *
     * @return the Set of Word objects in the document
     */
    public Set<Word> getWordSet() {
        return wordMap.values().stream().collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Gets the set of topics that appears in this document.
     *
     * @return the set of topics appearing in the document
     */
    public Set<Integer> getTopics() {
        return wordMap.values().stream().map(Word::getTopic).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Returns a count of the number of words in each topic. The result is a map
     * with the topic number being the key, and the topic count being the value.
     *
     * @return a Map of topic numbers to number of words
     */
    public Map<Integer, Integer> getTopicCounts() {
        return wordMap.values().stream()
                .collect(Collectors.groupingBy(Word::getTopic, Collectors.summingInt(Word::getTotalCount)));
    }

    /**
     * Returns a count of the number of times the specified word appears with the topic.
     * The return value is a Map that maps topic numbers to their counts for the word.
     *
     * @param word the word to analyze
     * @return a Map of the number of times the specified topic appears with the word
     */
    public Map<Integer, Integer> getWordTopicCount(Word word) {
        return wordMap.values().stream()
                .filter(w -> w.getVocabularyId() == word.getVocabularyId())
                .collect(Collectors.groupingBy(Word::getTopic, Collectors.summingInt(Word::getTotalCount)));
    }

    /**
     * Returns a count of each of the words in the specified topic. The Map returned
     * maps a word vocabulary id to a count of the number of times it appears in the
     * document.
     *
     * @param topic the topic number to count
     * @return a map
     */
    public Map<Integer, Integer> getWordCountsByTopic(int topic) {
        return wordMap.values().stream()
                .filter(word -> word.getTopic() == topic)
                .collect(Collectors.groupingBy(Word::getVocabularyId, Collectors.summingInt(Word::getTotalCount)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SparseDocument that = (SparseDocument) o;

        return wordMap.equals(that.wordMap) && vocabulary.equals(that.vocabulary);
    }

    @Override
    public int hashCode() {
        int result = wordMap.hashCode();
        result = 31 * result + vocabulary.hashCode();
        return result;
    }
}
