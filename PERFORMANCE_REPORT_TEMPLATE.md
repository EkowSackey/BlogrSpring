# Performance Engineering Report: Blogr API

## 📅 1. Test Overview
- **Date**: 2026-03-03
- **Environment**: Localhost (JDK 17, MongoDB 6.0)
- **Tools**: Apache JMeter 5.x, VisualVM 2.2.1
- **Scenario**: Baseline Test (Healthy State) - 5 Users, 100 Loops (1000 Total Samples)

---

## 📉 2. Baseline Performance (Pre-Optimization)

### 2.1 JMeter Summary Report
| Endpoint | Avg Latency (ms) | p95 Latency (ms) | Throughput (RPS) | Error % |
| :--- | :--- | :--- | :--- | :--- |
| `GET /api/v1/posts` | 5.46 | 7.00 | 27.74 | 0.00% |
| `POST /api/v1/users/auth/login` | 94.70 | 104.00 | 27.48 | 0.00% |
| **Total** | **50.08** | **100.00** | **54.94** | **0.00%** |

### 2.2 VisualVM Observations
- **Peak Heap Usage**: ~110 MB (during test), stabilized at **49 MB** post-GC.
- **CPU Usage (Peak)**: ~50% during active sampling.
- **GC Behavior**: Healthy "sawtooth" pattern observed. Memory successfully reclaimed after test completion, indicating no immediate leaks.
- **Top Memory Objects**: `byte[]` (21.6%) and `java.util.TreeMap$Entry` (10.5%).

---

## 🔍 3. Bottleneck Identification (Initial)
- **Authentication Overhead**: The Login endpoint is significantly slower (94ms) than data retrieval (5ms). This is the primary CPU consumer due to password hashing.
- **TreeMap Usage**: High `TreeMap$Entry` count suggests potential overhead in caching structures or MongoDB result mapping that should be monitored under higher load.

---

## 🛠 4. Applied Optimizations
*(To be completed after Stress/Endurance tests)*

---

## 🚀 5. Post-Optimization Results
*(To be completed after final validation)*

---

## ✅ 6. Conclusion & Recommendations
- **Current Status**: The system is highly responsive for basic read operations (likely due to Caffeine caching).
- **Recommendation**: Proceed to **Stage B (Stress Test)** including Analytics endpoints to evaluate MongoDB aggregation performance under concurrent load.
