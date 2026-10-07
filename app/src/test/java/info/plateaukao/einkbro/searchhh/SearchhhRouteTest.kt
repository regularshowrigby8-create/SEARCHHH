package info.plateaukao.einkbro.searchhh

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchhhRouteTest {
    @Test
    fun unknownAndMissingRoutesReturnHome() {
        assertEquals(SearchhhRoute.DISCOVER, SearchhhRoute.fromId("unregistered"))
        assertEquals(SearchhhRoute.DISCOVER, SearchhhRoute.fromId(null))
    }

    @Test
    fun everyRegisteredIdRoundTripsAndHasUniqueSemantics() {
        val routes = SearchhhRoute.entries
        routes.forEach { assertEquals(it, SearchhhRoute.fromId(it.id)) }
        assertEquals(routes.size, routes.map { it.id }.toSet().size)
        assertEquals(routes.size, routes.map { it.screenTag }.toSet().size)
        assertEquals(routes.size, routes.map { it.menuTag }.toSet().size)
    }
}
