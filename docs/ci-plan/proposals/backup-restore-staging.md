# PROPOSAL (not applied): make legacy backup restore atomic, and stop it destroying the profile it failed to restore

**Status: proposal only. No file outside `docs/ci-plan/proposals/` was changed by this note or the patch beside it.**

Found while doing the second-pass defect hunt recorded in `docs/ci-plan/debt-inventory.md` §13 — it is
not a lint/Detekt finding and would never have surfaced from a rule count.

## The defect

`BackupUnit.restoreLegacyBackupData` (`app/src/main/java/info/plateaukao/einkbro/unit/BackupUnit.kt:605`)

```kotlin
bookmarkManager.database.close()          // 1. releases the live database first
...
writeStreamToFile(zis, file)              // 2. then streams each zip entry straight onto the live file
```

1. The Room database is closed **before** the archive is even opened, so a backup that cannot be read
   at all (unreadable URI, `null` stream → `return false`) still leaves the app holding a closed
   database.
2. Each entry is written directly to its destination — `File(databasesDir, name)` or
   `File(sharedPrefsDir, …)`. `IOException` halfway through (disk full, truncated zip) is caught and
   turned into `return false`, but by then the earlier entries have already replaced the live
   database/preferences. **"Restore failed" therefore means "your profile is now the corrupt one"**,
   not "nothing happened".
3. A backup carrying only the main `.db` is swapped in **beside the previous `-wal`/`-shm` files**.
   SQLite applies a write-ahead log to the database it was written for; a stale `-wal` from the old
   main file next to a new one is the classic "database disk image is malformed" on first open.
4. The caller `SettingActivity.kt:168` shows a restart dialog only on `true`. On `false` it says
   **nothing at all** — the user taps Restore and the UI returns to normal, which is the same silence
   this branch was removing elsewhere.

The modern (manifest) path is *not* affected by 1–3: `restoreZipEntries` never writes
`databasesDir` (that directory and the `close()` appear once each, at lines 622 and 611) and restores
`DATABASE_DATA` through `restoreDatabaseData(JSONObject)` — DAO writes, not a file swap.

## What the patch does

- Every entry is written to `"<name>.restoring"` beside its target; nothing live is touched until the
  whole archive has been read successfully.
- `bookmarkManager.database.close()` moves to the point immediately before the swap, so a failure to
  open or read the stream leaves the app and its database intact.
- Each staging file is `renameTo`-ed over its target (same directory, so atomic on ext4/f2fs), and a
  `.db` swap deletes that database's stale `-wal`/`-shm`.
- `IOException` during the swap deletes the remaining staging files. Residual risk is stated rather
  than hidden: if a *rename itself* fails partway, entries already moved stay moved — closing that
  would need a reverse swap, which is why the failure must also be surfaced (below).

## Verification performed here, and its limits

- `git apply --check` clean against `9509aa3`; regenerated from a real edit, so the context lines are
  the file's actual text rather than hand-written hunks.
- Delimiter profile of the whole file identical before/after (net `{}` 0, min depth 0, net `()` 0,
  net `[]` 1 → unchanged), max added line 105 chars (ktlint limit 140), statements added inside one
  existing function, no imports needed (`File`, `IOException`, `mutableListOf` all already in scope).
- **Not compiled.** CI cannot verify a file inside `docs/`: `:app:compileDebugKotlin` only sees it once
  the patch is applied. Whoever applies it gets the compile and the 371-test suite for free, and gets
  *nothing* for this path, because nothing tests it.

## What a real fix should also include

1. A unit/Robolectric test that feeds a **truncated** zip (valid first entry, garbage second) and
   asserts both `false` **and** that the pre-existing `.db` file still opens afterwards. That is the
   behaviour this patch adds; today it would be untested.
2. A user-visible failure in `SettingActivity` on `false`. Deliberately left out of the patch: a new
   string is the cheapest way to add a `MissingTranslation` finding (see §11 — that is exactly how the
   9 never-localised keys were made), so it should be a translated key from the start, which is a
   translation decision, not a mechanical one. A hardcoded toast would match `BackupUnit:1266`'s
   existing style but deepen that particular debt.
3. Whether `restoreUserscripts`'s count-based toast (`"$count userscript(s) imported"`) should say
   "3 of 5" when the loop skipped some — the same silence, one function over, in the modern path.
