# UI quality and remote-control review

Scope: static review of PR #1 (`095987cd` UI), checked against upstream `main` / `master`. This is not a visual/device acceptance test: no Android TV emulator, phone screen captures, overscan test or real remote was used. Build and protocol tests do not establish that every focus path works on devices.

## Overall result

- **TV mode:** appropriate Leanback foundation and visible focus states, but still a basic profile-launching interface rather than a complete remote-first client. Important usability work remains.
- **Phone mode:** feature-complete upstream interface for touch usage, with additional focusability for remotes. It should not yet be called fully remote-accessible.
- **Recommendation:** finish the priority remote-navigation items below before calling PR #1 production-ready for TV. A larger visual redesign can be a separate PR.

## TV mode

### What is good

- Uses `BrowseSupportFragment` and Leanback rows rather than imitating touch controls.
- Action cards are 240×100 dp, profile cards 280×160 dp; focused cards have a cyan outline and an 8% scale animation.
- Light primary text on a dark slate background gives clear visual hierarchy.
- Profile name and protocol are separated; imports from a phone remove the need to type long configurations with a remote.
- Shared Android application includes both TV and phone UI; switching to upstream editors is possible.

### Priority improvements

1. **Selected/running profile must be visible.** `ProfileCardPresenter` always hides `statusDot`; `profile_status` contains a server address rather than selection/service state. Use separate selected and connected markers plus visible labels; do not conflate focus with selection or connection.
2. **Do not rebuild actions while a remote user is navigating.** `updateActionsRow()` clears and repopulates the adapter on service changes. This removes the focused item and risks resetting focus/scroll. Replace the first start/stop item in place; preserve focused action ID. Profile reloads should similarly preserve profile ID and scroll position.
3. **Provide group/subscription navigation.** The TV screen loads only `DataStore.selectedGroup`. There is no group selector or TV update-subscription action, so older groups become inaccessible without changing to phone UI. Add a remote-friendly group row/selector and explicit update action.
4. **Make common paths short.** “Import from phone” is currently the seventh action. Put start/stop, profiles/groups and phone import first; move uncommon import/editor methods into a secondary menu.
5. **Show a persistent service status.** “Service running” is coarse; show disconnected/connecting/connected/stopping and the active profile. Profile click immediately starts or reloads the proxy; label this behavior so users do not interpret it as selection only.
6. **Media key parity.** Play/Pause handling exists in phone `MainActivity`, but not in `MainActivityTv`. Implement the same shortcut once, with safeguards during transitions, and show a short hint for OK/Back/Menu/Play-Pause. Ordinary remotes still need an obvious on-screen alternative.

### Visual refinements

- Action subtitles are 13 sp; server metadata is 12 sp. Raise secondary TV text toward 16–18 sp and test at real viewing distance; avoid fixed-height clipping with larger font settings.
- Replace emoji-only decorative prefixes with consistent vector icons and localized labels; TV fonts do not always render emoji consistently.
- Protocol names currently come from class names. Replace implementation terminology with user-facing localized protocol labels.
- Add text overflow rules to action titles/subtitles and server addresses. Check long profile names, localization, display scaling, low-resolution TV outputs and overscan.
- Verify focus animation works with Leanback's own highlighting and does not clip at row edges.
- Add edit/details/delete context actions reachable without long press or switching UI.

## Phone mode

### What is good

- Retains upstream Material navigation, profile editors, groups, subscriptions and theme behavior.
- Profile cards, edit/share controls, FAB and status panel have focusability; profile cards get a visible focus outline.
- Several explicit left/up/down links were added, and toolbar touch-focus blocking is disabled.
- Phone mode preference persists and deep-link imports retain the upstream handler.

### Remote-control gaps to verify/fix

1. **Horizontal focus paths are incomplete.** In `layout_profile.xml`, `nextFocusRight="@id/edit"` sits on an inner non-focusable wrapper, not on the focusable root profile card. Put directional links on the controls that can actually receive focus; connect profile → edit → share → delete and back within each row.
2. **Drawer behavior needs boundary-aware navigation.** `MainActivity.onKeyDown()` opens the drawer on an unhandled Left key. Test that Left from nested edit/share controls returns within the row before opening navigation. Back should close the drawer and restore the last content focus.
3. **Fragment key handling can steal focus.** `ConfigurationFragment.onKeyDown()` requests list focus for most non-up/down keys if the list has no focus. It should not redirect a key intended for a toolbar/FAB or other focused control.
4. **Focus is not consistently visualized.** Profile rows have a clear outline, but small icon controls rely on theme-dependent borderless backgrounds. Use a consistent focus treatment on edit/share/overflow/buttons, and verify it in light, dark and black themes.
5. **Touch gestures must have remote alternatives.** Audit swipe-to-delete, drag/reorder, long-press selection, floating controls and popup menus. Expose equivalent labelled menu actions where needed.
6. **Settings/editor navigation needs device tests.** A focusable container is not evidence that every preference, validation dialog, Save/Cancel button and IME interaction can be reached. Walk each editor with D-pad only; restore focus after dialogs and after returning from editors.

### Changes to avoid

Do not replace the touch-oriented phone screen with fixed-size TV cards. Keep compact touch layouts and add remote-aware focus/navigation behavior when a hardware navigation device is in use. Preserve upstream workflows, themes and accessibility rather than applying TV styles globally.

## Acceptance checklist before merging

- Android TV at 720p/1080p/4K density configurations; phone portrait/landscape; larger font settings.
- D-pad only: reach every primary action, change group, choose/edit a profile, update a subscription, import via phone, start/stop VPN, close dialogs and return without losing focus.
- Service transitions: maintain focus while connecting/reconnecting/stopping; no unsolicited focus jump after returning from QR/editor screens.
- Distinguish focused, selected and connected profiles visually and with text.
- Verify Media Play/Pause behavior in both modes and ensure ordinary remotes without this button are fully supported.
- TalkBack labels/roles, text truncation, dark/light themes, RTL and localization.
- Test a no-camera TV and a TV remote with only arrows/OK/Back.

## Release filename audit

The previous release used GitHub labels that differed from the real download names. It also retained old unsigned APKs. Publication now derives ABI labels from the actual native libraries inside each APK, verifies signatures, packages under the final real names, uploads without a misleading `#label`, downloads the uploaded bytes for SHA-256 verification, and removes superseded APKs only afterward.

A combined APK inspected during this review contains `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64`. This is why it is called **universal**, not “ARM-only universal”; the two separate split APKs are labelled by their individual ARM ABIs. Filename format:

`TunXBox-<version>-rc-android-tv-phone-<actual-ABI-or-universal>-preview-release-<commit8>.apk`

`release` describes the build type, not a guarantee of a production signing key. CI verifies APK signatures; release notes disclose that preview APKs may use the CI debug key. The platform minimum is Android 5.0 (minSdk 21). `apk-manifest.json` and `SHA256SUMS.txt` provide exact ABI lists, commit, sizes and checksums. The older `v1.4.2-rc` historical release is outside the rolling `v1.5.0-rc` cleanup scope.
