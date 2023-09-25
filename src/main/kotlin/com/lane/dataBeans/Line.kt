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

    @field:XmlAttribute(name = "id")
    var id = ""

    @field:XmlAttribute(name = "fileRelativePath")
    var fileRelativePath = ""

    @field:XmlAttribute(name = "selectionLine")
    var selectionLine = -1

    @field:XmlAttribute(name = "fileName")
    var fileName = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Line

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}