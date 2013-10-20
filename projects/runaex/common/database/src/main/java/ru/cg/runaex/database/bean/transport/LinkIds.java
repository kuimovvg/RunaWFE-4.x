package ru.cg.runaex.database.bean.transport;

/**
 * @author urmancheev
 */
public class LinkIds {
  private Long linkId;
  private Long linkedObjectId;

  public LinkIds(Long linkId, Long linkedObjectId) {
    this.linkId = linkId;
    this.linkedObjectId = linkedObjectId;
  }

  public Long getLinkId() {
    return linkId;
  }

  public Long getLinkedObjectId() {
    return linkedObjectId;
  }
}
