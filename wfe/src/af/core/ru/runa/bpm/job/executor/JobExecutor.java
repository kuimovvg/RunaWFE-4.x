package ru.runa.bpm.job.executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;

public class JobExecutor implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(JobExecutor.class);

    private final String name = "JbpmJobExecutor";
    private int size;
    private int idleInterval;
    private int maxIdleInterval;
    private int historyMaxSize;

    private int maxLockTime;
    private int lockMonitorInterval;
    private int lockBufferTime;

    private Map<String, JobExecutorThread> threads = Maps.newHashMap();
    private LockMonitorThread lockMonitorThread;
    private Map<String, Long> monitoredJobIds = Maps.newConcurrentMap();

    private boolean started = false;

    public synchronized void start() {
        if (!started) {
            log.debug("starting thread group '" + name + "'...");
            for (int i = 0; i < size; i++) {
                startThread();
            }
            lockMonitorThread = new LockMonitorThread(lockMonitorInterval, maxLockTime, lockBufferTime);
            started = true;
        } else {
            log.debug("ignoring start: thread group '" + name + "' is already started'");
        }
    }

    /**
     * signals to all threads in this job executor to stop.  It may be that 
     *   threads are in the middle of something and they will finish that firts.
     * @return a list of all the stopped threads.  In case no threads were stopped
     *   an empty list will be returned. 
     */
    public synchronized List<Thread> stop() {
        List<Thread> stoppedThreads = new ArrayList<Thread>(threads.size());
        if (started) {
            log.debug("stopping thread group '" + name + "'...");
            for (int i = 0; i < size; i++) {
                stoppedThreads.add(stopThread());
            }
            lockMonitorThread.deactivate();
            started = false;
        } else {
            log.debug("ignoring stop: thread group '" + name + "' not started");
        }
        return stoppedThreads;
    }

    private synchronized void startThread() {
        String threadName = getNextThreadName();
        JobExecutorThread thread = new JobExecutorThread(threadName, this, idleInterval, maxIdleInterval, maxLockTime, historyMaxSize);
        threads.put(threadName, thread);
        log.debug("starting new job executor thread '" + threadName + "'");
        thread.start();
    }

    private String getNextThreadName() {
        return getThreadName(threads.size() + 1);
    }

    private String getLastThreadName() {
        return getThreadName(threads.size());
    }

    private String getThreadName(int index) {
        return name + ":" + index;
    }

    private synchronized Thread stopThread() {
        String threadName = getLastThreadName();
        JobExecutorThread thread = threads.remove(threadName);
        log.debug("removing job executor thread '" + threadName + "'");
        thread.deactivate();
        return thread;
    }

    public Set<Long> getMonitoredJobIds() {
        return new HashSet<Long>(monitoredJobIds.values());
    }

    public void addMonitoredJobId(String threadName, long jobId) {
        monitoredJobIds.put(threadName, new Long(jobId));
    }

    @Required
    public void setHistoryMaxSize(int historyMaxSize) {
        this.historyMaxSize = historyMaxSize;
    }

    @Required
    public void setIdleInterval(int idleInterval) {
        this.idleInterval = idleInterval;
    }

    @Required
    public void setMaxIdleInterval(int maxIdleInterval) {
        this.maxIdleInterval = maxIdleInterval;
    }

    @Required
    public void setSize(int size) {
        this.size = size;
    }

    @Required
    public void setMaxLockTime(int maxLockTime) {
        this.maxLockTime = maxLockTime;
    }

    @Required
    public void setLockBufferTime(int lockBufferTime) {
        this.lockBufferTime = lockBufferTime;
    }

    @Required
    public void setLockMonitorInterval(int lockMonitorInterval) {
        this.lockMonitorInterval = lockMonitorInterval;
    }

}
