package io.nekohasekai.sagernet.ui.tv

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ObjectAdapter
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.RemoteFocusHighlighter
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class RemoteUiTest {
    private var controller: ActivityController<Activity>? = null
    private fun activity(): Activity {
        val result = Robolectric.buildActivity(Activity::class.java)
        result.get().setTheme(R.style.Theme_SagerNet)
        controller = result
        return result.setup().get()
    }
    @After fun closeActivity() { controller?.pause()?.stop()?.destroy() }
    private fun layout(parent: View) {
        parent.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1400, View.MeasureSpec.AT_MOST))
        parent.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
    }
    @Test fun profileBindShowsSelectedAndActiveAsIndependentLabels() {
        val context = activity(); val parent = FrameLayout(context); context.setContentView(parent)
        val presenter = ProfileCardPresenter(); val holder = presenter.onCreateViewHolder(parent) as ProfileCardPresenter.ProfileCardViewHolder
        parent.addView(holder.view)
        presenter.onBindViewHolder(holder, TvProfileCard(1, "One", "VLESS", "example.com", true, null))
        assertTrue(holder.view.isActivated); assertEquals(context.getString(R.string.tv_selected), holder.statusText.text.toString()); assertEquals(View.GONE, holder.statusDot.visibility)
        presenter.onBindViewHolder(holder, TvProfileCard(1, "One", "VLESS", "example.com", false, TvVpnPhase.CONNECTED))
        assertFalse(holder.view.isActivated); assertEquals(View.VISIBLE, holder.statusDot.visibility); assertEquals(context.getString(R.string.tv_active), holder.statusText.text.toString())
        assertTrue(holder.view.contentDescription.toString().contains(context.getString(R.string.tv_active)))
    }
    @Test fun recycledHintClearsEveryPreviousProfileState() {
        val parent = FrameLayout(activity()); val presenter = ProfileCardPresenter(); val holder = presenter.onCreateViewHolder(parent) as ProfileCardPresenter.ProfileCardViewHolder
        presenter.onBindViewHolder(holder, TvProfileCard(1, "One", "VLESS", "example.com", true, TvVpnPhase.CONNECTED))
        presenter.onBindViewHolder(holder, TvEmptyHint("Empty"))
        assertFalse(holder.view.isActivated); assertEquals(View.GONE, holder.statusDot.visibility); assertEquals(0L, holder.profileId)
        assertEquals("", holder.address.text.toString()); assertEquals("", holder.statusText.text.toString())
    }
    @Test fun longPressUsesCurrentBoundProfileIdentity() {
        var selected = 0L; val parent = FrameLayout(activity()); val presenter = ProfileCardPresenter { selected = it }
        val holder = presenter.onCreateViewHolder(parent)
        presenter.onBindViewHolder(holder, TvProfileCard(7, "One", "VLESS", "example.com", false, null)); assertTrue(holder.view.performLongClick()); assertEquals(7L, selected)
        presenter.onBindViewHolder(holder, TvProfileCard(8, "Two", "VLESS", "example.com", false, null)); holder.view.performLongClick(); assertEquals(8L, selected)
    }
    @Test fun rebindDoesNotReplaceTheFocusedProfileView() {
        val context = activity(); val parent = FrameLayout(context); context.setContentView(parent)
        val presenter = ProfileCardPresenter(); val holder = presenter.onCreateViewHolder(parent); parent.addView(holder.view)
        presenter.onBindViewHolder(holder, TvProfileCard(1, "One", "VLESS", "example.com", true, null)); layout(parent)
        holder.view.isFocusableInTouchMode = true; assertTrue(holder.view.requestFocus())
        presenter.onBindViewHolder(holder, TvProfileCard(1, "One", "VLESS", "example.com", true, TvVpnPhase.CONNECTED))
        assertTrue(holder.view.isFocused)
    }
    @Test fun actionUpdateDoesNotRemoveOrInsertFocusedIdentity() {
        val adapter = ArrayObjectAdapter(ActionPresenter()); var removed = 0; var inserted = 0
        adapter.setItems(listOf(TvAction(1, "Start", "Idle", R.drawable.ic_service_idle)), TvRowDiff)
        adapter.registerObserver(object : ObjectAdapter.DataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) { removed += itemCount }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { inserted += itemCount }
        })
        adapter.setItems(listOf(TvAction(1, "Stop", "Connected", R.drawable.ic_service_active)), TvRowDiff)
        assertEquals(0, removed); assertEquals(0, inserted); assertEquals(1, adapter.size())
    }
    @Test fun phoneProfileHasAnExplicitHorizontalFocusChain() {
        val context = activity(); val parent = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }; context.setContentView(parent)
        val first = LayoutInflater.from(context).inflate(R.layout.layout_profile, parent, false)
        val second = LayoutInflater.from(context).inflate(R.layout.layout_profile, parent, false)
        parent.addView(first); parent.addView(second); layout(parent)
        assertEquals(R.id.edit, second.nextFocusRightId)
        assertSame(second.findViewById<View>(R.id.edit), second.focusSearch(View.FOCUS_RIGHT))
        assertSame(second, second.findViewById<View>(R.id.edit).focusSearch(View.FOCUS_LEFT))
    }
    @Test fun remoteHighlightDoesNotReplaceTouchBackground() {
        val context = activity(); val parent = FrameLayout(context); val button = android.widget.Button(context).apply { text = "Control"; isFocusableInTouchMode = true }
        parent.addView(button); context.setContentView(parent); layout(parent)
        val original = button.background; val highlighter = RemoteFocusHighlighter(parent)
        highlighter.setEnabled(true); button.requestFocus(); assertTrue(highlighter.enabled); assertSame(original, button.background)
        highlighter.setEnabled(false); assertFalse(highlighter.enabled); assertSame(original, button.background); highlighter.close()
    }
    @Test @Config(qualifiers = "ru") fun russianStateLabelsAreActuallyLocalized() {
        val context = activity(); assertEquals("Подключено", context.getString(R.string.tv_connected)); assertEquals("Выбран", context.getString(R.string.tv_selected))
    }
    @Test fun actionCardUsesReadableSecondaryTextAndWrapHeight() {
        val parent = FrameLayout(activity()); val presenter = ActionPresenter(); val holder = presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        presenter.onBindViewHolder(holder, TvAction(1, "A long translated connection action", "Connected\nSelected: A long profile name\nActive: Other profile", R.drawable.ic_service_active))
        val scale = holder.view.resources.displayMetrics.scaledDensity
        assertTrue(holder.title.textSize / scale >= 20); assertTrue(holder.subtitle.textSize / scale >= 16)
        assertEquals(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, holder.view.layoutParams.height)
        assertEquals(3, holder.subtitle.maxLines)
    }
}
