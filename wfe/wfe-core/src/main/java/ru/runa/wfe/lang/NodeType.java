package ru.runa.wfe.lang;

public enum NodeType {
    StartState, ActionNode, End, WaitState, TaskNode, Fork, Join, Decision, Subprocess, MultiSubprocess, SendMessage, ReceiveMessage, EndToken, MultiTaskNode
}