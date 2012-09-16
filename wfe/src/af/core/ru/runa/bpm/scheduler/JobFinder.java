package ru.runa.bpm.scheduler;

import java.util.TimerTask;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JobFinder extends TimerTask {

    @Override
    public void run() {
        System.out.println("TODO: check jobs");
    }
    
}
