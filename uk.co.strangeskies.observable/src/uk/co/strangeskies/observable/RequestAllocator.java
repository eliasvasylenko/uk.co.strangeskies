package uk.co.strangeskies.observable;

import static java.util.Comparator.comparing;

import java.util.List;

@FunctionalInterface
public interface RequestAllocator {
  long allocateRequests(long requestCount, List<Observation> observations);

  static RequestAllocator balanced() {
    return (count, observations) -> {
      if (count == Long.MAX_VALUE) {
        observations.forEach(o -> o.request(Long.MAX_VALUE));

      } else {
        for (int i = 0; i < observations.size() && count > 0; i++) {
          Observation observation = observations.get(i);
          if (observation.getPendingRequestCount() == 0) {
            observation.requestNext();
            count--;
          }
        }
      }

      return count;
    };
  }

  static RequestAllocator sequential() {
    return (count, observations) -> {
      observations.get(0).request(count);
      return count == Long.MAX_VALUE ? Long.MAX_VALUE : 0;
    };
  }

  static RequestAllocator spread() {
    return (count, observations) -> {
      if (count == Long.MAX_VALUE) {
        observations.forEach(o -> o.request(Long.MAX_VALUE));
        return count;

      } else {
        observations.sort(comparing(Observation::getPendingRequestCount));

        int observationsUnderBaseline = observations.size();
        long pendingUnderBaseline = observations
            .stream()
            .mapToLong(Observation::getPendingRequestCount)
            .sum();

        long newRequestBaseline;
        do {
          newRequestBaseline = (pendingUnderBaseline + count) / observationsUnderBaseline;

          long maximumPendingRequests = observations
              .get(observationsUnderBaseline - 1)
              .getPendingRequestCount();

          if (maximumPendingRequests > newRequestBaseline) {
            pendingUnderBaseline -= maximumPendingRequests;
            observationsUnderBaseline--;
          } else {
            break;
          }
        } while (true);

        for (int i = 0; i < observationsUnderBaseline; i++) {
          Observation observation = observations.get(i);
          long fulfilled = newRequestBaseline - observation.getPendingRequestCount();
          observation.request(fulfilled);
          count -= fulfilled;
        }

        for (int i = 0; i < count; i++) {
          observations.get(i).requestNext();
        }

        return 0;
      }
    };
  }
}
