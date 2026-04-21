/**
 * Cloudflare Worker - IDLIX Proxy
 * Worker URL: https://idlixm.tepargun.workers.dev
 *
 * CARA UPDATE: Jika domain IDLIX berubah, cukup ganti nilai IDLIX_ORIGIN
 * di bawah dan Save & Deploy ulang. APK tidak perlu dirilis ulang.
 */

const IDLIX_ORIGIN = "https://z1.idlixku.com";

const BROWSER_HEADERS = {
  "User-Agent":
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
  Accept:
    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
  "Accept-Language": "en-US,en;q=0.9,id;q=0.8",
  "Accept-Encoding": "gzip, deflate, br",
  "Cache-Control": "no-cache",
  Pragma: "no-cache",
  "Sec-Fetch-Dest": "document",
  "Sec-Fetch-Mode": "navigate",
  "Sec-Fetch-Site": "none",
  "Sec-Fetch-User": "?1",
  "Upgrade-Insecure-Requests": "1",
};

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // Health check endpoint
    if (url.pathname === "/health") {
      return new Response(JSON.stringify({ status: "ok" }), {
        headers: { "Content-Type": "application/json" },
      });
    }

    const targetUrl = IDLIX_ORIGIN + url.pathname + url.search;

    try {
      const response = await fetch(targetUrl, {
        method: request.method,
        headers: {
          ...BROWSER_HEADERS,
          Cookie: request.headers.get("Cookie") || "",
        },
        redirect: "follow",
      });

      const newResponse = new Response(response.body, response);
      newResponse.headers.set("Access-Control-Allow-Origin", "*");
      newResponse.headers.set("Access-Control-Allow-Methods", "GET, OPTIONS");
      newResponse.headers.delete("X-Frame-Options");
      newResponse.headers.delete("Content-Security-Policy");

      return newResponse;
    } catch (error) {
      return new Response(
        JSON.stringify({ error: "Proxy error: " + error.message }),
        {
          status: 502,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        }
      );
    }
  },
};
