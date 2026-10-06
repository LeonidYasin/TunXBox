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
    @Test fun tvCatalogMatchesAllUpstreamMenuEntriesExceptRedundantTvSwitch() {
        val context = activity()
        val menu = android.widget.PopupMenu(context, View(context)).menu
        android.view.MenuInflater(context).inflate(R.menu.main_drawer_menu, menu)
        val expected = (0 until menu.size()).map { menu.getItem(it).itemId }.filter { it != R.id.nav_switch_tv_mode }
        assertEquals(expected, TvFunctionCatalog.entries(context, true, false).map { it.id })
        assertFalse(TvFunctionCatalog.entries(context, false, true).any { it.id == R.id.nav_traffic || it.id == R.id.nav_tuiguang })
    }
    @Test fun rowMenuAndInfoWorkFromNestedControlWithoutRepeats() {
        val context = activity(); val row = LinearLayout(context); val child = android.widget.Button(context)
        row.addView(child); context.setContentView(row)
        var opened = 0
        io.nekohasekai.sagernet.ui.RemoteRowActions.bind(row, listOf(child)) { opened++ }
        assertTrue(io.nekohasekai.sagernet.ui.RemoteRowActions.handle(child, android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MENU)))
        assertTrue(io.nekohasekai.sagernet.ui.RemoteRowActions.handle(child, android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MENU)))
        assertEquals(1, opened)
        assertTrue(io.nekohasekai.sagernet.ui.RemoteRowActions.handle(child, android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_INFO)))
        assertEquals(2, opened)
        assertTrue(child.isFocusable); assertTrue(child.minimumHeight >= 48 * context.resources.displayMetrics.density)
    }
    @Test fun longClickPreferenceHasHardwareMenuAlternative() {
        val context = activity()
        val preference = moe.matsuri.nb4a.ui.LongClickListPreference(context)
        var called = 0
        preference.setOnLongClickListener { called++; true }
        val row = LayoutInflater.from(context).inflate(androidx.preference.R.layout.preference, null)
        preference.onBindViewHolder(androidx.preference.PreferenceViewHolder.createInstanceForTests(row))
        assertTrue(io.nekohasekai.sagernet.ui.RemoteRowActions.handle(row, android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MENU)))
        assertEquals(1, called)
    }
    @Test fun routeAndChainHaveVisibleFocusableActions() {
        val context=activity()
        val asset=LayoutInflater.from(context).inflate(R.layout.layout_asset_item, null)
        val actions=asset.findViewById<View>(R.id.remote_asset_actions)
        assertEquals(View.VISIBLE, actions.visibility);assertTrue(actions.isFocusable)
    }

    @Test fun configurationEditorControlsFitPhoneWidthAndAreFocusable() {
        val context=activity()
        val view=LayoutInflater.from(context).inflate(R.layout.layout_edit_config,null)
        context.setContentView(view)
        val width=(390 * context.resources.displayMetrics.density).toInt()
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(844,View.MeasureSpec.EXACTLY))
        view.layout(0,0,width,844)
        for(id in listOf(R.id.action_tab,R.id.action_undo,R.id.action_redo,R.id.action_format)) {
            val control=view.findViewById<View>(id)
            assertTrue(control.isFocusable);assertNotNull(control.contentDescription)
            assertTrue(control.right <= control.parent.let { it as View }.width)
        }
        val key=LayoutInflater.from(context).inflate(R.layout.item_keyboard_key,null)
        assertTrue(key.isFocusable)
    }

}
