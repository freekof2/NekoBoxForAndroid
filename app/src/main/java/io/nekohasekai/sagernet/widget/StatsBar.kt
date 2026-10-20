package io.nekohasekai.sagernet.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.google.android.material.bottomappbar.BottomAppBar
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class StatsBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.bottomAppBarStyle,
) : BottomAppBar(context, attrs, defStyleAttr) {
    private lateinit var statusText: TextView
    private lateinit var txText: TextView
    private lateinit var rxText: TextView
    private lateinit var latencyText: TextView
    private lateinit var webRtcStunStateText: TextView
    private lateinit var behavior: YourBehavior

    var allowShow = true

    override fun getBehavior(): YourBehavior {
        if (!this::behavior.isInitialized) behavior = YourBehavior { allowShow }
        return behavior
    }

    class YourBehavior(val getAllowShow: () -> Boolean) : Behavior() {

        override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomAppBar, target: View,
            dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
            type: Int, consumed: IntArray,
        ) {
            super.onNestedScroll(
                coordinatorLayout,
                child,
                target,
                dxConsumed,
                dyConsumed + dyUnconsumed,
                dxUnconsumed,
                0,
                type,
                consumed
            )
        }

        override fun slideUp(child: BottomAppBar) {
            if (!getAllowShow()) return
            super.slideUp(child)
        }

        override fun slideDown(child: BottomAppBar) {
            if (!getAllowShow()) return
            super.slideDown(child)
        }
    }


    override fun setOnClickListener(l: OnClickListener?) {
        statusText = findViewById(R.id.status)
        txText = findViewById(R.id.tx)
        rxText = findViewById(R.id.rx)
        latencyText = findViewById(R.id.latency)
        webRtcStunStateText = findViewById(R.id.webrtc_stun_state)
        updateWebRtcStunState()
        super.setOnClickListener(l)
    }

    private fun setStatus(text: CharSequence) {
        statusText.text = text
        TooltipCompat.setTooltipText(this, text)
    }

    private fun updateWebRtcStunState() {
        if (this::webRtcStunStateText.isInitialized) {
            webRtcStunStateText.text = if (DataStore.enableWebRtcStunProxy) "WebRtc STUN" else ""
        }
    }

    fun changeState(state: BaseService.State) {
        val activity = context as MainActivity
        fun postWhenStarted(what: () -> Unit) = activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(100L)
            activity.whenStarted { what() }
        }
        if ((state == BaseService.State.Connected).also { hideOnScroll = it }) {
            postWhenStarted {
                if (allowShow) performShow()
                updateWebRtcStunState()
                setStatus(app.getText(R.string.vpn_connected))
            }
        } else {
            postWhenStarted {
                performHide()
            }
            updateSpeed(0, 0)
            setStatus(
                context.getText(
                    when (state) {
                        BaseService.State.Connecting -> R.string.connecting
                        BaseService.State.Stopping -> R.string.stopping
                        else -> R.string.not_connected
                    }
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateSpeed(txRate: Long, rxRate: Long) {
        txText.text = "▲  ${
            context.getString(
                R.string.speed, Formatter.formatFileSize(context, txRate)
            )
        }"
        rxText.text = "▼  ${
            context.getString(
                R.string.speed, Formatter.formatFileSize(context, rxRate)
            )
        }"
        updateWebRtcStunState()
    }

    fun testConnection() {
        val activity = context as MainActivity
        isEnabled = false
        setStatus(app.getText(R.string.connection_test_testing))
        runOnDefaultDispatcher {
            supervisorScope {
                val latencyJob = launch {
                    try {
                        val delayMillis = activity.urlTest()
                        onMainDispatcher {
                            latencyText.text = ConnectionTestStatusFormatter.formatLatency(delayMillis)
                        }
                    } catch (e: Exception) {
                        Logs.w(e.toString())
                        onMainDispatcher {
                            activity.snackbar(
                                app.getString(
                                    R.string.connection_test_error, e.readableMessage
                                )
                            ).show()
                        }
                    }
                }
                val ipJob = launch {
                    try {
                        val ip = activity.urlTestIp()
                        onMainDispatcher {
                            setStatus(ip)
                        }
                    } catch (e: Exception) {
                        Logs.w(e.toString())
                        onMainDispatcher {
                            activity.snackbar(
                                app.getString(
                                    R.string.connection_test_error, e.readableMessage
                                )
                            ).show()
                        }
                    }
                }
                latencyJob.join()
                ipJob.join()
                onMainDispatcher {
                    isEnabled = true
                    updateWebRtcStunState()
                }
            }
        }
    }

}
