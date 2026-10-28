package io.nekohasekai.sagernet.ui.tv

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ObjectAdapter
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.LanProxyAddress
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
    @Test fun compactHealthCardHasHumanErrorAndSameHeightAsOtherActions() {
        val parent=FrameLayout(activity());val presenter=ActionPresenter()
        val holder=presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        val reason=TvHealthReason.text(parent.context,io.nekohasekai.sagernet.group.SubscriptionFailureCategory.REALITY)
        presenter.onBindViewHolder(holder,TvAction(8,reason,"Current profile\n↑ 0 ↓ 0\nNET_REALITY",R.drawable.ic_remote_groups))
        assertEquals(3,holder.subtitle.minLines);assertEquals(3,holder.subtitle.maxLines)
        assertTrue(holder.view.isFocusable);assertTrue(holder.title.text.contains("REALITY"))
    }
    @Test fun groupTileShowsCollapseStateAndOpensActions() {
        val parent=FrameLayout(activity());val presenter=GroupCardPresenter()
        val holder=presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        presenter.onBindViewHolder(holder,TvGroupCard(7,"Collection",3,true))
        assertTrue(holder.view.isActivated);assertEquals("Collection",holder.title.text.toString())
        assertTrue(holder.subtitle.text.contains(parent.context.getString(R.string.tv_group_row_selected)))
        assertTrue(holder.subtitle.text.contains(parent.context.getString(R.string.tv_group_row_count,3)))
        assertTrue(holder.view.contentDescription.contains(parent.context.getString(R.string.tv_group_row_selected)))
        presenter.onBindViewHolder(holder,TvGroupCard(7,"Collection",3,true,collapsed=true))
        assertTrue(holder.subtitle.text.contains(parent.context.getString(R.string.tv_group_row_count_collapsed,3)))
        presenter.onBindViewHolder(holder,TvGroupCard(8,"Other",0,false))
        assertFalse(holder.view.isActivated);assertTrue(holder.view.isFocusable)
        assertTrue(holder.subtitle.text.contains(parent.context.getString(R.string.tv_group_row_choose)))
        presenter.onUnbindViewHolder(holder);assertFalse(holder.view.isActivated)
    }
    @Test fun lanProxyCardListsEveryFullAddressWithoutEllipsis() {
        val parent=FrameLayout(activity());val presenter=LanProxyCardPresenter()
        val holder=presenter.onCreateViewHolder(parent) as LanProxyCardPresenter.Holder
        presenter.onBindViewHolder(holder,TvLanProxyCard(19,listOf(
            LanProxyAddress.Entry("192.168.43.1","wlan0"),
            LanProxyAddress.Entry("10.76.41.155","rmnet_data0"),
        ),2080))
        assertEquals(parent.context.getString(R.string.tv_lan_proxy_label),holder.label.text.toString())
        assertEquals(listOf("192.168.43.1:2080","10.76.41.155:2080"),holder.addressLines.map { it.text.toString() })
        holder.addressLines.forEach { assertNull(it.ellipsize);assertEquals(1,it.maxLines);assertEquals(View.VISIBLE,it.visibility) }
        assertTrue(holder.view.isFocusable)
        assertTrue(holder.view.contentDescription.contains("10.76.41.155:2080"))
        // A shorter list hides the stale extra line instead of leaving it on screen; an invalid
        // port keeps the host readable rather than rendering a broken `host:0`.
        presenter.onBindViewHolder(holder,TvLanProxyCard(19,listOf(
            LanProxyAddress.Entry("192.168.43.1","wlan0"),
        ),0))
        assertEquals(2,holder.addressLines.size)
        assertEquals("192.168.43.1",holder.addressLines[0].text.toString())
        assertEquals(View.GONE,holder.addressLines[1].visibility)
    }
    @Test fun longLanAddressShrinksInsteadOfBeingCutAtTheCardEdge() {
        val parent=FrameLayout(activity());val presenter=LanProxyCardPresenter()
        val holder=presenter.onCreateViewHolder(parent) as LanProxyCardPresenter.Holder
        presenter.onBindViewHolder(holder,TvLanProxyCard(19,listOf(
            LanProxyAddress.Entry("192.168.100.100","wlan0"),
        ),10808))
        val line=holder.addressLines.single()
        assertEquals(View.VISIBLE,line.visibility)
        assertEquals("192.168.100.100:10808",line.text.toString())
        assertEquals(1,line.maxLines)
        assertNull(line.ellipsize)
        // Autosize must actually be active: with setSingleLine()/horizontally scrolling the text
        // is measured unbounded and gets clipped ("192.168.100.100…") instead of shrinking.
        assertEquals(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM,TextViewCompat.getAutoSizeTextType(line))
    }
    @Test fun longActionLabelsShrinkInsteadOfBeingCutInsideTheCard() {
        val parent=FrameLayout(activity());val presenter=ActionPresenter()
        val holder=presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        assertEquals(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM,TextViewCompat.getAutoSizeTextType(holder.title))
        assertEquals(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM,TextViewCompat.getAutoSizeTextType(holder.subtitle))
        presenter.onBindViewHolder(holder,TvAction(1,"Автопереключение","Включено · профилей: 3 · OK — изменить или выключить",R.drawable.ic_remote_groups))
        assertEquals(2,holder.title.maxLines);assertEquals(3,holder.subtitle.maxLines)
    }
    @Test fun groupProfileAndEmptyHintHaveSeparateFocusNamespaces() {
        val group=TvGroupCard(1,"Group",1,false)
        val profile=TvProfileCard(1,"Profile","SOCKS","fixture",false,null,1)
        assertNotEquals(TvGroupRows.focus(group),TvGroupRows.focus(profile))
        assertNotEquals(TvGroupRows.focus(group),TvGroupRows.focus(TvEmptyHint("Empty",1)))
        assertNotEquals(TvGroupRows.row(1),TvGroupRows.row(2))
        assertFalse(TvRowDiff.areItemsTheSame(group,profile))
        assertTrue(TvRowDiff.areItemsTheSame(group,group.copy(selected=true)))
        assertFalse(TvRowDiff.areContentsTheSame(group,group.copy(selected=true)))
    }
    @Test fun eachCatalogEntryCanBindAsItsOwnFocusableButton() {
        val parent=FrameLayout(activity());val presenter=FunctionCardPresenter()
        val entries=TvFunctionCatalog.entries(parent.context,true,false)
        assertTrue(entries.any {it.id==R.id.nav_app_updates})
        assertEquals(entries.size,entries.map {it.id}.toSet().size)
        for(entry in entries) {
            val holder=presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
            presenter.onBindViewHolder(holder,TvFunctionCard(entry.id,entry.title))
            assertTrue(holder.view.isFocusable);assertEquals(entry.title,holder.title.text.toString())
        }
    }
    @Test fun connectionHasSeparateProfileCountersAndShortResultViews() {
        val parent=FrameLayout(activity());val presenter=ConnectionCardPresenter()
        val h=presenter.onCreateViewHolder(parent) as ConnectionCardPresenter.Holder
        presenter.onBindViewHolder(h,TvConnectionCard(8,"Netherlands Hysteria", "↑ 12 KiB","↓ 3 MiB","OK · 113 ms"))
        assertEquals("Netherlands Hysteria",h.name.text.toString())
        assertEquals("↑ 12 KiB",h.sent.text.toString());assertEquals("↓ 3 MiB",h.received.text.toString())
        assertEquals("OK · 113 ms",h.result.text.toString())
        assertEquals(1,h.sent.maxLines);assertEquals(1,h.received.maxLines);assertEquals(1,h.result.maxLines)
        assertTrue(h.view.isFocusable);assertEquals(8L,TvGroupRows.focus(TvConnectionCard(8,"","","","")))
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
    @Test fun actionCardUsesReadableSecondaryTextAndStableTileHeight() {
        val parent = FrameLayout(activity()); val presenter = ActionPresenter(); val holder = presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        presenter.onBindViewHolder(holder, TvAction(1, "A long translated connection action", "Connected\nSelected: A long profile name\nActive: Other profile", R.drawable.ic_service_active))
        val scale = holder.view.resources.displayMetrics.scaledDensity
        assertTrue(holder.title.textSize / scale >= 20); assertTrue(holder.subtitle.textSize / scale >= 16)
        // Every tile family asks for the same explicit size: a wrap-content height let the LAN
        // tile with its address list grow taller than the cards next to it.
        assertEquals(TvCardMetrics.heightPx(parent.context), holder.view.layoutParams.height)
        assertEquals(TvCardMetrics.widthPx(parent.context), holder.view.layoutParams.width)
        assertEquals(3, holder.subtitle.maxLines)
    }
    @Test @Config(qualifiers = "w960dp-h540dp-xhdpi")
    fun everyConnectionTileHasTheSameWidthAndHeight() {
        val parent = FrameLayout(activity()); val context = parent.context
        val presenters = listOf(ConnectionCardPresenter(), LanProxyCardPresenter(), QualityCardPresenter(), ActionPresenter())
        val widths = presenters.map { it.onCreateViewHolder(parent).view.layoutParams.width }
        val heights = presenters.map { it.onCreateViewHolder(parent).view.layoutParams.height }
        assertEquals("the LAN tile used dp as px and rendered at half width", 1, widths.toSet().size)
        assertEquals(1, heights.toSet().size)
        assertEquals(TvCardMetrics.widthPx(context), widths.first())
        assertEquals(TvCardMetrics.heightPx(context), heights.first())
        // 300dp at xhdpi: the address tile must not be 300 raw pixels wide.
        assertEquals(600, widths.first())
    }
    @Test @Config(qualifiers = "w960dp-h540dp-xhdpi")
    fun lanTileKeepsEveryAddressInsideTheSharedTileSize() {
        val parent = FrameLayout(activity()); val context = parent.context
        val presenter = LanProxyCardPresenter()
        val holder = presenter.onCreateViewHolder(parent) as LanProxyCardPresenter.Holder
        parent.addView(holder.view)
        presenter.onBindViewHolder(holder, TvLanProxyCard(19, listOf(
            LanProxyAddress.Entry("192.168.43.1", "wlan0"),
            LanProxyAddress.Entry("10.76.41.155", "rmnet_data0"),
            LanProxyAddress.Entry("172.19.0.1", "tun0"),
            LanProxyAddress.Entry("192.168.100.100", "eth0"),
        ), 2080))
        val width = TvCardMetrics.widthPx(context); val height = TvCardMetrics.heightPx(context)
        parent.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        parent.layout(0, 0, width, height)
        assertEquals(width, holder.view.measuredWidth); assertEquals(height, holder.view.measuredHeight)
        val visible = holder.addressLines.filter { it.visibility == View.VISIBLE }
        assertEquals(4, visible.size)
        val minLine = TvCardMetrics.lineHeightPx(context, 11f)
        for (line in visible) {
            assertEquals(1, line.maxLines); assertNull(line.ellipsize)
            assertTrue("an address must not be cut at the card edge", line.width >= line.paint.measureText(line.text.toString()).toInt())
            assertTrue("autosize must leave a readable line", line.measuredHeight >= minLine)
        }
        // Label + address list + hint have to fit the shared tile height, otherwise LinearLayout
        // clips the last line instead of the tile growing.
        val container = holder.view as ViewGroup
        val content = (0 until container.childCount).sumOf { index ->
            val child = container.getChildAt(index)
            child.measuredHeight + (child.layoutParams as LinearLayout.LayoutParams).topMargin
        }
        assertTrue("content $content must fit the $height px tile", content <= height)
    }
    @Test fun tvCatalogMatchesAllUpstreamMenuEntriesExceptRedundantTvSwitch() {
        val context = activity()
        val menu = android.widget.PopupMenu(context, View(context)).menu
        android.view.MenuInflater(context).inflate(R.menu.main_drawer_menu, menu)
        val expected = (0 until menu.size()).map { menu.getItem(it).itemId }.filter { it !in setOf(R.id.nav_switch_tv_mode, R.id.nav_restart_app, R.id.nav_close_app) }
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


    @Test @Config(qualifiers = "w390dp-h844dp-port")
    fun horizontalActionCardsKeepEqualHeightForShortAndLongLabels() {
        val parent = FrameLayout(activity()); val presenter = ActionPresenter()
        val holder = presenter.onCreateViewHolder(parent) as ActionPresenter.ActionViewHolder
        parent.addView(holder.view)
        presenter.onBindViewHolder(holder, TvAction(1, "Connect", "Idle", R.drawable.ic_service_idle))
        layout(parent); val shortHeight = holder.view.measuredHeight
        presenter.onBindViewHolder(holder, TvAction(1, "A very long translated connection action wrapping onto two lines", "Connected\nA very long selected profile name which wraps\nAn active profile", R.drawable.ic_service_active))
        layout(parent); assertEquals(shortHeight, holder.view.measuredHeight)
        assertEquals(2, holder.title.minLines); assertEquals(3, holder.subtitle.minLines)
    }
    @Test @Config(qualifiers = "w390dp-h844dp-port")
    fun trafficCardHeightStaysFixedWhenCountersGrow() {
        val parent = FrameLayout(activity()); val presenter = ActionPresenter()
        val holder = presenter.onCreateViewHolder(parent)
        parent.addView(holder.view)
        presenter.onBindViewHolder(holder, TvAction(8, "Connection", "Idle\n0 B/s", R.drawable.ic_remote_groups))
        layout(parent); val height = holder.view.measuredHeight
        presenter.onBindViewHolder(holder, TvAction(8, "Connection", "Connected\nLong current profile name\n999 GiB/s up\n999 GiB/s down\nTotals 999 TiB\nLast check passed", R.drawable.ic_remote_groups))
        layout(parent); assertEquals(height, holder.view.measuredHeight)
    }
    @Test fun rowPresenterKeepsPhonePaddingStableAcrossFocusAndExpansion() {
        val parent = FrameLayout(activity()); val presenter = StableTvRowPresenter(true)
        assertEquals(androidx.leanback.widget.FocusHighlight.ZOOM_FACTOR_NONE, presenter.focusZoomFactor)
        val outer = presenter.onCreateViewHolder(parent)
        presenter.onBindViewHolder(outer, androidx.leanback.widget.ListRow(androidx.leanback.widget.HeaderItem("Actions"), ArrayObjectAdapter(ActionPresenter())))
        val holder = presenter.getRowViewHolder(outer) as androidx.leanback.widget.ListRowPresenter.ViewHolder
        presenter.setRowViewSelected(outer, true)
        val before = listOf(holder.gridView.paddingTop, holder.gridView.paddingBottom)
        presenter.setRowViewExpanded(outer, true); presenter.setRowViewSelected(outer, false)
        assertEquals(before, listOf(holder.gridView.paddingTop, holder.gridView.paddingBottom))
        presenter.freeze(holder, false); assertFalse(holder.gridView.isChildLayoutAnimated)
        assertEquals(androidx.leanback.widget.BaseGridView.FOCUS_SCROLL_ITEM, holder.gridView.focusScrollStrategy)
    }
    @Test fun tvKeepsRemoteFocusZoom() {
        assertEquals(androidx.leanback.widget.FocusHighlight.ZOOM_FACTOR_SMALL, StableTvRowPresenter(false).focusZoomFactor)
    }
    @Test fun tvAddCatalogIncludesEveryInboundPhoneMethodPlusPhoneAndUrl() {
        val context = activity()
        val source = io.nekohasekai.sagernet.ui.ProfileCreationActions.addMenu(context)
        val expected = (0 until source.size()).map { source.getItem(it) }
            .filter { it.isVisible && it.itemId != R.id.action_send_to_tv }
            .map { if (it.hasSubMenu()) TvProfileAddCatalog.MANUAL else it.itemId }.toSet()
        val ids = TvProfileAddCatalog.entries(context).map { it.id }
        assertEquals(expected + setOf(TvProfileAddCatalog.PHONE, TvProfileAddCatalog.URL, TvProfileAddCatalog.SEND), ids.toSet())
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(9, ids.size)
    }
    @Test fun allSeventeenPhoneManualEditorsHaveSharedExplicitIntents() {
        val context = activity()
        val entries = io.nekohasekai.sagernet.ui.ProfileCreationActions.manualEntries(context)
        assertEquals(17, entries.size)
        for (entry in entries) {
            val intent = io.nekohasekai.sagernet.ui.ProfileCreationActions.intent(context, entry.id)
            assertNotNull(entry.title, intent)
            assertNotNull(entry.title, intent!!.component)
            assertNotEquals(android.content.Intent.ACTION_SEND, intent.action)
        }
        assertNull(io.nekohasekai.sagernet.ui.ProfileCreationActions.intent(context, R.id.action_import_file))
    }
    @Test fun sharedVlessEditorRetainsVlessFlagAndVmessDoesNot() {
        val context = activity()
        val vless = io.nekohasekai.sagernet.ui.ProfileCreationActions.intent(context, R.id.action_new_vless)!!
        val vmess = io.nekohasekai.sagernet.ui.ProfileCreationActions.intent(context, R.id.action_new_vmess)!!
        assertEquals(vmess.component, vless.component)
        assertTrue(vless.getBooleanExtra("vless", false))
        assertFalse(vmess.getBooleanExtra("vless", false))
    }
    @Test @Config(qualifiers = "ru") fun emptyGroupHintOffersChoiceInsteadOfDirectPhoneImport() {
        val text = activity().getString(R.string.tv_empty)
        assertTrue(text.contains("выбрать способ"))
        assertFalse(text.contains("импорта с телефона"))
    }

    @Test fun tvActionCanReceiveKeyboardFocusAfterTouchModePicker() {
        val context = activity(); val parent = FrameLayout(context); context.setContentView(parent)
        val presenter = ActionPresenter(); val holder = presenter.onCreateViewHolder(parent)
        parent.addView(holder.view); layout(parent)
        assertTrue(holder.view.isFocusableInTouchMode)
        assertTrue(holder.view.requestFocus())
        assertTrue(holder.view.hasFocus())
    }

}
