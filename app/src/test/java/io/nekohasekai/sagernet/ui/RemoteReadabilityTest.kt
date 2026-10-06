package io.nekohasekai.sagernet.ui

import org.junit.Assert.*
import org.junit.Test

class RemoteReadabilityTest {
    @Test fun phoneTouchDensityDoesNotChange() { assertEquals(1f, RemoteReadability.fontScale(false, 1f), 0f) }
    @Test fun tvAndRemoteOnlyDevicesGetReadableMinimum() { assertEquals(1.2f, RemoteReadability.fontScale(true, 1f), 0f) }
    @Test fun largerAccessibilityChoiceIsPreserved() { assertEquals(1.8f, RemoteReadability.fontScale(true, 1.8f), 0f); assertEquals(1.8f, RemoteReadability.fontScale(false, 1.8f), 0f) }
    @Test fun invalidScaleIsSanitized() { assertEquals(1.2f, RemoteReadability.fontScale(true, Float.NaN), 0f); assertEquals(1f, RemoteReadability.fontScale(false, -1f), 0f) }
}
