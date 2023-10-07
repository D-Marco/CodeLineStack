package com.lane.dataBeans

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
class Item {

    @field:XmlAttribute(name = "id")
    var id = ""

    @field:XmlAttribute(name = "name")
    var name = ""

    @field:XmlElement(name = "line")
    var lineList: ArrayList<Line>? = null


}