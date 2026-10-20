package io.nekohasekai.sagernet.widget

import android.content.Context
import android.util.AttributeSet
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ktx.Logs
import moe.matsuri.nb4a.ui.SimpleMenuPreference

/**
 * Same behavior as [OutboundPreference] but for the global "DNS over proxy" option,
 * which is stored in the configuration store instead of the profile cache.
 */
class DnsProxyPreference
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.dropdownPreferenceStyle
) : SimpleMenuPreference(context, attrs, defStyle, 0) {

    init {
        setEntries(R.array.dns_proxy_entry)
        setEntryValues(R.array.dns_proxy_value)
    }

    override fun getSummary(): CharSequence? {
        if (value == "3") {
            val proxyId = DataStore.remoteDnsProxy
            if (proxyId > 0L) {
                val name = try {
                    ProfileManager.getProfile(proxyId)?.displayName()
                } catch (e: Exception) {
                    Logs.w(e)
                    null
                }
                if (!name.isNullOrBlank()) return name
            }
            return context.getString(androidx.preference.R.string.not_set)
        }
        return super.getSummary()
    }

}
