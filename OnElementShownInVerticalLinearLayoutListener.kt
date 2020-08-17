class OnElementShownInVerticalLinearLayoutListener(
        private val linearLayoutManager: LinearLayoutManager,
        private val onShown: (firstLastPosition: Pair<Int, Int>) -> Unit
) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
            getFirstLastVisiblePositions().let {
                onShown.invoke(it)
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0) {
            getFirstLastVisiblePositions().let {
                onShown.invoke(it)
            }
        }
    }

    private fun getFirstLastVisiblePositions(): Pair<Int, Int> = Pair(
            linearLayoutManager.findFirstVisibleItemPosition(),
            linearLayoutManager.findLastVisibleItemPosition()
    )
}