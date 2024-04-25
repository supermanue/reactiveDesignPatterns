# Heartbeat Pattern

Chapter 12.3.5

## Intro

This pattern is a corollary of the Let It Crash one.

Here we have a heartbeat between the coordinator of the Execution and the Workers. If the workers don't sena signal in
the acknowledged time, they are killed and removed from the workers list and a new one is set in place.
