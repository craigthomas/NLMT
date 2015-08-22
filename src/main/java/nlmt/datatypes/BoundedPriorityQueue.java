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
package nlmt.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a priority queue with a set bound on the number of
 * elements that can be in the queue. The ordering of the queue is such
 * that larger priorities occur at the front of the queue, and smaller
 * priorities at the rear. When reading contents of the queue with
 * <code>getElements</code> and <code>getPriorities</code>, the ordering is
 * from the front of the queue (highest priority) to rear of the queue
 * (lowest priority).
 */
public class BoundedPriorityQueue<T> {

    private List<Integer> priorities;
    private List<T> elements;
    private int size;

    public BoundedPriorityQueue(int size) {
        this.size = size + 1;
        priorities = new ArrayList<>(size);
        elements = new ArrayList<>(size);
    }

    /**
     * Add an element to the queue in the correct priority order. If the
     * queue is full and the priority is smaller than existing priorities,
     * the element is not added. If the priority is bigger than existing
     * priorities, the element is added to the correct location, and
     * all other elements are shifted, dropping off the last element.
     *
     * @param priority the priority of the object to add
     * @param id the object to add
     */
    public void add(int priority, T id) {
        if (priorities.isEmpty()) {
            priorities.add(priority);
            elements.add(id);
            return;
        }

        for (int index = 0; index < priorities.size(); index++) {
            if (priority >= priorities.get(index)) {
                priorities.add(index, priority);
                elements.add(index, id);
                if (priorities.size() == size) {
                    priorities.remove(size - 1);
                    elements.remove(size - 1);
                }
                return;
            }
        }

        if (priorities.size() < size - 1) {
            priorities.add(priority);
            elements.add(id);
        }
    }

    /**
     * Returns the list of elements in the queue, sorted by their priorities.
     *
     * @return the list of elements in the queue
     */
    public List<T> getElements() {
        return elements;
    }

    /**
     * Returns the list of priorities in the queue, sorted by their priorities.
     *
     * @return the list of priorities in the queue
     */
    public List<Integer> getPriorities() {
        return priorities;
    }
}
