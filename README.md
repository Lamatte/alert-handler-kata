# Alert handling kata

## Objectives
Your job is to build a "high performance" and robust alarming system, that reacts to network supervision alerts and
aggregate them to raise alarms.
This kata aims at experiencing thread safety and concurrency issues, and to see how to deal with them in a clean way :
clean code, tests, ... 

## The rules

The systems is built on a agents, running on network devices, that ping their neighbours periodically.
When something goes wrong, these agents raise an alert, with the device the agent is located on, and the tested neigghbour.

### Rule 1

As we cannot easily guess the faulty device, on network interface, we were requested to count alerts per devices, in order to
trigger alarms when there are too much.

Note: triggering an actual alarm is not the point in this kata, we simply count alerts for now.

### Rule 2

Also, we were requested to store, for each device, the neighbour device that had the most alerts.
That's a strange requirement actually, but well... 

## The kata

All business rules are properly tested and implemented.
You don't have to change any of that.

Regarding performance, we are plainly satisfied with current throughput, and wish not to degrade it (much...).

Sad enough, we discovered recently that there were race conditions in the code!
A member of our team was able to reproduce the problem in a unit test, but nobody knows the best solution to fix it properly.

Would you be kind enough to try and help having all tests green!

## Language selection

```shell
git checkout <language>
```
where language in (java, go).
