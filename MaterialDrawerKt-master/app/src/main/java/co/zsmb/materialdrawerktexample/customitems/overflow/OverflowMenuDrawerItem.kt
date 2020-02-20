package co.zsmb.materialdrawerktexample.customitems.overflow

import android.view.View
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.PopupMenu
import co.zsmb.materialdrawerktexample.R
import com.mikepenz.iconics.IconicsColor.Companion.colorInt
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize.Companion.dp
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem
import com.mikepenz.materialdrawer.model.BaseViewHolder

/**
 * Created by mikepenz on 03.02.15.
 */
class OverflowMenuDrawerItem : BaseDescribeableDrawerItem<OverflowMenuDrawerItem, OverflowMenuDrawerItem.ViewHolder>() {
    var menu: Int = 0
        private set

    var onMenuItemClickListener: PopupMenu.OnMenuItemClickListener? = null
        private set

    var onDismissListener: PopupMenu.OnDismissListener? = null
        private set

    override val type: Int
        get() = R.id.material_drawer_item_overflow_menu

    override val layoutRes: Int
        @LayoutRes
        get() = R.layout.material_drawer_item_overflow_menu_primary

    fun withMenu(menu: Int): OverflowMenuDrawerItem {
        this.menu = menu
        return this
    }

    fun withOnMenuItemClickListener(onMenuItemClickListener: PopupMenu.OnMenuItemClickListener): OverflowMenuDrawerItem {
        this.onMenuItemClickListener = onMenuItemClickListener
        return this
    }


    fun withOnDismissListener(onDismissListener: PopupMenu.OnDismissListener): OverflowMenuDrawerItem {
        this.onDismissListener = onDismissListener
        return this
    }

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val ctx = holder.itemView.context

        //bind the basic view parts
        bindViewHelper(holder)

        //handle menu click
        holder.menu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            val inflater = popup.menuInflater
            inflater.inflate(menu, popup.menu)

            popup.setOnMenuItemClickListener(onMenuItemClickListener)
            popup.setOnDismissListener(onDismissListener)

            popup.show()
        }

        //handle image
        holder.menu.setImageDrawable(IconicsDrawable(ctx, GoogleMaterial.Icon.gmd_more_vert).size(dp(12)).color(colorInt(getIconColor(ctx))))

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, holder.itemView)
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : BaseViewHolder(view) {
        //protected ImageButton ibOverflow;
        internal val menu: ImageButton = view.findViewById<ImageButton>(R.id.material_drawer_menu_overflow)
    }
}
