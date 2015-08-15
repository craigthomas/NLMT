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
package nlmt.models;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Simple implementation of Latent Dirichlet Allocation using Gibbs
 * Sampling.
 */
public class LDAModel
{
    // The number of topics to produce, topicIndex will range from
    // 0 to numTopics
    protected int numTopics;

    // The alpha smoothing parameter
    protected double alpha;

    // The beta smoothing parameter
    protected double beta;

    // Keeps track of how many documents have been assigned to a topic
    // topicDocumentCount[topicIndex][documentIndex]
    protected int [][] topicDocumentCount;

    // Keeps track of how many of each topic has been assigned to a word
    // wordTopicCount[wordIndexInVocab][topicIndex]
    protected int [][] wordTopicCount;

    // Keeps track of the total number of words assigned to a topic
    // topicTotals[topicIndex]. This prevents us from having to loop
    // through the wordTopicCount to calculate the totals for each
    // topic
    protected int [] topicTotals;

    // Keeps track of what topics have been assigned to the word in a document,
    // associates each word with an index giving us wordIndexInDoc. The
    // documentIndex will range from 0 to the total number of documents
    protected Document [] documents;

    // Associates a word with a specific number, giving us wordIndexInVocab
    protected Vocabulary vocabulary;

    // Used to produce random numbers
    private Random random;

    public LDAModel(int numTopics) {
        this(numTopics, 0, 0);
    }

    public LDAModel(int numTopics, double alpha, double beta) {
        if (numTopics <= 0) {
            throw new IllegalArgumentException("numTopics must be > 0");
        }
        if (alpha < 0) {
            throw new IllegalArgumentException("alpha must be > 0");
        }
        if (beta < 0) {
            throw new IllegalArgumentException("beta must be > 0");
        }
        this.alpha = (alpha == 0.0) ? 0.5 : alpha;
        this.beta = (beta == 0.0) ? 0.1 : beta;
        this.numTopics = numTopics;
        vocabulary = new Vocabulary();
        documents = new Document[0];
        random = new Random();
    }

    /**
     * Read in a list of documents. Each document is assumed to be a
     * list of Strings. Stopwords and other pre-processing should be
     * done prior to reading documents. The documents should be read before
     * running doGibbsSampling.
     *
     * @param documents the List of documents, each being a List of Strings
     */
    public void readDocuments(List<List<String>> documents) {
        this.documents = new Document[documents.size()];

        for (int i = 0; i < documents.size(); i++) {
            this.documents[i] = new Document(vocabulary);
            this.documents[i].readDocument(documents.get(i));
        }
    }

    /**
     * Initialize all of the counters. Simply loop through every word in
     * every document, and assign it a random topic. Update the global
     * counters to reflect the random assignments.
     */
    public void initialize() {
        int totalDocs = documents.length;
        topicDocumentCount = new int[numTopics][totalDocs];
        wordTopicCount = new int[vocabulary.size()][numTopics];
        topicTotals = new int[numTopics];

        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
            int [] words = documents[documentIndex].getWordArray();
            for (int wordIndexInDoc = 0; wordIndexInDoc < words.length; wordIndexInDoc++) {
                int topic = random.nextInt(numTopics);
                addTopicToWord(documentIndex, words[wordIndexInDoc], wordIndexInDoc, topic);
            }
        }
    }

    /**
     * Calculates p(topicIndex). In other words, given the topic counts for each word, the
     * topic counts for each document, calculate the probability that this word should belong
     * to the specified topic. This is the sampling part of the Gibbs Sampler.
     *
     * @param documentIndex the document number
     * @param wordIndexInVocab the word number in the vocabulary
     * @param topicIndex the topic number
     * @return the probability that the word should be in the topicIndex
     */
    public double getTopicProbability(int documentIndex, int wordIndexInVocab, int topicIndex) {
        double result = wordTopicCount[wordIndexInVocab][topicIndex] + beta;
        result /= (topicTotals[topicIndex] + (topicTotals[topicIndex] * beta));
        result *= (topicDocumentCount[topicIndex][documentIndex] + alpha);
        return result;
    }

    /**
     * Determines what topic the word should belong to given the document and the
     * word in the vocabulary.
     *
     * @param documentIndex the document number
     * @param wordIndexInVocab the word number in the vocabulary
     * @return the best topic number
     */
    protected int getNewTopic(int documentIndex, int wordIndexInVocab) {
        int bestTopic = -1;
        double bestProbability = -99999;
        for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
            double probability = getTopicProbability(documentIndex, wordIndexInVocab, topicIndex);
            if (probability > bestProbability) {
                bestProbability = probability;
                bestTopic = topicIndex;
            }
        }
        return bestTopic;
    }

    /**
     * Removes the word in the document from the topic. Updates the counter
     * for the vocabulary to remove one instance of the word from the topic.
     * Also updates the counter for the documents to remove the document from
     * the counter, and remove the word from the topic totals. Updates the
     * topicIndex in the actual document to be -1.
     *
     * @param documentIndex the document number to update
     * @param wordIndexInVocab the vocabulary word number to update
     * @param wordIndexInDoc the document word number to update
     * @param topicIndex the topic number to update
     */
    protected void removeTopicFromWord(int documentIndex, int wordIndexInVocab, int wordIndexInDoc, int topicIndex) {
        documents[documentIndex].setTopicForWord(wordIndexInDoc, -1);
        topicDocumentCount[topicIndex][documentIndex]--;
        wordTopicCount[wordIndexInVocab][topicIndex]--;
        topicTotals[topicIndex]--;
    }

    /**
     * Adds the word in the document to the topic. Updates the counter
     * for the vocabulary to add one instance of the word to the topic.
     * Also updates the counter for the documents to add the document to
     * the counter, and add the word to the topic totals. Updates the
     * topicIndex in the actual document to be the new topicIndex.
     *
     * @param documentIndex the document number to update
     * @param wordIndexInVocab the vocabulary word number to update
     * @param wordIndexInDoc the document word number to update
     * @param topicIndex the topic number to update
     */
    protected void addTopicToWord(int documentIndex, int wordIndexInVocab, int wordIndexInDoc, int topicIndex) {
        documents[documentIndex].setTopicForWord(wordIndexInDoc, topicIndex);
        topicDocumentCount[topicIndex][documentIndex]++;
        wordTopicCount[wordIndexInVocab][topicIndex]++;
        topicTotals[topicIndex]++;
    }

    /**
     * Loops for the specified number of iterations, removing the word from
     * the topic, calculating what topic it should belong to (by performing
     * sampling of the topic distributions), and adds word to the newly sampled topic.
     *
     * @param numIterations the number of times to perform Gibbs Sampling
     */
    public void doGibbsSampling(int numIterations) {
        initialize();
        for (int i = 0; i < numIterations; i++) {
            for (int documentIndex = 0; documentIndex < documents.length; documentIndex++) {
                int [] words = documents[documentIndex].getWordArray();
                int [] topics = documents[documentIndex].getTopicArray();
                for (int wordIndexInDoc = 0; wordIndexInDoc < words.length; wordIndexInDoc++) {
                    removeTopicFromWord(documentIndex, words[wordIndexInDoc], wordIndexInDoc, topics[wordIndexInDoc]);
                    int newTopic = getNewTopic(documentIndex, words[wordIndexInDoc]);
                    addTopicToWord(documentIndex, words[wordIndexInDoc], wordIndexInDoc, newTopic);
                }
            }
        }
    }

    /**
     * Returns the list of words that describe each topic. The index into
     * the returned list is the topic number. For example, the second
     * index (index 1) is the second topic.
     *
     * @param numWords the number of words to fetch
     * @return the List of topics, each with a List of Strings that are the words for that topic
     */
    public List<List<String>> getTopics(int numWords) {
        List<List<String>> result = new ArrayList<>();
        for (int topic = 0; topic < numTopics; topic++) {
            List<String> words = new ArrayList<>();
            PriorityQueue<Pair<Integer, Integer>> maxQueue = new PriorityQueue<>(numWords, (object1, object2) ->
                    ((object1.getLeft() < object2.getLeft()) ? 1 : ((object1.getLeft() > object2.getLeft()) ? -1 : 0)));
            for (int wordIndex = 0; wordIndex < vocabulary.size(); wordIndex++) {
                maxQueue.add(Pair.of(wordTopicCount[wordIndex][topic], wordIndex));
            }
            while (maxQueue.size() > 0) {
                Pair<Integer, Integer> pair = maxQueue.remove();
                words.add(vocabulary.getWordFromIndex(pair.getRight()));
            }
            result.add(words);
        }
        return result;
    }
}
