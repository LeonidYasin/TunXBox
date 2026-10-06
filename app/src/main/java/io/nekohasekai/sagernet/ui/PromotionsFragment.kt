package io.nekohasekai.sagernet.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.button.MaterialButton
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.launchCustomTab

/** Local information, not an automatic jump to upstream's third-party promotions. */
class PromotionsFragment : ToolbarFragment(R.layout.layout_promotions) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.ads)
        view.findViewById<MaterialButton>(R.id.promotions_project).setOnClickListener {
            requireContext().launchCustomTab(ProjectLinks.REPOSITORY)
        }
        view.findViewById<MaterialButton>(R.id.promotions_issues).setOnClickListener {
            requireContext().launchCustomTab(ProjectLinks.ISSUES)
        }
    }
}
