// A worker is disposable: the UI keeps the last board and can terminate an
// evaluation or generation that does not finish. Never evaluate code on the UI thread.
export class LifeWorker {
  constructor(url, { factory = (url) => new Worker(url), timeout = 3000, startupTimeout = 30000 } = {}) {
    this.timeout = timeout;
    this.nextId = 0;
    this.pending = new Map();
    this.closed = false;
    this.worker = factory(url);
    this.ready = new Promise((resolve, reject) => {
      this.resolveReady = resolve;
      this.rejectReady = reject;
    });
    // A failed candidate may be discarded before its ready promise is awaited.
    this.ready.catch(() => {});
    this.startupTimer = setTimeout(() => this.close(new Error("The ClojureScript runtime did not load. Try Restore Conway.")), startupTimeout);
    this.worker.onmessage = ({ data }) => {
      if (this.closed) return;
      if (data.ready) {
        clearTimeout(this.startupTimer);
        this.resolveReady();
        return;
      }
      const request = this.pending.get(data.id);
      if (!request) return;
      clearTimeout(request.timer);
      this.pending.delete(data.id);
      if (data.error) request.reject(new Error(data.error));
      else request.resolve(data);
    };
    this.worker.onerror = (event) => {
      event.preventDefault?.();
      this.close(new Error(event.message || "The ClojureScript worker stopped."));
    };
  }

  async request(operation, args = {}) {
    await this.ready;
    if (this.closed) throw new Error("Runtime stopped. Choose Restore Conway to recover.");
    const id = ++this.nextId;
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => this.close(new Error("Execution took too long and was stopped. Your last board is preserved.")), this.timeout);
      this.pending.set(id, { resolve, reject, timer });
      try {
        this.worker.postMessage({ ...args, operation, id });
      } catch (error) {
        clearTimeout(timer);
        this.pending.delete(id);
        reject(error);
      }
    });
  }

  close(error = new Error("Runtime replaced.")) {
    if (this.closed) return;
    this.closed = true;
    clearTimeout(this.startupTimer);
    this.worker.terminate();
    this.rejectReady(error);
    for (const request of this.pending.values()) {
      clearTimeout(request.timer);
      request.reject(error);
    }
    this.pending.clear();
  }
}
