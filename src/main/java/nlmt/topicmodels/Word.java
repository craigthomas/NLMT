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

package nlmt.topicmodels;

/**
 * Created by thomas on 01/10/15.
 */
public class Word {

    private int vocabularyId;
    private String rawWord;
    private int totalCount;
    private int topic;

    public Word(String rawWord, int vocabularyId) {
        this.vocabularyId = vocabularyId;
        this.rawWord = rawWord;
        totalCount = 1;
        topic = -1;
    }

    public void setTotalCount(int newCount) {
        totalCount = newCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTopic(int newTopic) {
        topic = newTopic;
    }

    public int getTopic() {
        return topic;
    }

    public String getRawWord() {
        return rawWord;
    }

    public int getVocabularyId() {
        return vocabularyId;
    }
}
