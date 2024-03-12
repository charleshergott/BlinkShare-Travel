package app.blinkshare.android.notification

class Data {
    var product_id: String? = null
    var icon = 0
    var body: String? = null
    var title: String? = null

    constructor(product_id: String?, icon: Int, body: String?, title: String?) {
        this.product_id = product_id
        this.icon = icon
        this.body = body
        this.title = title
    }

    constructor() {}
}
