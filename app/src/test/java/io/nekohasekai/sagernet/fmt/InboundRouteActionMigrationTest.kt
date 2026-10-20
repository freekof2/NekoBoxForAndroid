package io.nekohasekai.sagernet.fmt

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InboundRouteActionMigrationTest {
    @Test
    fun migratesDomainStrategyAndSniffForGeneratedInbound() {
        val actions = buildInboundRouteActions(
            inboundTag = "mixed-in",
            domainStrategy = "prefer_ipv4",
            enableSniff = true,
        )

        assertEquals(2, actions.size)
        assertEquals(listOf("mixed-in"), actions[0].inbound)
        assertEquals("resolve", actions[0].action)
        assertEquals("prefer_ipv4", actions[0].strategy)
        assertNull(actions[0].timeout)

        assertEquals(listOf("mixed-in"), actions[1].inbound)
        assertEquals("sniff", actions[1].action)
        assertNull(actions[1].strategy)
    }

    @Test
    fun omitsActionsWhenCorrespondingLegacyOptionsWereDisabled() {
        assertTrue(
            buildInboundRouteActions(
                inboundTag = "tun-in",
                domainStrategy = "",
                enableSniff = false,
            ).isEmpty()
        )
    }

    @Test
    fun migratesRawConfigAndPrependsNonFinalActions() {
        val migrated = migrateLegacyInboundFields(
            """
            {
              "inbounds": [
                {
                  "type": "mixed",
                  "sniff": true,
                  "sniff_timeout": "1s",
                  "sniff_override_destination": true,
                  "domain_strategy": "prefer_ipv4"
                }
              ],
              "route": {
                "rules": [
                  { "action": "reject" }
                ]
              }
            }
            """.trimIndent()
        )

        val root = JsonParser.parseString(migrated).asJsonObject
        val inbound = root.getAsJsonArray("inbounds")[0].asJsonObject
        assertEquals("legacy-inbound-0", inbound["tag"].asString)
        assertFalse(inbound.has("sniff"))
        assertFalse(inbound.has("sniff_timeout"))
        assertFalse(inbound.has("sniff_override_destination"))
        assertFalse(inbound.has("domain_strategy"))

        val rules = root.getAsJsonObject("route").getAsJsonArray("rules")
        assertEquals(3, rules.size())
        assertEquals("resolve", rules[0].asJsonObject["action"].asString)
        assertEquals("legacy-inbound-0", rules[0].asJsonObject["inbound"].asString)
        assertEquals("prefer_ipv4", rules[0].asJsonObject["strategy"].asString)
        assertEquals("sniff", rules[1].asJsonObject["action"].asString)
        assertEquals("1s", rules[1].asJsonObject["timeout"].asString)
        assertEquals("reject", rules[2].asJsonObject["action"].asString)
    }

    @Test
    fun migratesLegacyTunAddressFieldsIntoUnifiedLists() {
        val migrated = migrateLegacyInboundFields(
            """
            {
              "inbounds": [
                {
                  "type": "tun",
                  "address": ["10.0.0.1/30"],
                  "inet4_address": "172.19.0.1/30",
                  "inet6_address": ["fdfe:dcba:9876::1/126"],
                  "inet4_route_address": ["0.0.0.0/1", "128.0.0.0/1"],
                  "inet6_route_address": "::/1",
                  "inet4_route_exclude_address": "192.168.0.0/16",
                  "inet6_route_exclude_address": ["fc00::/7"]
                }
              ]
            }
            """.trimIndent()
        )

        val inbound = JsonParser.parseString(migrated)
            .asJsonObject
            .getAsJsonArray("inbounds")[0]
            .asJsonObject
        assertEquals(
            listOf("10.0.0.1/30", "172.19.0.1/30", "fdfe:dcba:9876::1/126"),
            inbound.getAsJsonArray("address").map { it.asString },
        )
        assertEquals(
            listOf("0.0.0.0/1", "128.0.0.0/1", "::/1"),
            inbound.getAsJsonArray("route_address").map { it.asString },
        )
        assertEquals(
            listOf("192.168.0.0/16", "fc00::/7"),
            inbound.getAsJsonArray("route_exclude_address").map { it.asString },
        )
        listOf(
            "inet4_address",
            "inet6_address",
            "inet4_route_address",
            "inet6_route_address",
            "inet4_route_exclude_address",
            "inet6_route_exclude_address",
        ).forEach { legacyField ->
            assertFalse("legacy field remains: $legacyField", inbound.has(legacyField))
        }
    }
}
