package org.collegelabs.buildmonitor.buildmonitor2.tests;

public class TestResult {
    public String Name, Status;
    public int Order, DurationMs;

    public TestResult(int order, String name, String status, int duration){
        this.Order = order;
        this.Name = name;
        this.Status = status;
        this.DurationMs = duration;
    }

    @Override
    public String toString() {
        return "["+this.Status+"] " + this.Name;
    }
}
