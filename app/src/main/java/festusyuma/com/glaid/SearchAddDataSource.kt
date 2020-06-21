package festusyuma.com.glaid

import festusyuma.com.glaid.model.SearchAddresses

class SearchAddDataSource {
    companion object {
        // hard coded data for recycler view search
        fun createDataSet(): ArrayList<SearchAddresses>{
            val list = ArrayList<SearchAddresses>()
            list.add(
                SearchAddresses(
                    "Eko Hotels",
                    "1415 Adetokunbo Ademola Street, Victoria Island"
                )
            )
            list.add(
                SearchAddresses(
                    "Festusyuma",
                    "house 5, joel oguniake Street, amuodofin"
                )
            )
            list.add(
                SearchAddresses(
                    null,
                    "house 5, joel oguniake Street, amuodofin"
                )
            )
            return list
        }
    }
}