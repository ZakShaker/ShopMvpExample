
class ShopFragment : BaseFragment(), ShopView {
    override val layoutRes = R.layout.fragment_shop

    // Используется делегат из Android Navigation только для удобного извлечения/создания аргументов
    private val args: ShopFragmentArgs by navArgs()

    // Используется Moxy
    @InjectPresenter
    lateinit var presenter: ShopPresenter

    @ProvidePresenter
    fun providePresenter(): ShopPresenter {
        require(args.shopId >= 0) { "Argument shopId should be non-negative, was ${args.shopId}" }
        return ShopPresenter(
                get(parentNavigationModuleQualifier),
                get(),
                args.shopId,
                args.shopName,
                args.query
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        btn_back?.setSafeOnClickListener {
            it.hideKeyboard()
            activity?.onBackPressed()
        }
        btn_sort?.setSafeOnClickListener { presenter.onSelectSortClicked() }
        initPreviewsLayout()
        SearchQueryChangedListener(
                afterTextChanged = { s ->
                    presenter.onQueryChanged(s?.toString() ?: "")
                }
        ).also {
            et_search?.addTextChangedListener(it)
        }
    }

    override fun showTitle(title: String) {
        tv_title?.text = title
    }

    override fun setInitialQuery(query: String) {
        et_search?.setText(query)
    }

    override fun showSorts(sorts: List<ShopsRepository.Sort>) {
        context?.let { ctxt ->
            FiltersDialog(
                    context = ctxt,
                    title = getString(R.string.shop_sort_title),
                    onFilterClicked = {
                        presenter.onSortSelected(ShopsRepository.Sort.values()[it.id])
                    },
                    filters = sorts.map {
                        Filter(
                                id = it.ordinal,
                                name = when (it) {
                                    ShopsRepository.Sort.New -> getString(R.string.shop_sort_new)
                                    ShopsRepository.Sort.Sale -> getString(R.string.shop_sort_sale)
                                    ShopsRepository.Sort.Price -> getString(R.string.shop_sort_price)
                                }
                        )
                    }
            ).show()
        }
    }

    override fun showLoadingPage(isLoading: Boolean) {
        if (isLoading) {
            view_loading?.visibility = View.VISIBLE
        } else {
            view_loading?.visibility = View.GONE
        }
    }

    override fun showCouldntLoadPage(show: Boolean) {
        if (show) {
            view_try_again?.visibility = View.VISIBLE
            view_try_again?.tv_message?.text = getString(R.string.partner_couldnt_load_info)
            view_try_again?.btn_try_again?.setSafeOnClickListener { presenter.onRefreshPage() }
        } else {
            view_try_again?.visibility = View.GONE
        }
    }

    override fun showFilters(filters: List<Filter>) {
        context?.let { ctxt ->
            FiltersDialog(
                    context = ctxt,
                    title = getString(R.string.shop_sort_title),
                    onFilterClicked = {
                        presenter.onCategorySelected(it)
                    },
                    filters = filters
            ).show()
        }
    }

    override fun removeFilter() {
        tv_category_name?.text = context?.getString(R.string.catalog_all_categories_name)
        btn_choose_category?.visibility = View.GONE
    }

    override fun showSelectedFilter(filter: Filter) {
        tv_category_name?.text = filter.name
        btn_choose_category?.apply {
            visibility = View.VISIBLE
            setSafeOnClickListener { presenter.onRemoveCategory() }
        }
    }

    override fun showFiltersLoaded() {
        btn_choose_category?.apply {
            visibility = View.VISIBLE
            setSafeOnClickListener { presenter.onSelectCategoryClicked() }
            rv_previews?.let { rv ->
                rv.setPadding(rv.paddingLeft, 0, rv.paddingRight, rv.paddingRight)
            }
        }
        tv_category_name?.apply {
            text = context?.getString(R.string.catalog_all_categories_name)
            visibility = View.VISIBLE
            setSafeOnClickListener { presenter.onSelectCategoryClicked() }
        }
        btn_remove_category?.visibility = View.GONE
    }

    override fun showEmptyCardsProgress(show: Boolean) {
        if (show) {
            rv_previews?.visibility = View.GONE
            view_try_again?.apply {
                visibility = View.VISIBLE
                tv_message?.text = getString(R.string.catalog_no_preview_cards)
                btn_try_again?.setSafeOnClickListener {
                    presenter.onRefreshPage()
                    view_try_again?.apply {
                        visibility = View.GONE
                    }
                }
            }
        } else {
            rv_previews?.visibility = View.VISIBLE
            view_try_again?.visibility = View.GONE
        }
    }

    override fun showEmptyCards(show: Boolean) {
        view_placeholder?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showEmptyCardsError(show: Boolean) {
        if (show) {
            rv_previews?.visibility = View.GONE
            view_try_again?.apply {
                visibility = View.VISIBLE
                tv_message?.text = getString(R.string.catalog_no_preview_cards)
                btn_try_again?.setSafeOnClickListener {
                    presenter.onRefreshPage()
                    view_try_again?.apply {
                        visibility = View.GONE
                    }
                }
            }
        } else {
            rv_previews?.visibility = View.VISIBLE
            view_try_again?.visibility = View.GONE
        }
    }

    override fun showCards(show: Boolean, data: List<CatalogItemViewModel>) {
        if (show) {
            rv_previews?.visibility = View.VISIBLE
            previewsAdapter.swapData(data)
        } else {
            rv_previews?.visibility = View.GONE
        }
    }

    override fun showRefreshCardsProgress(show: Boolean) {
        view_loading?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPageOfCardsProgress(show: Boolean) {
        pb_loading?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private lateinit var previewsAdapter: DiffUtilCompositeAdapter
    private fun initPreviewsLayout() {
        rv_previews?.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context).apply {
                addOnScrollListener(
                        OnEndOfVerticalScrollViewListener(this) {
                            presenter.onLoadMorePreviews()
                        }
                )
                addOnScrollListener(
                        OnElementShownInVerticalLinearLayoutListener(this) {
                            presenter.onCardsAppeared(it)
                        }
                )
            }

            adapter = DiffUtilCompositeAdapter.Builder()
                    .add(AdultContentDelegateAdapter { presenter.onAdultContentConfirmed() })
                    .add(ProductPreviewAdapter { product ->
                        context?.let {
                            AddProductToShoppingNotesDialog(it, product.name) { productName ->
                                presenter.onAddProductToNewShoppingNote(productName)
                            }.show()
                        }
                    })
                    .build()
                    .also { previewsAdapter = it }

            isNestedScrollingEnabled = true

        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }

}