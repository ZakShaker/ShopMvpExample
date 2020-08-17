# ShopMvpExample
Пример страницы магазина с продуктами и поиском внутри него.

# Несколько основных примерных классов с кодом
View и Presenter:
 - `ShopFragment.kt` - инициализирует виджеты и исполняет команды от презентера, использует слушателей прокрутки карточек. Для списка используется делегат-адаптеры для отображения разных карточек: 18+-предупреждений и товаров.
 - `ShopPresenter.kt` - принимает id магазина, отсылает запросы по поиску с фильтрацие и командует View отобразить карточки продуктов. Навигация осущетсвляется библиотекой Cicerone через `flowRouter`.

Дополнительно несколько классов и файлов:
 - `item_product.xml` - вёрстка карточки товара
 - `ProductPreviewAdapter.kt` - адаптер карточки товара
 - `OnElementShownInVerticalLinearLayoutListener.kt` - слушатель появления элемента в списке при прокрутке списка вниз


![Экран магазина](https://github.com/ZakShaker/ShopMvpExample/blob/master/fragment_shop_pic.png)
![Экран магазина Видео](https://github.com/ZakShaker/ShopMvpExample/blob/master/screenshot.gif)
