# Benchmark Test Explanation

## Explain the Why

### Why a Benchmark Test?

Unit tests verify correctness (`assertEquals`). Benchmark tests verify performance characteristics (`assertTrue(speedup > 1.5)`). This serves as a **regression test** against accidental serialization (e.g., putting a synchronized block in the wrong place).

### Why 100ms delay?

It's long enough to dominate the overhead of thread creation, but short enough to keep the test suite fast.

## Potential Reviewer Questions

**Q: Is this a microbenchmark?**

**A:** Yes, and microbenchmarks in Java are notoriously hard due to JIT compilation/warmup. This is a "coarse" benchmark. For scientific precision, we would use **JMH (Java Microbenchmark Harness)**, but for this phase, a simple wall-clock comparison suffices to prove the architectural concept.
