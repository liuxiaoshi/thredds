/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.coord;

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.util.Misc;

import java.util.*;

/**
 * Store objects of type T in a sparse array.
 * Conceptually a multidim array with shape[n] and totalsize.
 * Stored as track[totalsize] = {0 = missing, else = index+1 into List<T> content}
 * So we dont have to store missing Ts.
 *
 * @author caron
 * @since 11/24/13
 */
@Immutable
public class SparseArray<T> {
  static private final Logger logger = LoggerFactory.getLogger(SparseArray.class);

  private final int[] shape;    // multidim sizes
  private final int[] stride;  // for index calculation
  private final int totalSize; // product of sizes

  private final int[] track;    // index into content, size totalSize. LOOK could use byte, short to save memory ??
  private final List<T> content; // keep the things in a List.
  private final int ndups;

  public SparseArray( int[] shape, int[] track, List<T> content, int ndups) {
    this.shape = shape;
    this.totalSize = calcTotalSize(shape);
    this.stride = calcStrides(shape);

    this.track = track;
    this.content = Collections.unmodifiableList(content);
    this.ndups = ndups;

    if (track.length != totalSize)
      throw new IllegalStateException("track len "+track.length+" != totalSize "+totalSize);
  }

  static int calcTotalSize(int[] shape) {
    int total = 1;
    for (int aSize : shape) total *= aSize;
    return total;
  }

  private static int[] calcStrides(int[] shape) {
    int[] strides = new int[shape.length];
    int product = 1;
    for (int ii = shape.length - 1; ii >= 0; ii--) {
      int thisDim = shape[ii];
      strides[ii] = product;
      product *= thisDim;
    }
    return strides;
  }

  public int calcIndex(int... index) {
    assert index.length == shape.length;
    int result = 0;
    for (int ii = 0; ii < index.length; ii++)
      result += index[ii] * stride[ii];
    return result;
  }

  public T getContent(int idx) {
    if (idx > track.length || idx < 0)
      logger.error("BAD index get="+ idx+" max= "+track.length, new Throwable());
    int contentIdx = track[idx]-1;
    if (contentIdx < 0)
      return null; // missing
    return content.get(contentIdx);
  }

  public T getContent(int[] index) {
    int where = calcIndex(index);
    return getContent(where);
  }

  public int[] getShape() {
    return shape.clone();
  }

  public int getRank() {
    return shape.length;
  }

  public int getTotalSize() {
    return totalSize;
  }

  public int[] getTrack() {
    return track;
  }

  public int getTrack(int idx) {
    return track[idx];
  }

  public List<T> getContent() {
    return content;
  }

  public int countNotMissing() { // LOOK could use content.size()
     int result=0;
     for (int idx : track)
       if (idx > 0) result++;
     return result;
   }

   public int countMissing() {
     int result=0;
     for (int idx : track)
       if (idx == 0) result++;
     return result;
   }

  public float getDensity() {
    return (float) countNotMissing() / totalSize;
  }

  public int getNdups() {
    return ndups;
  }

  public void showInfo(Formatter info, GribRecordStats all) {
    info.format("SparseArray shape=[%s] ", Misc.showInts(shape));
    info.format("ndups=%d, missing/total=%d/%d, density=%f%n", ndups, countMissing(), totalSize, getDensity());

    if (all != null) {
      all.dups += ndups;
      all.recordsUnique += countNotMissing();
      all.recordsTotal += totalSize;
      all.vars++;
    }

    List<Integer> sizes = new ArrayList<>();
    for (int s : shape) {
      if (s == 1) continue; // skip dimension len 1
      sizes.add(s);
    }
    info.format("%n");
    showMissingRecurse(0, sizes, info);
  }

  int showMissingRecurse(int offset, List<Integer> sizes, Formatter f) {
    if (sizes.size() == 0) return 0;
    if (sizes.size() == 1) {
      int len = sizes.get(0);
      for (int i=0; i<len; i++) {
        boolean hasRecord = track[offset+i] > 0;
        if (hasRecord) f.format("X"); else f.format("-");
      }
      f.format("%n");
      return len;

    } else {
      int total = 0;
      int len = sizes.get(0);
      for (int i=0; i<len; i++) {
        int count = showMissingRecurse(offset, sizes.subList(1,sizes.size()), f);
        offset += count;
        total += count;
      }
      f.format("%n");
      return total;
    }

  }

  public void showContent(Formatter f) {
    int count = 0;
    f.format("Content%n");
    for (T record : content)
      f.format(" %d %s %n", count++, record);
  }

  ////////////////////////////////////////////////////////////////////////////////////

  // separate out the mutable part
  public static class Builder<T> {
    private int[] shape;    // multidim sizes
    private int[] stride;  // for index calculation
    private int totalSize; // product of sizes
    private int ndups = 0; // number of duplicates

    private int[] track; // index into content, size totalSize. LOOK use byte, short to save memory ??
    private List<T> content; // keep the things in a List.

    public Builder( int... shape) {
      this.shape = shape;
      this.totalSize = calcTotalSize(shape);
      this.stride = calcStrides(shape);

      track = new int[totalSize];
      this.content = new ArrayList<>(totalSize);  // LOOK could only allocate part of this
    }

    public void add(T thing, Formatter info, int... index) {
      content.add(thing);            // add the thing at end of list, idx = size-1
      int where = calcIndex(index);
      if (where < 0 || where >= track.length) {
        logger.error("BAD index add=" + Misc.showInts(index), new Throwable());
      }
      if (track[where] > 0) {
        ndups++;
        if (info != null) info.format(" duplicate %s%n     with %s%n%n", thing, content.get(track[where] - 1));
      }
      track[where] = content.size();  // 1-based so that 0 = missing, so content at where = content.get(track[where]-1)
    }

    public int calcIndex(int... index) {
      assert index.length == shape.length;
      int result = 0;
      for (int ii = 0; ii < index.length; ii++)
        result += index[ii] * stride[ii];
      return result;
    }

    public void setTrack(int[] track) {
      this.track = track;
    }

    public void setContent(List<T> content) {
      this.content = content;
    }

    public int getTotalSize() {
      return totalSize;
    }

    SparseArray<T> finish() {
       return new SparseArray<>( shape, track, content, ndups);
    }
  }


}
