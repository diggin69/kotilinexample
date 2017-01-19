class RecycleBin {
    /**
     * Views that were on screen at the start of layout. This array is populated at the start of
     * layout, and at the end of layout all view in activeViews are moved to scrapViews.
     * Views in activeViews represent a contiguous range of Views, with position of the first
     * view store in mFirstActivePosition.
     */
    private val activeViews = arrayOfNulls<View>(0)
    private val activeViewTypes = IntArray(0)

    /** Unsorted views that can be used by the adapter as a convert view.  */
    private var scrapViews: Array<SparseArray<View>>? = null

    private var viewTypeCount: Int = 0

    private var currentScrapViews: SparseArray<View>? = null

    fun setViewTypeCount(viewTypeCount: Int) {
        if (viewTypeCount < 1) {
            throw IllegalArgumentException("Can't have a viewTypeCount < 1")
        }

        val scrapViews = arrayOfNulls<SparseArray<*>>(viewTypeCount)
        for (i in 0..viewTypeCount - 1) {
            scrapViews[i] = SparseArray<View>()
        }
        this.viewTypeCount = viewTypeCount
        currentScrapViews = scrapViews[0]
        this.scrapViews = scrapViews
    }

    protected fun shouldRecycleViewType(viewType: Int): Boolean {
        return viewType >= 0
    }

    /** @return A view from the ScrapViews collection. These are unordered.
     */
    internal fun getScrapView(position: Int, viewType: Int): View? {
        if (viewTypeCount == 1) {
            return retrieveFromScrap(currentScrapViews, position)
        } else if (viewType >= 0 && viewType < scrapViews!!.size) {
            return retrieveFromScrap(scrapViews!![viewType], position)
        }
        return null
    }

    /**
     * Put a view into the ScrapViews list. These views are unordered.

     * @param scrap The view to add
     */
    internal fun addScrapView(scrap: View, position: Int, viewType: Int) {
        if (viewTypeCount == 1) {
            currentScrapViews!!.put(position, scrap)
        } else {
            scrapViews!![viewType].put(position, scrap)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            scrap.setAccessibilityDelegate(null)
        }
    }

    /** Move all views remaining in activeViews to scrapViews.  */
    internal fun scrapActiveViews() {
        val activeViews = this.activeViews
        val activeViewTypes = this.activeViewTypes
        val multipleScraps = viewTypeCount > 1

        var scrapViews: SparseArray<View> = currentScrapViews
        val count = activeViews.size
        for (i in count - 1 downTo 0) {
            val victim = activeViews[i]
            if (victim != null) {
                val whichScrap = activeViewTypes[i]

                activeViews[i] = null
                activeViewTypes[i] = -1

                if (!shouldRecycleViewType(whichScrap)) {
                    continue
                }

                if (multipleScraps) {
                    scrapViews = this.scrapViews!![whichScrap]
                }
                scrapViews.put(i, victim)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    victim.setAccessibilityDelegate(null)
                }
            }
        }

        pruneScrapViews()
    }

    /**
     * Makes sure that the size of scrapViews does not exceed the size of activeViews.
     * (This can happen if an adapter does not recycle its views).
     */
    private fun pruneScrapViews() {
        val maxViews = activeViews.size
        val viewTypeCount = this.viewTypeCount
        val scrapViews = this.scrapViews
        for (i in 0..viewTypeCount - 1) {
            val scrapPile = scrapViews!![i]
            var size = scrapPile.size()
            val extras = size - maxViews
            size--
            for (j in 0..extras - 1) {
                scrapPile.remove(scrapPile.keyAt(size--))
            }
        }
    }

    companion object {

        internal fun retrieveFromScrap(scrapViews: SparseArray<View>, position: Int): View? {
            val size = scrapViews.size()
            if (size > 0) {
                // See if we still have a view for this position.
                for (i in 0..size - 1) {
                    val fromPosition = scrapViews.keyAt(i)
                    val view = scrapViews.get(fromPosition)
                    if (fromPosition == position) {
                        scrapViews.remove(fromPosition)
                        return view
                    }
                }
                val index = size - 1
                val r = scrapViews.valueAt(index)
                scrapViews.remove(scrapViews.keyAt(index))
                return r
            } else {
                return null
            }
        }
    }
}