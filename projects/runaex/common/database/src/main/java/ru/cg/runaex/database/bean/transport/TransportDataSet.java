package ru.cg.runaex.database.bean.transport;

import java.io.Serializable;
import java.util.*;

/**
 * Date: 17.08.12
 * Time: 13:12
 *
 * @author Sabirov
 */
public class TransportDataSet implements Serializable {
  private static final long serialVersionUID = -1765141419933762857L;

  private Integer rowCounts;
  private Set<TransportData> sets;

  public TransportDataSet() {
  }

  public TransportDataSet(Integer rowCounts, Set<TransportData> sets) {
    this.rowCounts = rowCounts;
    this.sets = sets;
  }

  public Integer getRowCounts() {
    return rowCounts;
  }

  public void setRowCounts(Integer rowCounts) {
    this.rowCounts = rowCounts;
  }

  public Set<TransportData> getSets() {
    if (sets == null)
      return new LinkedHashSet<TransportData>();
    return sets;
  }

  public void setSets(Set<TransportData> sets) {
    this.sets = sets;
  }

  public void add(TransportData transportData) {
    if (this.sets == null) {
      this.sets = new LinkedHashSet<TransportData>();
    }
    this.sets.add(transportData);
  }

  public List<TransportData> getSortSets() {
    if (this.sets == null)
      return new ArrayList<TransportData>();
    List<TransportData> sortList = Arrays.asList(this.sets.toArray(new TransportData[this.sets.size()]));
    Collections.sort(sortList, new TransportDataComparator());
    return sortList;
  }

  public class TransportDataComparator implements Comparator<TransportData> {
    public int compare(TransportData o1, TransportData o2) {
      return o1.getNum().compareTo(o2.getNum());
    }
  }
}
