// Disconnect before jsdom destroys its document; production disconnects on reinjection.
afterEach(() => {
  window._translateMutationObserver?.disconnect();
  for (const observer of IntersectionObserver.instances) observer.disconnect();
});
