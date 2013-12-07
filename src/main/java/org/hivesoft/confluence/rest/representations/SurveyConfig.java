package org.hivesoft.confluence.rest.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveyConfig {

    @XmlElement
    private String iconSet;

    public String getIconSet() {
        return iconSet;
    }

    public void setIconSet(String iconSet) {
        this.iconSet = iconSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SurveyConfig))
            return false;
        return (((SurveyConfig) obj).getIconSet().equals(this.iconSet));
    }
}