//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.04.12 um 10:21:29 AM CEST 
//


package com.becketal.lsc.plugins.connectors.foreignkeysrc.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keySource" type="{http://lsc-project.org/XSD/lsc-foreign-key-plugin-1.0.xsd}sourceType"/>
 *         &lt;element name="dataSource" type="{http://lsc-project.org/XSD/lsc-foreign-key-plugin-1.0.xsd}sourceType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "keySource",
    "dataSource"
})
@XmlRootElement(name = "ForeignKeySrcServiceConfig")
public class ForeignKeySrcServiceConfig {

    @XmlElement(required = true)
    protected SourceType keySource;
    @XmlElement(required = true)
    protected SourceType dataSource;

    /**
     * Ruft den Wert der keySource-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SourceType }
     *     
     */
    public SourceType getKeySource() {
        return keySource;
    }

    /**
     * Legt den Wert der keySource-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceType }
     *     
     */
    public void setKeySource(SourceType value) {
        this.keySource = value;
    }

    /**
     * Ruft den Wert der dataSource-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SourceType }
     *     
     */
    public SourceType getDataSource() {
        return dataSource;
    }

    /**
     * Legt den Wert der dataSource-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceType }
     *     
     */
    public void setDataSource(SourceType value) {
        this.dataSource = value;
    }

}
