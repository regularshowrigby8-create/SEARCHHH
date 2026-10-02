package info.plateaukao.einkbro.unit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import info.plateaukao.einkbro.preference.ConfigManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.OutputStream

/**
 * Routes file writes to the Supernote `Document/` folder via a persisted SAF tree URI.
 * Android 11+ scoped storage blocks both DownloadManager (path allowlist) and direct
 * File I/O (EPERM) on `/storage/emulated/0/Document/`, so we must go through SAF.
 */
object SupernoteStorage : KoinComponent {
    private val config: ConfigManager by inject()

    @Volatile
    private var pickerLauncher: ActivityResultLauncher<Uri?>? = null

    @Volatile
    private var pendingCallback: ((Uri?) -> Unit)? = null

    fun registerPicker(launcher: ActivityResultLauncher<Uri?>) {
        pickerLauncher = launcher
    }

    fun unregisterPicker(launcher: ActivityResultLauncher<Uri?>) {
        if (pickerLauncher === launcher) {
            pickerLauncher = null
            pendingCallback = null
        }
    }

    fun storedTreeUri(): Uri? = config.browser.supernoteFolderUri?.toUri()

    /** Persist a freshly-granted tree URI and run the queued callback (if any). */
    fun onPickerResult(
        context: Context,
        uri: Uri?,
    ) {
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (e: SecurityException) {
                Log.w("SupernoteStorage", "takePersistableUriPermission denied: $e")
            } catch (e: IllegalArgumentException) {
                Log.w("SupernoteStorage", "takePersistableUriPermission rejected: $e")
            }
            config.browser.supernoteFolderUri = uri.toString()
        }
        val cb = pendingCallback
        pendingCallback = null
        cb?.invoke(uri)
    }

    /**
     * Resolve the persisted tree URI, or prompt the user to grant access.
     * Callback is invoked with the URI on success or null if the user cancels
     * / no launcher is currently registered.
     */
    fun ensureTreeUri(onResult: (Uri?) -> Unit) {
        storedTreeUri()?.let {
            onResult(it)
            return
        }
        val launcher = pickerLauncher
        if (launcher == null) {
            onResult(null)
            return
        }
        pendingCallback = onResult
        try {
            launcher.launch(HelperUnit.supernoteStorageRootInitialUri())
        } catch (e: IllegalStateException) {
            pendingCallback = null
            Log.w("SupernoteStorage", "launch picker is unavailable: $e")
            onResult(null)
        } catch (e: SecurityException) {
            pendingCallback = null
            Log.w("SupernoteStorage", "launch picker denied: $e")
            onResult(null)
        }
    }

    /**
     * Write under the granted tree, replacing an existing same-name document.
     * Owns the stream through write and close; returns its URI only after both succeed.
     * A missing tree/file/stream returns null. Provider, write and close failures propagate.
     */
    fun write(
        context: Context,
        treeUri: Uri,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): Uri? {
        val tree = DocumentFile.fromTreeUri(context, treeUri)
        val file =
            tree?.let {
                it.findFile(fileName)?.delete()
                it.createFile(mimeType.ifBlank { "application/octet-stream" }, fileName)
            }
        return file?.let { document ->
            context.contentResolver.openOutputStream(document.uri)?.use { stream ->
                writeContent(stream)
                document.uri
            }
        }
    }
}
