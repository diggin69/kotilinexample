class InfiniteViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    private var mHandler: Handler? = null
    private var mAutoScroll: Boolean = false
    private var isInfinitePagerAdapter: Boolean = false
    private var mTouchedWhenAutoScroll: Boolean = false
    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mDelay = DEFAULT_AUTO_SCROLL_INTERVAL

    init {
        init()
    }

    internal fun init() {
        offscreenPageLimit = 1
        super.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener?.onPageScrolled(FakePositionHelper.getFakeFromReal(this@InfiniteViewPager, position), positionOffset, positionOffsetPixels)
                }
            }

            override fun onPageSelected(position: Int) {
                if (position < FakePositionHelper.getStartPosition(this@InfiniteViewPager) || position > FakePositionHelper.getEndPosition(this@InfiniteViewPager)) {
                    mHandler?.removeMessages(MSG_SET_PAGE)
                    val msg = mHandler?.obtainMessage(MSG_SET_PAGE)
                    msg?.arg1 = position
                    if (msg != null) {
                        mHandler?.sendMessageDelayed(msg, 500)
                    }
                    return
                }
                mOnPageChangeListener?.onPageSelected(FakePositionHelper.getFakeFromReal(this@InfiniteViewPager, position))
            }

            override fun onPageScrollStateChanged(state: Int) {
                mOnPageChangeListener?.onPageScrollStateChanged(state)
            }
        })
        mHandler = object : Handler() {
            override fun dispatchMessage(msg: Message) {
                when (msg.what) {
                    MSG_AUTO_SCROLL -> {
                        setItemToNext()
                        sendDelayMessage()
                    }
                    MSG_SET_PAGE -> setFakeCurrentItem(FakePositionHelper.getRealPositon(this@InfiniteViewPager, msg.arg1), false)
                }
            }
        }
    }

    @JvmOverloads fun startAutoScroll(delayTime: Long = this.mDelay) {
        if (adapter == null || adapter.count == 0)
            return
        this.mDelay = delayTime
        this.mAutoScroll = true
        sendDelayMessage()

    }

    private fun sendDelayMessage() {
        mHandler?.removeMessages(MSG_AUTO_SCROLL)
        mHandler?.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mDelay)
    }


    fun stopAutoScroll() {
        this.mAutoScroll = false
        mHandler?.removeMessages(MSG_AUTO_SCROLL)
    }

    override fun addOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        this.mOnPageChangeListener = listener
    }

    private fun setItemToNext() {
        val adapter = adapter
        if (adapter == null || adapter.count == 0) {
            stopAutoScroll()
            return
        }
        val totalCount = if (isInfinitePagerAdapter) FakePositionHelper.getRealAdapterSize(this) else adapter.count
        if (totalCount <= 1)
            return

        val nextItem = fakeCurrentItem + 1
        if (isInfinitePagerAdapter) {
            fakeCurrentItem = nextItem
        } else {
            if (nextItem == totalCount) {
                fakeCurrentItem = 0
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        if (this.mAutoScroll || this.mTouchedWhenAutoScroll) {
            val action = ev.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    this.mTouchedWhenAutoScroll = true
                    stopAutoScroll()
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(FakePositionHelper.getRealFromFake(this, item))
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(FakePositionHelper.getRealFromFake(this, item), smoothScroll)
    }

    override fun getCurrentItem(): Int {
        return FakePositionHelper.getFakeFromReal(this, fakeCurrentItem)
    }

    private var fakeCurrentItem: Int
        get() = super.getCurrentItem()
        set(item) = super.setCurrentItem(item)

    private fun setFakeCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
    }

    private val adapterSize: Int
        get() = if (adapter == null) 0 else adapter.count

    override fun setAdapter(adapter: PagerAdapter) {
        super.setAdapter(adapter)
        isInfinitePagerAdapter = getAdapter() is InfinitePagerAdapter
        if (!isInfinitePagerAdapter) {
            throw IllegalArgumentException("Currently, only InfinitePagerAdapter is supported")
        }
        setFakeCurrentItem(FakePositionHelper.getRealPositon(this@InfiniteViewPager, 0), false)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (this.mAutoScroll || this.mTouchedWhenAutoScroll) {
            val action = ev.action
            when (action) {
                MotionEvent.ACTION_UP -> {
                    this.mTouchedWhenAutoScroll = false
                    startAutoScroll()
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    object FakePositionHelper {
        val MULTIPLIER = 5
        fun getRealFromFake(viewPager: InfiniteViewPager, fake: Int): Int {
            var fake = fake
            val realAdapterSize = viewPager.adapterSize / MULTIPLIER
            if (realAdapterSize == 0)
                return 0
            fake = fake % realAdapterSize//ensure it
            val currentReal = viewPager.fakeCurrentItem
            val real = fake + (currentReal - currentReal % realAdapterSize)//set to the target level
            return real
        }

        fun getFakeFromReal(viewPager: InfiniteViewPager, real: Int): Int {
            val realAdapterSize = viewPager.adapterSize / MULTIPLIER
            if (realAdapterSize == 0)
                return 0
            return real % realAdapterSize
        }

        fun getStartPosition(viewPager: InfiniteViewPager): Int {
            val realAdapterSize = viewPager.adapterSize / MULTIPLIER
            return realAdapterSize
        }

        fun getEndPosition(viewPager: InfiniteViewPager): Int {
            val realAdapterSize = viewPager.adapterSize / MULTIPLIER
            return realAdapterSize * (MULTIPLIER - 1) - 1
        }

        fun getRealAdapterSize(viewPager: InfiniteViewPager): Int {
            return if (viewPager.isInfinitePagerAdapter) viewPager.adapterSize / MULTIPLIER else viewPager.adapterSize
        }

        fun getRealPositon(viewPager: InfiniteViewPager, position: Int): Int {
            val realAdapterSize = getRealAdapterSize(viewPager)
            if (realAdapterSize == 0)
                return 0
            val startPostion = getStartPosition(viewPager)
            val endPosition = getEndPosition(viewPager)
            if (position < startPostion) {
                return endPosition + 1 - realAdapterSize + position % realAdapterSize
            }
            if (position > endPosition) {
                return startPostion + position % realAdapterSize
            }
            return position
        }
    }

    companion object {
        private val DEFAULT_AUTO_SCROLL_INTERVAL: Long = 3000//3s
        private val MSG_AUTO_SCROLL = 1
        private val MSG_SET_PAGE = 2
    }
}
