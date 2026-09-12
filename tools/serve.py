"""Preview the built Codebook with caching disabled, so a rebuild always shows up on refresh.

Usage:  python tools/serve.py [port] [site-directory]
        python tools/serve.py 8967 site      then open http://localhost:8967
"""
import functools
import http.server
import sys


class NoCacheHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cache-Control", "no-store, must-revalidate")
        self.send_header("Expires", "0")
        super().end_headers()

    def log_message(self, format, *args):
        pass


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8967
    directory = sys.argv[2] if len(sys.argv) > 2 else "site"
    handler = functools.partial(NoCacheHandler, directory=directory)
    with http.server.ThreadingHTTPServer(("", port), handler) as server:
        print(f"Serving {directory} at http://localhost:{port} (Ctrl+C to stop)")
        server.serve_forever()


if __name__ == "__main__":
    main()
