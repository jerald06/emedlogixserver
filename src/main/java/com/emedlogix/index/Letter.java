//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.0 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.07.13 at 09:55:40 PM IST 
//


package com.emedlogix.index;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{}title"/&gt;
 *           &lt;element ref="{}diff"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{}mainTerm" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "title",
    "diff",
    "mainTerm"
})
@XmlRootElement(name = "letter")
public class Letter {

    protected Title title;
    protected Diff diff;
    @XmlElement(required = true)
    protected List<MainTerm> mainTerm;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link Title }
     *     
     */
    public Title getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link Title }
     *     
     */
    public void setTitle(Title value) {
        this.title = value;
    }

    /**
     * Gets the value of the diff property.
     * 
     * @return
     *     possible object is
     *     {@link Diff }
     *     
     */
    public Diff getDiff() {
        return diff;
    }

    /**
     * Sets the value of the diff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Diff }
     *     
     */
    public void setDiff(Diff value) {
        this.diff = value;
    }

    /**
     * Gets the value of the mainTerm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the mainTerm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMainTerm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MainTerm }
     * 
     * 
     */
    public List<MainTerm> getMainTerm() {
        if (mainTerm == null) {
            mainTerm = new ArrayList<MainTerm>();
        }
        return this.mainTerm;
    }

}
