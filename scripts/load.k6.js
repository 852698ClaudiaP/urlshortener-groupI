import http from "k6/http";
import { check, sleep } from "k6";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.1.0/index.js";

/**
 * Host or Compose load profile against the product LB.
 *
 * Host:   BASE_URL=http://localhost:8080 k6 run scripts/load.k6.js
 * Compose: docker compose --profile load run --rm k6
 *
 * load-compare evidence: run N=1 vs N=2 replicas, compare RPS + p95 + error rate.
 */
export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: __ENV.DURATION || "30s",

  // ─────────────────────────────────────────────────────────────────────────
  // THRESHOLDS — Uncomment and tune for your load-compare evidence gates
  // Docs: https://grafana.com/docs/k6/latest/using-k6/thresholds/
  // ─────────────────────────────────────────────────────────────────────────
  // thresholds: {
  //   http_req_failed: ["rate<0.01"],        // <1% errors
  //   http_req_duration: ["p(95)<500"],      // 95th percentile < 500ms
  //   http_reqs: ["rate>50"],                // >50 RPS sustained
  //   checks: ["rate>0.99"],                 // >99% checks pass
  // },

  // ─────────────────────────────────────────────────────────────────────────
  // SCENARIOS — Uncomment for ramp-up / spike / soak patterns
  // Docs: https://grafana.com/docs/k6/latest/using-k6/scenarios/
  // ─────────────────────────────────────────────────────────────────────────
  // scenarios: {
  //   ramp: {
  //     executor: "ramping-vus",
  //     startVUs: 1,
  //     stages: [
  //       { duration: "30s", target: 10 },
  //       { duration: "1m", target: 10 },
  //       { duration: "10s", target: 0 },
  //     ],
  //   },
  // },
};

const BASE = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
  const create = http.post(`${BASE}/api/link`, { url: "https://example.com/load" });
  check(create, { "create 201": (r) => r.status === 201 });
  let hash = null;
  try {
    hash = create.json("hash");
  } catch (_) {}
  if (hash) {
    const redirect = http.get(`${BASE}/${hash}`, { redirects: 0 });
    check(redirect, { "redirect 307": (r) => r.status === 307 });
  }
  sleep(0.2);
}

/**
 * Summary output — save JSON for CI artifact or interpretation.
 * Override: K6_SUMMARY_EXPORT=results.json k6 run ...
 */
export function handleSummary(data) {
  const out = { stdout: textSummary(data, { indent: " ", enableColors: true }) };
  if (__ENV.K6_SUMMARY_EXPORT) {
    out[__ENV.K6_SUMMARY_EXPORT] = JSON.stringify(data, null, 2);
  }
  return out;
}
