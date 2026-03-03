# Blogr Performance Engineering & Load Testing Strategy

## 📈 1. Multi-Stage Testing Roadmap

### Stage A: Baseline (Pre-Optimization)
- **Goal**: Establish "Healthy State" metrics for all operation types.
- **Config**: 5 Users, 100 Loops.
- **Endpoints**:
  1. `POST /api/v1/users/auth/login` (Auth/CPU)
  2. `GET /api/v1/posts` (Read/List)
  3. `GET /api/v1/posts/{id}` (Read/Single)
  4. `POST /api/v1/posts` (Write/DB)
  5. `GET /api/v1/analytics/top-authors` (Aggregation/Heavy)

### Stage B: Stress & Endurance
- **Stress**: Ramp up to 100-200 users. Find the "Knee" in the curve where latency spikes.
- **Endurance**: 20 users for 20 minutes. Monitor VisualVM for memory leaks (rising heap floor).

### Stage C: Post-Optimization Validation
- **Goal**: Re-run Stage A & B after applying Async/Caching/Indexing.
- **Success**: Target 30-50% reduction in latency for Analytics and Auth.

---

## 🛠 2. Iterative Improvement Workflow
1. **Baseline**: Run Stage A. Save results as "Pre-Optimization".
2. **Profile**: Use VisualVM Sampler during Stage B to find the slowest methods.
3. **Optimize**:
   - Implement `@Async` for non-blocking operations.
   - Add MongoDB Indexes for aggregation fields.
   - Tune Caffeine cache TTL/Size.
4. **Validate**: Run Stage A again. Compare results.
