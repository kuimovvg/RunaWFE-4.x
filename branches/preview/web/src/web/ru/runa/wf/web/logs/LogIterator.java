package ru.runa.wf.web.logs;

import java.util.Iterator;
import java.util.List;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.logging.log.ProcessLog;

/**
 * Bidirectional iterator via {@link List} of logs. Provides access to current iterator element
 */
class LogIterator implements Iterator<ProcessLog> {

    /**
     * {@link List} of iterated process logs.
     */
    private final List<ProcessLog> logs;

    /**
     * Current iteration index.
     */
    private int idx = -1;

    public LogIterator(List<ProcessLog> logs) {
        this.logs = logs;
    }

    @Override
    public boolean hasNext() {
        return !logs.isEmpty() && idx < logs.size() - 1;
    }

    /**
     * Check, if iterator can be moved back.
     * 
     * @return true, if iterator can be moved back and false, if iterator points to first element or iteration not started.
     */
    public boolean hasPrev() {
        return !logs.isEmpty() && idx > 0;
    }

    @Override
    public ProcessLog next() {
        if (!hasNext()) {
            throw new InternalApplicationException("Iterator can't be moved forward.");
        }
        idx++;
        return logs.get(idx);
    }

    /**
     * Advice iterator to previous element and returns it.
     * 
     * @return {@link ProcessLog}, pointed by iterator after moved back.
     */
    public ProcessLog prev() {
        if (!hasPrev()) {
            throw new InternalApplicationException("Iterator can't be moved backward.");
        }
        idx--;
        return logs.get(idx);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns current element, pointed by iterator.
     * 
     * @return {@link ProcessLog}, pointed by iterator.
     */
    public ProcessLog current() {
        if (idx < 0 || idx >= logs.size()) {
            throw new InternalApplicationException("Iteration not started.");
        }
        return logs.get(idx);
    }
}
