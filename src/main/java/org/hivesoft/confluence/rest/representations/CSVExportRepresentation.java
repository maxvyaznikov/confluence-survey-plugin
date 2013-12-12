package org.hivesoft.confluence.rest.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CSVExportRepresentation {

  @XmlElement
  private String uri;

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof CSVExportRepresentation)) return false;

    return (((CSVExportRepresentation) o).getUri().equalsIgnoreCase(uri));
  }
}
