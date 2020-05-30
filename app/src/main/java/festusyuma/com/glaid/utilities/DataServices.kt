package festusyuma.com.glaid.utilities

object DataServices {
    val fragmentPageDatas = mutableListOf(
        CarouselModel("activeIcon1", "inactiveIcon2", "inactiveIcon3", "ic_trucklogo"),
        CarouselModel("activeIcon1", "activeIcon2", "inactiveIcon3", "ic_phoneslogo"),
        CarouselModel("activeIcon1", "activeIcon2", "activeIcon3", "ic_cardslogo")

    )
    val questions = listOf(
        Question(
            1,
            "Question 1 and some plenty details about it detail about it",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat." +
                    " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore" +
                    " eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                    "qui officia deserunt mollit anim id est laborum", "imageUrl"
        ), Question(
            2,
            "Question 2 and some plenty details about it detail about it",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat." +
                    " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore" +
                    " eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                    "qui officia deserunt mollit anim id est laborum", "imageUrl"
        ), Question(
            3,
            "Question 3 and some plenty details about it detail about it",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat." +
                    " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore" +
                    " eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                    "qui officia deserunt mollit anim id est laborum", "imageUrl"
        ), Question(
            4,
            "Question 4 and some plenty details about it detail about it",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat." +
                    " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore" +
                    " eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                    "qui officia deserunt mollit anim id est laborum", "imageUrl"
        )
    )
    val orderHistoryList = listOf(
        OrderHistory(
            1,
            "1000",
            "Standard Oil",
            "8, Ogidi cresent, Lekki Phase 1",
            "Delivered"
        ),
        OrderHistory(
            2,
            "2000",
            "Standard Diesel",
            "31, Ogidi cresent, Lekki Phase 1",
            "Delivering..."
        ),
        OrderHistory(
            3,
            "4000",
            "Standard Oil",
            "18, Ogidi cresent, Lekki Phase 1",
            "Delivery incomplete"
        ),
        OrderHistory(
            4,
            "1000",
            "Standard Oil",
            "38, Ogidi cresent, Lekki Phase 1",
            "Delivered"
        ),
        OrderHistory(
            1,
            "5000",
            "Standard Diesel",
            "8, Ogidi cresent, Lekki Phase 1",
            "Delivering..."
        )
    )
}