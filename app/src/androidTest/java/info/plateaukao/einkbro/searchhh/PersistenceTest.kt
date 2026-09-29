package info.plateaukao.einkbro.searchhh

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.local.LocalIdentity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    @Test fun sameNameInIsolatedStoresHasIndependentIdentity() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        fun store(prefix: String) = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) = base.getSharedPreferences(prefix + name, mode)
        }
        try {
            val a = LocalIdentity.create(store("isolation-a-"), "Same name")
            val b = LocalIdentity.create(store("isolation-b-"), "Same name")
            assertNotEquals(a.id, b.id); assertNotEquals(a.token, b.token)
            assertEquals(a, LocalIdentity.load(store("isolation-a-")))
        } finally {
            base.deleteSharedPreferences("isolation-a-searchhh_connection")
            base.deleteSharedPreferences("isolation-b-searchhh_connection")
        }
    }
    @Test fun migrationPreservesSavedRowsAndAddsSessions() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "searchhh-migration-test.db"
        context.deleteDatabase(name)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("CREATE TABLE saved_opportunities (id TEXT NOT NULL PRIMARY KEY, payload TEXT NOT NULL, savedAt INTEGER NOT NULL)")
            old.execSQL("INSERT INTO saved_opportunities VALUES ('kept', '{}', 42)")
            old.version = 1
        }
        val db = Room.databaseBuilder(context, SearchhhDatabase::class.java, name).addMigrations(SearchhhDatabase.MIGRATION_1_2).build()
        try {
            assertEquals("kept", db.saved().all().first().single().id)
            db.sessions().put(LocalSession("session", "{}"))
            assertEquals("{}", db.sessions().get("session")!!.payload)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
