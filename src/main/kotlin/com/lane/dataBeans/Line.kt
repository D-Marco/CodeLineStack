package com.lane.dataBeans

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType(XmlAccessType.FIELD)
class Line {
    @field:XmlAttribute(name = "describe")
    var describe = ""

    @field:XmlAttribute(name = "text")
    var text = ""

    @field:XmlAttribute(name = "fileRelativePath")
    var fileRelativePath = ""

    @field:XmlAttribute(name = "selectionLine")
    var selectionLine = -1

    @field:XmlAttribute(name = "fileName")
    var fileName = ""

    override fun toString(): String {
        return "[ $fileName ]:  $text"
    }

}