package io.nekohasekai.sagernet.ui.tv
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R
class GroupCardPresenter: Presenter() {
 private val delegate=ActionPresenter()
 override fun onCreateViewHolder(parent: ViewGroup)=delegate.onCreateViewHolder(parent)
 override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
  val group=item as TvGroupCard;val context=holder.view.context
  delegate.onBindViewHolder(holder,TvAction(group.id,group.name,context.getString(R.string.tv_group_row_count,group.count)+"\n"+
      context.getString(if(group.selected)R.string.tv_group_row_selected else R.string.tv_group_row_choose)+"\n"+
      context.getString(R.string.tv_quality_group_hint),R.drawable.ic_remote_groups))
  holder.view.isActivated=group.selected
  holder.view.contentDescription=group.name+". "+context.getString(if(group.selected)R.string.tv_group_row_selected else R.string.tv_group_row_choose)+". "+context.getString(R.string.tv_quality_group_hint)
 }
 override fun onUnbindViewHolder(holder: ViewHolder){holder.view.isActivated=false;delegate.onUnbindViewHolder(holder)}
}
class FunctionCardPresenter: Presenter() {
 private val delegate=ActionPresenter()
 override fun onCreateViewHolder(parent: ViewGroup)=delegate.onCreateViewHolder(parent)
 override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
  val entry=item as TvFunctionCard
  delegate.onBindViewHolder(holder,TvAction(entry.id.toLong(),entry.title,holder.view.context.getString(R.string.tv_function_direct),R.drawable.ic_baseline_more_vert_24))
 }
 override fun onUnbindViewHolder(holder: ViewHolder)=delegate.onUnbindViewHolder(holder)
}
/** Separate namespaces avoid focus collisions between group IDs and profile IDs. */
object TvGroupRows {
 const val BASE=1_000_000_000_000L
 fun orderedGroupIds(ids: List<Long>,selected: Long)=ids.filter {it==selected}+ids.filter {it!=selected}
 fun row(group: Long)=BASE+group
 fun focus(item: Any?)=when(item) {
  is TvQualityCard -> item.id;is TvConnectionCard -> item.id
  is TvAction -> item.id
  is TvProfileCard -> item.id
  is TvGroupCard -> -BASE-item.id
  is TvFunctionCard -> item.id.toLong()
  is TvEmptyHint -> Long.MIN_VALUE+item.groupId
  else -> -1L
 }
}
