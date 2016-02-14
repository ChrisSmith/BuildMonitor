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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestResult that = (TestResult) o;

        if (Order != that.Order) return false;
        if (DurationMs != that.DurationMs) return false;
        if (!Name.equals(that.Name)) return false;
        return Status.equals(that.Status);

    }

    @Override
    public int hashCode() {
        int result = Name.hashCode();
        result = 31 * result + Status.hashCode();
        result = 31 * result + Order;
        result = 31 * result + DurationMs;
        return result;
    }
}
