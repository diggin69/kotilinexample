abstract class InfinitePagerAdapter internal constructor(private val recycleBin: RecycleBin) : android.support.v4.view.PagerAdapter() {

    constructor() : this(RecycleBin()) {}

    init {
        recycleBin.setViewTypeCount(viewTypeCount)
    }

    val viewTypeCount: Int
        get() = 1

    fun getItemViewType(position: Int): Int {
        return 0
    }

    abstract val itemCount: Int

    open fun getView(position: Int, convertView: View, container: ViewGroup): View? {
        return null
    }

    override fun notifyDataSetChanged() {
        recycleBin.scrapActiveViews()
        super.notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewType = getItemViewType(position)
        var view: View? = null
        if (viewType != IGNORE_ITEM_VIEW_TYPE) {
            view = recycleBin.getScrapView(position, viewType)
        }
        view = getViewInternal(position, view, container)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
        val viewType = getItemViewType(position)
        if (viewType != IGNORE_ITEM_VIEW_TYPE) {
            recycleBin.addScrapView(view, position, viewType)
        }
    }

    protected fun getViewInternal(position: Int, convertView: View, container: ViewGroup): View {
        return getView(position, convertView, container)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override
            /**
             * Note: use getItemCount instead */
    fun getCount(): Int {
        return itemCount * InfiniteViewPager.FakePositionHelper.MULTIPLIER
    }

    companion object {

        internal val IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE
    }
}