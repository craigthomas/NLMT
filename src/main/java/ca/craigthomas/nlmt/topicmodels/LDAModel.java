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
package ca.craigthomas.nlmt.topicmodels;

import ca.craigthomas.nlmt.datatypes.BoundedPriorityQueue;
import ca.craigthomas.nlmt.datatypes.Document;
import ca.craigthomas.nlmt.datatypes.IdentifierObjectMapper;
import ca.craigthomas.nlmt.probfunctions.PMFSampler;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple implementation of Latent Dirichlet Allocation using Gibbs
 * Sampling.
 */
public class LDAModel implements Serializable
{
    // The number of topics to produce, topicIndex will range from
    // 0 to numTopics
    protected int numTopics;

    // The alpha smoothing parameter
    protected double alpha;

    // The beta smoothing parameter
    protected double beta;

    // Beta multiplied by the size of the vocabulary
    protected double betaTotal;

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
    protected Document[] documents;

    // Associates a word with a specific number, giving us wordIndexInVocab
    protected IdentifierObjectMapper<String> vocabulary;

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
        vocabulary = new IdentifierObjectMapper<>();
        documents = new Document[0];
        random = new Random();
        pmfSampler = new PMFSampler(numTopics);
        documentTopicCount = new int[0][numTopics];
        wordTopicCount = new int[0][numTopics];
        topicTotals = new int[numTopics];
    }

    /**
     * Read in a list of documents. Each document is assumed to be a
     * list of Strings. Stop-words and other pre-processing should be
     * done prior to reading documents. The documents should be read before
     * running doGibbsSampling.
     *
     * @param documents the List of documents, each being a List of Strings
     */
    public void readDocuments(List<List<String>> documents) {
        this.documents = new Document[documents.size()];

        for (int i = 0; i < documents.size(); i++) {
            this.documents[i] = new Document(vocabulary);
            this.documents[i].readDocument(documents.get(i), true);
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
        betaTotal = vocabulary.size() * numTopics;

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
        double denominator = topicTotals[topicIndex] + betaTotal;
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
     * Runs Gibbs Sampling without any heartbeat.
     *
     * @param numIterations the number of times to perform Gibbs Sampling
     */
    public void doGibbsSampling(int numIterations) {
        doGibbsSampling(numIterations, false);
    }

    /**
     * Loops for the specified number of iterations, removing the word from
     * the topic, calculating what topic it should belong to (by performing
     * sampling of the topic distributions), and adds word to the newly sampled topic.
     *
     * @param numIterations the number of times to perform Gibbs Sampling
     * @param heartbeat whether to print progress information to standard out
     */
    public void doGibbsSampling(int numIterations, boolean heartbeat) {
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

            if (heartbeat) {
                System.out.print(".");
                if (i % 100 == 0 && i != 0) {
                    System.out.println(i);
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
        return priorityQueue.getElements().stream().map(vocabulary::getObjectFromIndex).collect(Collectors.toList());
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

    /**
     * Infer what the topic distribution should be for the given document.
     * Returns an array representing the topics found within the document.
     * For example, assuming there were 3 topics:
     *
     * result[0] = 0.87
     * result[1] = 0.10
     * result[2] = 0.03
     *
     * This means that the document is composed of 87% of topic 1,
     * 10% of topic 2, and 3% of topic 3. Words that have not been seen before
     * by the model are ignored when determining topic distributions.
     *
     * @param document the new document to infer
     * @param numIterations the number of iterations to perform the inference on
     * @return an array of topic distributions
     */
    public double [] inference(List<String> document, int numIterations) {
        // If the document is empty, then the topic distributions are empty
        if (document.size() == 0) {
            return new double[numTopics];
        }

        Document newDocument = new Document(vocabulary);
        newDocument.readDocument(document, false);
        int [] localTopicDocumentCount = new int[numTopics];
        int [] words = newDocument.getWordArray();

        // Randomly assign word to topic, checking to make sure the word is in the
        // global vocabulary
        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            if (words[wordIndex] < wordTopicCount.length) {
                int newTopic = random.nextInt(numTopics);
                newDocument.setTopicForWord(wordIndex, newTopic);
                localTopicDocumentCount[newTopic]++;
            }
        }

        // Loop and perform Gibbs Sampling, but do not update the global word and topic counts
        for (int iteration = 0; iteration < numIterations; iteration++) {
            int [] topics = newDocument.getTopicArray();
            for (int wordIndexInDoc = 0; wordIndexInDoc < words.length; wordIndexInDoc++) {
                localTopicDocumentCount[topics[wordIndexInDoc]]--;
                newDocument.setTopicForWord(wordIndexInDoc, -1);
                int newTopic = getNewTopic(words[wordIndexInDoc], localTopicDocumentCount);
                newDocument.setTopicForWord(wordIndexInDoc, newTopic);
                localTopicDocumentCount[newTopic]++;
            }
        }

        // Figure out what the distribution of topics should be
        PMFSampler documentPMFSampler = new PMFSampler(numTopics);
        boolean allEmpty = true;
        for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
            if (localTopicDocumentCount[topicIndex] <= 0) {
                documentPMFSampler.add(0.0);
            } else {
                documentPMFSampler.add((((double) localTopicDocumentCount[topicIndex]) + alpha));
                allEmpty = false;
            }
        }
        return (allEmpty) ? new double[numTopics] : documentPMFSampler.getProbabilities();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if ((o == null) || (getClass() != o.getClass())) { return false; }

        LDAModel ldaModel = (LDAModel) o;

        if (numTopics != ldaModel.numTopics) { return false; }
        if (Double.compare(ldaModel.alpha, alpha) != 0) { return false; }
        if (Double.compare(ldaModel.beta, beta) != 0) { return false; }
        if (Double.compare(ldaModel.betaTotal, betaTotal) != 0) { return false; }
        if (!Arrays.deepEquals(documentTopicCount, ldaModel.documentTopicCount)) { return false; }
        if (!Arrays.deepEquals(wordTopicCount, ldaModel.wordTopicCount)) { return false; }
        if (!Arrays.equals(topicTotals, ldaModel.topicTotals)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(documents, ldaModel.documents) && vocabulary.equals(ldaModel.vocabulary);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = numTopics;
        temp = Double.doubleToLongBits(alpha);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(beta);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(betaTotal);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.deepHashCode(documentTopicCount);
        result = 31 * result + Arrays.deepHashCode(wordTopicCount);
        result = 31 * result + Arrays.hashCode(topicTotals);
        result = 31 * result + Arrays.hashCode(documents);
        result = 31 * result + vocabulary.hashCode();
        return result;
    }
}
