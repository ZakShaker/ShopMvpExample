
@InjectViewState
class ShopPresenter(
        private val flowRouter: FlowRouter,
        private val interactor: CatalogInteractor,
        private val shopId: Long,
        private val shopName: String?,
        private var currentQuery: String? = null
) : PreviewCardsPresenter<ShopView>(flowRouter, interactor) {

    private lateinit var categories: List<Filter>
    private var currentSort: ShopsRepository.Sort? = null
    private var currentCategory: Filter? = null

    private val querySubject = PublishSubject.create<String>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        shopName?.let { viewState?.showTitle(it) }
        loadPage(currentQuery)
        querySubject.toFlowable(BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { query ->
                    currentQuery = query?.takeIf { it.isNotBlank() }
                    loadPage(currentQuery)
                })
                .disposeOnDestroy()
    }

    fun onQueryChanged(query: String) {
        querySubject.onNext(query)
    }

    fun onRefreshPage() {
        loadPage()
    }

    fun onSelectSortClicked() {
        viewState?.showSorts(ShopsRepository.Sort.values().toList())
    }

    fun onSortSelected(sort: ShopsRepository.Sort) {
        currentSort = sort
        reinitPreviewCardsPaginator(currentQuery)
    }

    fun onSelectCategoryClicked() {
        if (!categories.isNullOrEmpty())
            viewState?.showFilters(categories)
    }

    fun onCategorySelected(category: Filter) {
        currentCategory = category
        viewState?.showSelectedFilter(category)
        reinitPreviewCardsPaginator(currentQuery)
    }

    fun onRemoveCategory() {
        viewState?.showEmptyCards(false)
        viewState?.removeFilter()
        loadPage()
    }

    fun onAdultContentConfirmed() {
        interactor.confirmAdultContent()
        refreshPreviewCards()
    }

    private fun loadPage(query: String? = null) {
        viewState?.showLoadingPage(true)
        viewState?.showCouldntLoadPage(false)
        interactor.getProductsCategories(shopId = shopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { viewState?.showLoadingPage(false) }
                .subscribeBy(
                        onSuccess = {
                            categories = it
                            if (it.isNotEmpty())
                                viewState?.showFiltersLoaded()
                            loadPreviews(query)
                        },
                        onError = {
                            viewState?.showCouldntLoadPage(true)
                        }
                )
                .disposeOnDestroy()
    }

    private fun loadPreviews(query: String?) {
        reinitPreviewCardsPaginator(query)
    }

    private fun reinitPreviewCardsPaginator(query: String?) {
        initPreviewCardsPaginator { pageNum ->
            (if (query == null)
                interactor.getProductsInSelectedShop(
                        shopId = shopId,
                        pageNum = pageNum,
                        categoryId = currentCategory?.id,
                        sortBy = currentSort
                )
            else
                interactor.searchProductsInSelectedShopByQuery(
                        query = query,
                        shopId = shopId,
                        pageNum = pageNum,
                        categoryId = currentCategory?.id,
                        sortBy = currentSort
                ))
                    .map { page -> handlePreviews(page) }
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
        refreshPreviewCards()
    }

    private fun handlePreviews(
            page: List<PreviewViewModel>
    ): List<CatalogItemViewModel> = page.let { previews ->
        previews.forEach { preview ->
            previewCards[preview.id] = preview
        }
        previews
    }

    fun onBackPressed() {
        flowRouter.exit()
    }

    fun onAddProductToNewShoppingNote(productName: String) {
        flowRouter.startFlow(Screens.NotesFlow(productName))
    }

}