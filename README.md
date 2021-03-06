# ShopMvpExample
Пример страницы магазина с продуктами и поиском внутри него.

# Несколько основных примерных классов с кодом
View и Presenter:
 - `ShopFragment.kt` - инициализирует виджеты и исполняет команды от презентера, использует слушателей прокрутки карточек. Для списка используются делегат-адаптеры для отображения разных карточек: 18+предупреждений и товаров.
 - `ShopPresenter.kt` - принимает id магазина, отсылает запросы по поиску с фильтрацией/сортировкой и командует View отобразить карточки продуктов. Навигация осуществляется библиотекой Cicerone через `flowRouter` по принципу Single-Activity с flow-фрагментами и базовыми фрагментами. В качестве ServiceLocator-паттерна (вместо DI) используется библиотека Koin.

Дополнительно несколько классов и файлов:
 - `item_product.xml` - вёрстка карточки товара
 - `ProductPreviewAdapter.kt` - адаптер карточки товара
 - `OnElementShownInVerticalLinearLayoutListener.kt` - слушатель появления элемента в списке при прокрутке списка вниз
 
# Скриншоты
<img src="https://github.com/ZakShaker/ShopMvpExample/blob/master/screenshot.gif" height="500" />
