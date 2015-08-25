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

import nlmt.datatypes.BoundedPriorityQueue;
import nlmt.probfunctions.PMFSampler;

import java.util.*;
import java.util.stream.Collectors;

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
    // topicDocumentCount[documentIndex][topicIndex]
    protected int [][] documentTopicCount;

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

    // Used to sampler from a probability mass function
    private PMFSampler pmfSampler;

    public LDAModel(int numTopics) {
        this(numTopics, 0, 0);
    }

    public LDAModel(int numTopics, double alpha, double beta) {
        if (numTopics <= 0) {
            throw new IllegalArgumentException("numTopics must be > 0");
        }
        if (alpha < 0) {
            throw new IllegalArgumentException("alpha must be >= 0");
        }
        if (beta < 0) {
            throw new IllegalArgumentException("beta must be >= 0");
        }
        this.alpha = (alpha == 0.0) ? 0.5 : alpha;
        this.beta = (beta == 0.0) ? 0.1 : beta;
        this.numTopics = numTopics;
        vocabulary = new Vocabulary();
        documents = new Document[0];
        random = new Random();
        pmfSampler = new PMFSampler(numTopics);
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
        documentTopicCount = new int[totalDocs][numTopics];
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
     * Given the topic counts for each word, the topic count for the specified document,
     * calculate the mass associated with this word and topic combination.

     * @param topicDocumentCount the topic composition of the document
     * @param wordIndexInVocab the word number in the vocabulary
     * @param topicIndex the topic number
     * @return the mass belonging to the word
     */
    protected double getTopicWeight(int topicDocumentCount, int wordIndexInVocab, int topicIndex) {
        double denominator = topicTotals[topicIndex] + (topicTotals[topicIndex] * beta);
        if (denominator == 0.0) {
            return 0.0;
        }
        return ((wordTopicCount[wordIndexInVocab][topicIndex] + beta) / denominator) *
                (topicDocumentCount + alpha);
    }

    /**
     * Determines what topic the word should belong to given the current topic counts
     * in the specified document.
     *
     * @param wordIndexInVocab the word in the vocabulary to check
     * @param topicCounts the topic totals for the document
     * @return the new topic
     */
    protected int getNewTopic(int wordIndexInVocab, int [] topicCounts) {
        pmfSampler.clear();
        for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
            double weight = getTopicWeight(topicCounts[topicIndex], wordIndexInVocab, topicIndex);
            pmfSampler.add(weight);
        }
        return pmfSampler.sample();
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
        documentTopicCount[documentIndex][topicIndex]--;
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
        documentTopicCount[documentIndex][topicIndex]++;
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
                    int newTopic = getNewTopic(words[wordIndexInDoc], documentTopicCount[documentIndex]);
                    addTopicToWord(documentIndex, words[wordIndexInDoc], wordIndexInDoc, newTopic);
                }
            }
        }
    }

    /**
     * Returns the top <code>numWords</code> that best describe the topic.
     *
     * @param topicIndex the topic to scan
     * @param numWords the number of words to return
     * @return an List of Strings that are the words that describe the topic
     */
    public List<String> getTopWordsForTopic(int topicIndex, int numWords) {
        if (topicIndex >= numTopics  || topicIndex < 0) {
            throw new IllegalArgumentException("topicIndex must be >= 0 and < numTopics");
        }
        if (numWords <= 0) {
            throw new IllegalArgumentException("numWords must be > 0");
        }
        BoundedPriorityQueue<Integer> priorityQueue = new BoundedPriorityQueue<>(numWords);
        for (int wordIndex = 0; wordIndex < vocabulary.size(); wordIndex++) {
            priorityQueue.add(wordTopicCount[wordIndex][topicIndex], wordIndex);
        }
        return priorityQueue.getElements().stream().map(vocabulary::getWordFromIndex).collect(Collectors.toList());
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
            result.add(getTopWordsForTopic(topic, numWords));
        }
        return result;
    }

    /**
     * Returns the mixture of topics for the specified document. The array indices
     * represent the topic numbers.
     *
     * @param documentIndex the index of the document to check
     * @return the mixture of topics for the document
     */
    public double [] getTopicMixtureForDocument(int documentIndex) {
        if (documentIndex < 0 || documentIndex >= documents.length) {
            throw new IllegalArgumentException("documentIndex must be >= 0 and < number of documents");
        }
        PMFSampler documentPMFSampler = new PMFSampler(numTopics);
        for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
            documentPMFSampler.add((((double)documentTopicCount[documentIndex][topicIndex]) + alpha));
        }
        return documentPMFSampler.getProbabilities();
    }

    public double [] inference(List<String> document, int numIterations) {
        Vocabulary newVocabulary = new Vocabulary();
        Document newDocument = new Document(newVocabulary);
        newDocument.readDocument(document);

        int [] localTopicDocumentCount = new int[numTopics];
        String [] rawWords = newDocument.getRawWords();
        int [] words = newDocument.getWordArray();

        // Randomly assign topics to the words in the document
        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            int newTopic = random.nextInt(numTopics);
            localTopicDocumentCount[newTopic]++;
            newDocument.setTopicForWord(wordIndex, random.nextInt(numTopics));
        }

        // Loop and perform Gibbs Sampling, but clamp the global word and topic counts
        for (int iteration = 0; iteration < numIterations; iteration++) {
            for (int wordIndexInDoc = 0; wordIndexInDoc < words.length; wordIndexInDoc++) {
                newDocument.setTopicForWord(wordIndexInDoc, -1);
                if (vocabulary.contains(rawWords[wordIndexInDoc])) {
                    int newTopic = getNewTopic(words[wordIndexInDoc], localTopicDocumentCount);
                    newDocument.setTopicForWord(wordIndexInDoc, newTopic);
                }
            }
        }

        // Figure out what the distribution of topics are
        PMFSampler documentPMFSampler = new PMFSampler(numTopics);
        for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
            documentPMFSampler.add((((double)localTopicDocumentCount[topicIndex]) + alpha));
        }
        return documentPMFSampler.getProbabilities();
    }
}
