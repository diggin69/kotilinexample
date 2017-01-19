class PagerAdapter(private val mContext: Context) : InfinitePagerAdapter() {

    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(mContext)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun getView(position: Int, view: View?, container: ViewGroup): View {
        var makeView = view
        val holder: ViewHolder
        if (makeView != null) {
            holder = makeView.tag as ViewHolder
        } else {
            makeView = mInflater.inflate(R.layout.infinite_viewpager, container, false)
            holder = ViewHolder(makeView)
            makeView!!.tag = holder
        }
        holder.position = position
        when (position % 3) {
            0 -> holder.image.setImageResource(R.mipmap.ic_1)
            1 -> holder.image.setImageResource(R.mipmap.ic_2)
            2 -> holder.image.setImageResource(R.mipmap.ic_3)
        }
        return makeView
    }

    private class ViewHolder(view: View) {
        var position: Int = 0
        var image: ImageView

        init {
            image = view.findViewById(R.id.item_image) as ImageView
        }
    }
}