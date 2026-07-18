const NOW = Date.now();

export function minutesAgo(n: number): string {
  return new Date(NOW - n * 60_000).toISOString();
}

export function hoursAgo(n: number): string {
  return new Date(NOW - n * 60 * 60_000).toISOString();
}

export function daysAgo(n: number): string {
  return new Date(NOW - n * 24 * 60 * 60_000).toISOString();
}
