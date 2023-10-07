package com.lane.dataBeans

import javax.xml.bind.annotation.*

@XmlRootElement(name = "lineStack")
@XmlAccessorType(XmlAccessType.FIELD)
class LineStack {
    @field:XmlAttribute(name = "defaultItemId")
    var defaultItemId = ""

    @field:XmlAttribute(name = "showLineIndexNumber")
    var showLineIndexNumber = true

    @field:XmlAttribute(name = "showClassName")
    var showClassName = true

    @field:XmlElement(name = "item")
    var itemList: ArrayList<Item>? = null



}