class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var viewPager: InfiniteViewPager = findViewById(R.id.viewpager) as InfiniteViewPager
        val pagerAdapter = PagerAdapter(this)
        viewPager.adapter = pagerAdapter
    }

}
