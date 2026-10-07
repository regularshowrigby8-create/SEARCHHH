package info.plateaukao.einkbro.searchhh.web

import info.plateaukao.einkbro.searchhh.JobStatus
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.local.LocalPolicy

/**
 * The result surface rendered by Searchhh's own WebView.
 *
 * This is deliberately a static, escaped document. It does not include crawler
 * code or page scripts; its action links are handled by the hosting
 * Activity's allow-listed `searchhh://` scheme.
 */
object SearchhhResultsHtml {
    const val BASE_URL = "https://searchhh.local/"

    fun render(
        status: JobStatus,
        savedIds: Set<String>,
    ): String {
        val cards = status.results.joinToString("\n") { result -> resultCard(result, result.id in savedIds) }
        val errors =
            status.errors
                .distinct()
                .joinToString("\n") { "<li>${it.escapeHtml()}</li>" }
                .takeIf { it.isNotBlank() }
                ?.let { "<section class=\"notice error\"><strong>Some sources need attention</strong><ul>$it</ul></section>" }
                .orEmpty()
        val empty =
            if (status.results.isEmpty()) {
                "<section class=\"empty\"><div class=\"empty-icon\">⌕</div><h2>No matches in this pass</h2><p>Try a broader topic, select more public portals, or run another pass. Blocked pages and login-only portals stay visible in the source status instead of being bypassed.</p></section>"
            } else {
                ""
            }
        return page(
            title = "Searchhh results",
            body =
                """
                <header class="hero">
                    <div class="eyebrow">SEARCHHH · OPPORTUNITY HIVE</div>
                    <h1>Collected results</h1>
                    <p class="lede">${status.results.size} result${if (status.results.size == 1) "" else "s"} · ${status.duplicates} duplicates removed · pass ${status.round}</p>
                    <div class="status-row"><span class="status-dot"></span><span>${status.status.escapeHtml()}</span><span class="muted">Crawler profile: ${(status.adapter ?: "configured backend").escapeHtml()}</span></div>
                    <div class="status-row"><span class="muted">Results are evidence, not verified application approval.</span></div>
                </header>
                $errors
                $empty
                <main class="results" aria-label="Crawler results">$cards</main>
                """.trimIndent(),
        )
    }

    fun error(message: String): String =
        page(
            title = "Searchhh result error",
            body =
                """
                <section class="empty error-state">
                    <div class="empty-icon">!</div>
                    <h1>Results are temporarily unavailable</h1>
                    <p>${message.escapeHtml()}</p>
                    <a class="button primary" href="searchhh://retry">Retry</a>
                </section>
                """.trimIndent(),
        )

    private fun resultCard(
        result: Opportunity,
        saved: Boolean,
    ): String {
        val url = LocalPolicy.canonical(result.url)
        val evidence = result.evidenceUrl?.let(LocalPolicy::canonical)
        val saveAction = if (saved) "unsave" else "save"
        val saveLabel = if (saved) "Remove from saved" else "Save for later"
        val review =
            result.review
                ?.let {
                    """
                    <div class="review"><strong>AI relevance ${it.relevance}/100 · ${it.model.escapeHtml()}</strong><p>${it.summary.escapeHtml()}</p><blockquote>${it.quote.escapeHtml()}</blockquote>${it.deadlineQuote?.let { quote ->
                        "<small>Deadline evidence: ${quote.escapeHtml()}</small>"
                    }.orEmpty()}${it.eligibilityQuote?.let { quote ->
                        "<small>Eligibility evidence: ${quote.escapeHtml()}</small>"
                    }.orEmpty()}<small>AI-assisted review. Confirm conditions with the official programme.</small></div>
                    """.trimIndent()
                }.orEmpty()
        val evidenceLink = evidence?.let { "<a href=\"${it.escapeHtml()}\">View source evidence ↗</a>" }.orEmpty()
        val openLink =
            url?.let {
                "<a class=\"button primary\" href=\"${it.escapeHtml()}\">Open in WebView ↗</a>"
            } ?: "<span class=\"button disabled\">Invalid source URL</span>"
        return """
                        <article class="card" data-result-id="${result.id.escapeHtml()}">
                            <div class="card-top"><span class="kind">${result.kind.escapeHtml()}</span><span class="score">${result.score}/100</span></div>
                            <h2>${result.title.escapeHtml()}</h2>
                            <p>${result.description.escapeHtml()}</p>
                            <div class="meta"><span>${result.sources.joinToString(
            " · ",
        ).escapeHtml()}</span><span>${(result.published?.take(10) ?: "Date unknown").escapeHtml()}</span></div>
                            $review
                            <details><summary>Details</summary><dl><dt>Result ID</dt><dd>${result.id.escapeHtml()}</dd><dt>Discovered</dt><dd>${result.discovered.escapeHtml()}</dd><dt>Verified</dt><dd>${if (result.verified) "yes" else "no"}</dd></dl></details>
                            <div class="actions">$openLink <a class="button secondary" href="searchhh://$saveAction/${result.id.escapeHtml()}">$saveLabel</a></div>
                            $evidenceLink
                        </article>
            """.trimIndent()
    }

    private fun page(
        title: String,
        body: String,
    ): String =
        """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
            <meta name="color-scheme" content="dark light">
            <title>${title.escapeHtml()}</title>
            <style>
                :root { color-scheme: light dark; --bg:#f6f8f3; --surface:#fff; --ink:#182018; --muted:#667066; --line:#dbe3d7; --accent:#3f6315; --accent-soft:#e2f7b8; --danger:#a33a2a; }
                @media (prefers-color-scheme: dark) { :root { --bg:#101412; --surface:#1a211c; --ink:#eff7ef; --muted:#b0beb0; --line:#344237; --accent:#c5f178; --accent-soft:#293a19; --danger:#ffb4ab; } }
                * { box-sizing:border-box; }
                body { margin:0; min-height:100vh; padding:24px 18px 44px; background:var(--bg); color:var(--ink); font:16px/1.55 -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif; }
                .hero,.results,.notice,.empty { max-width:760px; margin:0 auto; }
                .hero { padding:18px 0 24px; }
                .eyebrow,.kind { color:var(--accent); font-size:12px; font-weight:800; letter-spacing:.12em; text-transform:uppercase; }
                h1 { margin:8px 0 4px; font-size:34px; line-height:1.08; letter-spacing:-.04em; }
                h2 { margin:9px 0; font-size:21px; line-height:1.2; letter-spacing:-.02em; }
                p { margin:0 0 12px; }
                .lede,.muted,.meta,small { color:var(--muted); }
                .status-row { display:flex; align-items:center; flex-wrap:wrap; gap:8px; margin-top:16px; font-size:13px; }
                .status-dot { width:9px; height:9px; border-radius:50%; background:var(--accent); box-shadow:0 0 0 4px var(--accent-soft); }
                .card { margin:14px 0; padding:18px; border:1px solid var(--line); border-radius:18px; background:var(--surface); box-shadow:0 8px 22px rgba(30,55,30,.06); }
                .card-top,.meta,.actions { display:flex; align-items:center; justify-content:space-between; gap:10px; flex-wrap:wrap; }
                .score { color:var(--muted); font-size:13px; font-weight:700; }
                .meta { padding:10px 0; border-top:1px solid var(--line); font-size:12px; }
                .review { margin:12px 0; padding:12px 14px; border-radius:12px; background:var(--accent-soft); }
                blockquote { margin:8px 0; padding-left:12px; border-left:3px solid var(--accent); color:var(--muted); }
                details { margin:12px 0; border-top:1px solid var(--line); padding-top:10px; }
                summary { color:var(--accent); cursor:pointer; font-weight:700; }
                dl { display:grid; grid-template-columns:max-content 1fr; gap:4px 12px; font-size:13px; }
                dt { color:var(--muted); } dd { margin:0; overflow-wrap:anywhere; }
                .button { display:inline-block; padding:9px 13px; border-radius:10px; font-weight:750; text-decoration:none; }
                .button.primary { color:#14200d; background:var(--accent); }
                .button.secondary { color:var(--ink); border:1px solid var(--line); }
                .button.disabled { color:var(--muted); border:1px dashed var(--line); }
                a:not(.button) { color:var(--accent); font-weight:650; }
                .notice { margin-bottom:14px; padding:14px 16px; border:1px solid var(--line); border-left:4px solid var(--danger); border-radius:12px; background:var(--surface); }
                .notice ul { margin:8px 0 0; padding-left:20px; color:var(--muted); }
                .empty { margin-top:26px; padding:38px 18px; text-align:center; border:1px dashed var(--line); border-radius:18px; }
                .empty-icon { display:grid; place-items:center; width:48px; height:48px; margin:0 auto 14px; border-radius:15px; color:#14200d; background:var(--accent); font-size:28px; font-weight:800; }
                .empty p { max-width:560px; margin:0 auto 18px; color:var(--muted); }
            </style>
        </head>
        <body>$body</body>
        </html>
        """.trimIndent()

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
