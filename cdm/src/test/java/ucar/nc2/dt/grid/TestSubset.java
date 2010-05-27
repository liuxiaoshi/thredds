/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.dt.grid;

import junit.framework.TestCase;
import ucar.ma2.*;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.NCdump;
import ucar.nc2.TestAll;
import ucar.nc2.thredds.ThreddsDataFactory;
import ucar.unidata.geoloc.*;
import ucar.unidata.geoloc.projection.LatLonProjection;
import ucar.unidata.geoloc.vertical.VerticalTransform;

import ucar.nc2.constants.FeatureType;

import java.util.Arrays;

public class TestSubset extends TestCase {

  public TestSubset(String name) {
    super(name);
  }

  public void testRegular() throws Exception {
    ucar.nc2.dt.grid.GridDataset dataset = GridDataset.open(TestAll.cdmUnitTestDir + "grid/03061219_ruc.nc");

    GeoGrid grid = dataset.findGridByName("T");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    CoordinateAxis zaxis = gcs.getVerticalAxis();
    assert zaxis.getUnitsString().equals("hectopascals");

    GeoGrid grid_section = grid.subset(null, null, null, 3, 3, 3);

    GridCoordSystem gcs_section = grid_section.getCoordinateSystem();
    CoordinateAxis zaxis2 = gcs_section.getVerticalAxis();
    assert zaxis2.getSize() == 7;
    assert zaxis2.getUnitsString().equals("hectopascals");
    assert gcs_section.getTimeAxis().equals(gcs.getTimeAxis());

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 2 : data.getShape()[0];
    assert data.getShape()[1] == 7 : data.getShape()[1];
    assert data.getShape()[2] == 22 : data.getShape()[2];
    assert data.getShape()[3] == 31 : data.getShape()[3];

    // check axes
    for (CoordinateAxis axis : gcs_section.getCoordinateAxes()) {
      assert axis.getAxisType() != null;
    }

    // NCdump.printArray( data, "grid_section", System.out,  null);
    dataset.close();
  }

  public void testGrib() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "grid/grib/grib1/data/AVN.wmo");

    GeoGrid grid = dataset.findGridByName("Temperature");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    GeoGrid grid_section = grid.subset(null, null, null, 3, 3, 3);

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 3 : data.getShape()[0];
    assert data.getShape()[1] == 3 : data.getShape()[1];
    assert data.getShape()[2] == 13 : data.getShape()[2];
    assert data.getShape()[3] == 15 : data.getShape()[3];

    dataset.close();
  }

  public void testWRF() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "grid/netcdf/wrf/wrfout_v2_Lambert.nc");

    GeoGrid grid = dataset.findGridByName("T");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    CoordinateAxis zaxis = gcs.getVerticalAxis();
    assert zaxis.getSize() == 27;

    VerticalTransform vt = gcs.getVerticalTransform();
    assert vt != null;
    assert vt.getUnitString().equals("Pa");

    GeoGrid grid_section = grid.subset(null, null, null, 3, 3, 3);

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 13 : data.getShape()[0];
    assert data.getShape()[1] == 9 : data.getShape()[1];
    assert data.getShape()[2] == 20 : data.getShape()[2];
    assert data.getShape()[3] == 25 : data.getShape()[3];

    GridCoordSystem gcs_section = grid_section.getCoordinateSystem();
    CoordinateAxis zaxis2 = gcs_section.getVerticalAxis();
    assert zaxis2.getSize() == 9 : zaxis2.getSize();

    assert zaxis2.getUnitsString().equals(zaxis.getUnitsString());
    assert gcs_section.getTimeAxis().equals(gcs.getTimeAxis());

    VerticalTransform vt_section = gcs_section.getVerticalTransform();
    assert vt_section != null;
    assert vt_section.getUnitString().equals(vt.getUnitString());

    dataset.close();
  }

  public void testMSG() throws Exception {
    String filename = TestAll.cdmUnitTestDir + "transforms/Eumetsat.VerticalPerspective.grb";
    GridDataset dataset = GridDataset.open(filename);
    System.out.printf("open %s%n", filename);

    GeoGrid grid = dataset.findGridByName("Pixel_scene_type");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 3;

    // bbox =  ll: 16.79S 20.5W+ ur: 14.1N 20.09E
    LatLonRect bbox = new LatLonRect(new LatLonPointImpl(-16.79, -20.5), new LatLonPointImpl(14.1, 20.9));

    ProjectionImpl p = gcs.getProjection();
    ProjectionRect prect = p.latLonToProjBB(bbox); // must override default implementation
    System.out.printf("%s -> %s %n", bbox, prect);
    assert TestAll.closeEnough(prect.getMinX(), -2120.3375);
    assert TestAll.closeEnough(prect.getWidth(), 4279.2196);
    assert TestAll.closeEnough(prect.getMinY(), -1809.0359);
    assert TestAll.closeEnough(prect.getHeight(), 3338.2484);

    LatLonRect bb2 = p.projToLatLonBB(prect);
    System.out.printf("%s -> %s %n", prect, bb2);

    GeoGrid grid_section = grid.subset(null, null, bbox, 1, 1, 1);

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getRank() == 3;
    int[] shape = data.getShape();
    assert shape[0] == 1 : data.getShape()[0];
    assert shape[1] == 370 : data.getShape()[1];
    assert shape[2] == 476 : data.getShape()[2];

    dataset.close();
  }

  public void testDODS() throws Exception {
    String ds = "http://motherlode.ucar.edu:8080/thredds/catalog/fmrc/NCEP/DGEX/CONUS_12km/files/latest.xml";
    //String dsid = "#NCEP/DGEX/CONUS_12km/latest.xml";
    ThreddsDataFactory.Result result = new ThreddsDataFactory().openFeatureDataset("thredds:resolve:" + ds, null);
    System.out.println("result errlog= " + result.errLog);
    assert !result.fatalError;
    assert result.featureType == FeatureType.GRID;
    assert result.featureDataset != null;

    GridDataset dataset = (GridDataset) result.featureDataset;

    GeoGrid grid = dataset.findGridByName("Temperature");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    GeoGrid grid_section = grid.subset(null, null, null, 3, 3, 3);
    int[] shape = grid_section.getShape();
    System.out.println("grid_section.getShape= " + Range.toString(Range.factory(shape)));

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == shape[0] : data.getShape()[0];
    assert data.getShape()[1] == shape[1] : data.getShape()[1];
    assert data.getShape()[2] == 101 : data.getShape()[2];
    assert data.getShape()[3] == 164 : data.getShape()[3];

    // NCdump.printArray( data, "grid_section", System.out,  null);
    dataset.close();
  }

  public void utestDODS2() throws Exception {
    String threddsURL = "http://lead.unidata.ucar.edu:8080/thredds/dqcServlet/latestOUADAS?adas";
    ThreddsDataFactory.Result result = new ThreddsDataFactory().openFeatureDataset(threddsURL, null);
    assert result.featureDataset != null;
    GridDataset dataset = (GridDataset) result.featureDataset;

    GeoGrid grid = dataset.findGridByName("PT");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    GeoGrid grid_section = grid.subset(null, null, null, 5, 5, 5);

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 11 : data.getShape()[1];
    assert data.getShape()[2] == 26 : data.getShape()[2];
    assert data.getShape()[3] == 43 : data.getShape()[3];

    grid_section = grid.subset(null, new Range(0, 0), null, 0, 2, 2);
    data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 1 : data.getShape()[1];
    assert data.getShape()[2] == 65 : data.getShape()[2];
    assert data.getShape()[3] == 106 : data.getShape()[3];

    NCdump.printArray(data, "grid_section", System.out, null);
    dataset.close();
  }

  public void test2D() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "grid/netcdf/cf/mississippi.nc");

    GeoGrid grid = dataset.findGridByName("salt");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    GeoGrid grid_section = grid.subset(null, null, null, 5, 5, 5);

    Array data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 4 : data.getShape()[1];
    assert data.getShape()[2] == 13 : data.getShape()[2];
    assert data.getShape()[3] == 26 : data.getShape()[3];

    grid_section = grid.subset(null, new Range(0, 0), null, 0, 2, 2);
    data = grid_section.readDataSlice(-1, -1, -1, -1);
    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 1 : data.getShape()[1];
    assert data.getShape()[2] == 32 : data.getShape()[2];
    assert data.getShape()[3] == 64 : data.getShape()[3];

    NCdump.printArray(data, "grid_section", System.out, null);

    LatLonPoint p0 = new LatLonPointImpl(29.0, -90.0);
    LatLonRect bbox = new LatLonRect(p0, 1.0, 2.0);
    grid_section = grid.subset(null, null, bbox, 1, 1, 1);
    data = grid_section.readDataSlice(-1, -1, -1, -1);

    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 20 : data.getShape()[1];
    assert data.getShape()[2] == 63 : data.getShape()[2];
    assert data.getShape()[3] == 53 : data.getShape()[3];

    gcs = grid_section.getCoordinateSystem();
    ProjectionRect rect = gcs.getBoundingBox();
    System.out.println(" rect= " + rect);

    p0 = new LatLonPointImpl(30.0, -90.0);
    bbox = new LatLonRect(p0, 1.0, 2.0);
    grid_section = grid.subset(null, null, bbox, 1, 1, 1);
    data = grid_section.readDataSlice(-1, -1, -1, -1);

    assert data.getShape()[0] == 1 : data.getShape()[0];
    assert data.getShape()[1] == 20 : data.getShape()[1];
    assert data.getShape()[2] == 18 : data.getShape()[2];
    assert data.getShape()[3] == 17 : data.getShape()[3];

    gcs = grid_section.getCoordinateSystem();
    System.out.println(" rect= " + gcs.getBoundingBox());


    dataset.close();
  }

  public void test3D() throws Exception {
    // GridDataset dataset = GridDataset.open("thredds:resolve:http://motherlode.ucar.edu:8080/thredds/dodsC/model/NCEP/NAM/CONUS_12km/latest.xml");
    GridDataset dataset = GridDataset.open("dods://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/NAM/CONUS_12km/NCEP-NAM-CONUS_12km_best.ncd");

    GeoGrid grid = dataset.findGridByName("Relative_humidity");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 4;

    GeoGrid grid_section = grid.subset(null, null, null, 1, 10, 10);

    Array data = grid_section.readDataSlice(0, -1, -1, -1);
    assert data.getRank() == 3;
    // assert data.getShape()[0] == 6 : data.getShape()[0];
    assert data.getShape()[1] == 43 : data.getShape()[1];
    assert data.getShape()[2] == 62 : data.getShape()[2];

    IndexIterator ii = data.getIndexIterator();
    while (ii.hasNext()) {
      float val = ii.getFloatNext();
      if (grid_section.isMissingData(val)) {
        if (!Float.isNaN(val)) {
          System.out.println(" got not NaN at =" + ii);
        }
        int[] current = ii.getCurrentCounter();
        if ((current[1] > 0) && (current[2] > 1)) {
          System.out.println(" got missing at =" + ii);
          System.out.println(current[1] + " " + current[2]);
        }
      }
    }

    dataset.close();
  }

  private void testLatLonSubset(GeoGrid grid, LatLonRect bbox, int[] shape) throws Exception {
    //LatLonProjection llproj = new LatLonProjection();
    //ucar.unidata.geoloc.ProjectionRect[] prect = llproj.latLonToProjRect(bbox);
    System.out.println("\n     grid bbox= " + grid.getCoordinateSystem().getLatLonBoundingBox().toString2());
    System.out.println(" constrain bbox= " + bbox.toString2());

    GeoGrid grid_section = grid.subset(null, null, bbox, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;
    assert grid_section.getRank() == 2;

    //ucar.unidata.geoloc.ProjectionRect subset_prect = gcs2.getBoundingBox();
    System.out.println(" resulting bbox= " + gcs2.getLatLonBoundingBox().toString2());

    Array data = grid_section.readDataSlice(0, 0, -1, -1);
    assert data != null;
    assert data.getRank() == 2;
    assert data.getShape()[0] == shape[0] : data.getShape()[0];
    assert data.getShape()[1] == shape[1] : data.getShape()[1];
  }


  public void testLatLonSubset() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "grid/netcdf/cf/SUPER-NATIONAL_latlon_IR_20070222_1600.nc");
    //GridDataset dataset = GridDataset.open("dods://motherlode.ucar.edu:8080/thredds/dodsC/model/NCEP/NAM/CONUS_12km/NAM_CONUS_12km_20060305_1200.grib2");
    // GridDataset dataset = GridDataset.open(TestAll.cdmUnitTestDir + "grid/grib/grib2/test/NAM_CONUS_12km_20060305_1200.grib2");
    GeoGrid grid = dataset.findGridByName("micron11");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 2;

    System.out.println("original bbox= " + gcs.getBoundingBox());

    Array data = null;

    LatLonRect bbox = new LatLonRect(new LatLonPointImpl(40.0, -100.0), 10.0, 20.0);
    testLatLonSubset(grid, bbox, new int[]{141, 281});

    bbox = new LatLonRect(new LatLonPointImpl(-40.0, -180.0), 120.0, 300.0);
    testLatLonSubset(grid, bbox, new int[]{800, 1300});


    dataset.close();
  }

  // longitude subsetting (CoordAxis1D regular)
  public void testLatLonSubset2() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "/grid/grib/grib2/data/GFS_Global_onedeg_20090105_0600.grib2");
    GeoGrid grid = dataset.findGridByName("Pressure_surface");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 3 : grid.getRank();

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox = " + gcs.getLatLonBoundingBox().toString2());

    LatLonRect bbox = new LatLonRect(new LatLonPointImpl(40.0, -100.0), 10.0, 20.0);
    System.out.println(" constrain bbox= " + bbox.toString2());

    GeoGrid grid_section = grid.subset(null, null, bbox, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;
    assert grid_section.getRank() == grid.getRank();

    //ucar.unidata.geoloc.ProjectionRect subset_prect = gcs2.getBoundingBox();
    System.out.println(" resulting bbox= " + gcs2.getLatLonBoundingBox().toString2());

    Array data = grid_section.readDataSlice(0, 0, -1, -1);
    assert data != null;
    assert data.getRank() == 2;

    int[] dataShape = data.getShape();
    assert dataShape.length == 2;
    assert dataShape[0] == 11 : data.getShape()[0];
    assert dataShape[1] == 21 : data.getShape()[1];

    dataset.close();
  }

  public void testGiniSubsetStride() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "satellite/gini/WEST-CONUS_4km_IR_20070216_1500.gini");
    GeoGrid grid = dataset.findGridByName("IR");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 3;
    int[] org_shape = grid.getShape();

    Array data_org = grid.readDataSlice(0, 0, -1, -1);
    assert data_org != null;
    assert data_org.getRank() == 2;
    int[] data_shape = data_org.getShape();
    assert org_shape[1] == data_shape[0];
    assert org_shape[2] == data_shape[1];

    System.out.println("original bbox= " + gcs.getBoundingBox());

    LatLonRect bbox = new LatLonRect(new LatLonPointImpl(40.0, -100.0), 10.0, 20.0);

    LatLonProjection llproj = new LatLonProjection();
    ucar.unidata.geoloc.ProjectionRect[] prect = llproj.latLonToProjRect(bbox);
    System.out.println("\n constrain bbox= " + prect[0]);

    GeoGrid grid_section = grid.subset(null, null, bbox, 1, 2, 3);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;
    assert grid_section.getRank() == 3;

    ucar.unidata.geoloc.ProjectionRect subset_prect = gcs2.getBoundingBox();
    System.out.println(" resulting bbox= " + subset_prect);

    // test stride
    grid_section = grid.subset(null, null, null, 1, 2, 3);
    Array data = grid_section.readDataSlice(0, 0, -1, -1);
    assert data != null;
    assert data.getRank() == 2;

    int[] shape = data.getShape();
    assert Math.abs(org_shape[1] - 2 * shape[0]) < 2 : org_shape[2] + " != " + (2 * shape[0]);
    assert Math.abs(org_shape[2] - 3 * shape[1]) < 3 : org_shape[2] + " != " + (3 * shape[1]);
    dataset.close();
  }

  public void testBBSubset() throws Exception {
    GridDataset dataset = GridDataset.open("dods://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/GFS/CONUS_80km/NCEP-GFS-CONUS_80km_best.ncd");
    GeoGrid grid = dataset.findGridByName("Pressure");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    ucar.unidata.geoloc.LatLonRect llbb = gcs.getLatLonBoundingBox();
    ucar.unidata.geoloc.LatLonRect llbb_subset = new LatLonRect(llbb.getLowerLeftPoint(), 20.0, llbb.getWidth() / 2);
    System.out.println("subset lat/lon bbox= " + llbb_subset);

    GeoGrid grid_section = grid.subset(null, null, llbb_subset, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    ProjectionRect pr = gcs2.getProjection().getDefaultMapArea();
    System.out.println("projection mapArea= " + pr);
    assert (pr.equals(gcs2.getBoundingBox()));

    dataset.close();
  }

  public void testBBSubset2() throws Exception {
    GridDataset dataset = GridDataset.open("dods://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/NAM/CONUS_40km/conduit/NCEP-NAM-CONUS_40km-conduit_best.ncd");
    GeoGrid grid = dataset.findGridByName("Pressure");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    ucar.unidata.geoloc.LatLonRect llbb = gcs.getLatLonBoundingBox();
    ucar.unidata.geoloc.LatLonRect llbb_subset = new LatLonRect(new LatLonPointImpl(-15, -140), new LatLonPointImpl(55, 30));
    System.out.println("subset lat/lon bbox= " + llbb_subset);

    GeoGrid grid_section = grid.subset(null, null, llbb_subset, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    ProjectionRect pr = gcs2.getProjection().getDefaultMapArea();
    System.out.println("projection mapArea= " + pr);
    assert (pr.equals(gcs2.getBoundingBox()));

    dataset.close();
  }

  public void utestFMRCSubset() throws Exception {
    GridDataset dataset = GridDataset.open("dods://localhost:8080/thredds/dodsC/data/cip/fmrcCase1/CIPFMRCCase1_best.ncd");
    GeoGrid grid = dataset.findGridByName("Latitude__90_to_+90");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    Range timeRange = new Range(2, 2);
    int bestZIndex = 5;

    GeoGrid subset = grid.subset(timeRange, new Range(bestZIndex, bestZIndex), null, null);
    Array yxData = subset.readYXData(0, 0);
    NCdump.printArray(yxData, "xyData", System.out, null);

    dataset.close();
  }

  public void testVerticalAxis() throws Exception {
    //String uri="dods://www.gri.msstate.edu/rsearch_data/nopp/bora_feb.nc";
    //String varName = "temp";

    String uri = TestAll.cdmUnitTestDir + "ncml/nc/cg/CG2006158_120000h_usfc.nc";
    String varName = "CGusfc";

    GridDataset dataset = GridDataset.open(uri);
    GeoGrid grid = dataset.findGridByName(varName);
    assert null != grid;

    GridCoordSystem gcsi = grid.getCoordinateSystem();
    assert null != gcsi;
    assert (gcsi.getVerticalAxis() != null);

    GridCoordSys gcs = (GridCoordSys) grid.getCoordinateSystem();
    assert null != gcs;
    assert gcs.hasVerticalAxis();          // returns true.

    // subset geogrid
    GeoGrid subg = grid.subset(null, null, null, 1, 1, 1);
    assert null != subg;

    GridCoordSystem gcsi2 = subg.getCoordinateSystem();
    assert null != gcsi2;
    assert (gcsi2.getVerticalAxis() != null);

    GridCoordSys gcs2 = (GridCoordSys) subg.getCoordinateSystem();
    assert null != gcs2;
    assert !gcs2.hasVerticalAxis();          // fails

    dataset.close();
  }

  public void testBBSubsetVP() throws Exception {
    String filename = TestAll.testdataDir + "cdmUnitTest/transforms/Eumetsat.VerticalPerspective.grb";
    GridDataset dataset = GridDataset.open(filename);
    GeoGrid grid = dataset.findGridByName("Pixel_scene_type");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    ucar.unidata.geoloc.LatLonRect llbb_subset = new LatLonRect(new LatLonPointImpl(), 20.0, 40.0);
    System.out.println("subset lat/lon bbox= " + llbb_subset);

    GeoGrid grid_section = grid.subset(null, null, llbb_subset, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    ProjectionRect pr = gcs2.getProjection().getDefaultMapArea();
    System.out.println("projection mapArea= " + pr);
    assert (pr.equals(gcs2.getBoundingBox()));

    dataset.close();
  }

  // x,y in meters
  public void testBBSubsetUnits() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.cdmUnitTestDir + "ncml/testBBSubsetUnits.ncml");
    System.out.println("file= " + dataset.getLocation());

    GeoGrid grid = dataset.findGridByName("pr");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    ucar.unidata.geoloc.LatLonRect llbb_subset = new LatLonRect(new LatLonPointImpl(38, -110), new LatLonPointImpl(42, -90));
    System.out.println("subset lat/lon bbox= " + llbb_subset);

    GeoGrid grid_section = grid.subset(null, null, llbb_subset, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    ProjectionRect pr = gcs2.getProjection().getDefaultMapArea();
    System.out.println("projection mapArea= " + pr);
    assert (pr.equals(gcs2.getBoundingBox()));

    CoordinateAxis xaxis = gcs.getXHorizAxis();
    CoordinateAxis yaxis = gcs.getYHorizAxis();
    System.out.println("(nx,ny)= " + xaxis.getSize() + "," + yaxis.getSize());

    dataset.close();
  }

  public void testAggByteGiniSubsetStride() throws Exception {
    GridDataset dataset = GridDataset.open(TestAll.testdataDir + "satellite/gini/giniAggByte.ncml"); // R:\testdata\satellite\gini
    GeoGrid grid = dataset.findGridByName("IR");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;
    assert grid.getRank() == 3;
    int[] org_shape = grid.getShape();
    assert grid.getDataType() == DataType.SHORT;

    Array data_org = grid.readDataSlice(0, 0, -1, -1);
    assert data_org != null;
    assert data_org.getRank() == 2;
    int[] data_shape = data_org.getShape();
    assert org_shape[1] == data_shape[0];
    assert org_shape[2] == data_shape[1];
    assert data_org.getElementType() == short.class : data_org.getElementType();

    System.out.println("original bbox= " + gcs.getBoundingBox());

    LatLonRect bbox = new LatLonRect(new LatLonPointImpl(40.0, -100.0), 10.0, 20.0);

    LatLonProjection llproj = new LatLonProjection();
    ucar.unidata.geoloc.ProjectionRect[] prect = llproj.latLonToProjRect(bbox);
    System.out.println("\n constrain bbox= " + prect[0]);

    GeoGrid grid_section = grid.subset(null, null, bbox, 1, 2, 3);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;
    assert grid_section.getRank() == 3;
    assert grid_section.getDataType() == DataType.SHORT;

    ucar.unidata.geoloc.ProjectionRect subset_prect = gcs2.getBoundingBox();
    System.out.println(" resulting bbox= " + subset_prect);

    // test stride
    grid_section = grid.subset(null, null, null, 2, 2, 3);
    Array data = grid_section.readVolumeData(1);
    assert data != null;
    assert data.getRank() == 2;
    assert data.getElementType() == short.class;

    int[] shape = data.getShape();
    assert Math.abs(org_shape[1] - 2 * shape[0]) < 2 : org_shape[2] + " != " + (2 * shape[0]);
    assert Math.abs(org_shape[2] - 3 * shape[1]) < 3 : org_shape[2] + " != " + (3 * shape[1]);
    dataset.close();
  }

  public void utestBBSubsetVP2() throws Exception {
    String filename = "C:/Documents and Settings/caron/My Documents/downloads/MSG2-SEVI-MSGCLAI-0000-0000-20070522114500.000000000Z-582760.grb";
    GridDataset dataset = GridDataset.open(filename);
    GeoGrid grid = dataset.findGridByName("Pixel_scene_type");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    ucar.unidata.geoloc.LatLonRect llbb_subset = new LatLonRect(new LatLonPointImpl(), 20.0, 40.0);
    System.out.println("subset lat/lon bbox= " + llbb_subset);

    GeoGrid grid_section = grid.subset(null, null, llbb_subset, 1, 1, 1);
    GridCoordSystem gcs2 = grid_section.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    ProjectionRect pr = gcs2.getProjection().getDefaultMapArea();
    System.out.println("projection mapArea= " + pr);
    assert (pr.equals(gcs2.getBoundingBox()));

    dataset.close();
  }

  public void utestNcmlRangeSubset() throws Exception {
    String filename = "D:/test/ncom_agg6.ncml";
    GridDataset dataset = GridDataset.open(filename);
    GeoGrid grid = dataset.findGridByName("water_temp");
    assert null != grid;
    GridCoordSystem gcs = grid.getCoordinateSystem();
    assert null != gcs;

    System.out.println("original bbox= " + gcs.getBoundingBox());
    System.out.println("lat/lon bbox= " + gcs.getLatLonBoundingBox());

    /*   tRange: 31:31
     zRange: 0:0
    yRange: 1:559
    zRange: 1:399 */
    GridDatatype subset = grid.makeSubset(null, null, new Range(31, 31), new Range(0, 0), new Range(1, 559), new Range(1, 399));
    assert subset != null;
    GridCoordSystem gcs2 = subset.getCoordinateSystem();
    assert null != gcs2;

    System.out.println("result lat/lon bbox= " + gcs2.getLatLonBoundingBox());
    System.out.println("result bbox= " + gcs2.getBoundingBox());

    Array data = subset.readVolumeData(0);
    int[] shape = data.getShape();
    assert shape.length == 3;
    assert shape[0] == 1;
    assert shape[1] == 559;
    assert shape[2] == 399;


    dataset.close();
  }

  public void testProblem() throws Exception {
    String filename = "dods://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/NAM/Alaska_11km/NCEP-NAM-Alaska_11km_best.ncd";
    GridDataset dataset = GridDataset.open(filename);
    GeoGrid grid = dataset.findGridByName("Geopotential_height");
    assert null != grid;

    GridCoordSystem gcs = grid.getCoordinateSystem();
    CoordinateAxis1D zaxis = gcs.getVerticalAxis();
    float zCoord = 10000;
    int zidx = zaxis.findCoordElement(zCoord);
    assert zidx == 28 : zidx;

    dataset.close();
  }

  public void utestSubsetCoordEdges() throws Exception {
    NetcdfDataset fooDataset = NetcdfDataset.openDataset(TestAll.cdmLocalTestDataDir + "dataset/subsetCoordEdges.ncml");

    try {
      GridDataset fooGridDataset = new GridDataset(fooDataset);
      GridDatatype fooGrid = fooGridDataset.findGridDatatype("foo");

      CoordinateAxis1D fooTimeAxis = fooGrid.getCoordinateSystem().getTimeAxis1D();
      CoordinateAxis1D fooLatAxis = (CoordinateAxis1D) fooGrid.getCoordinateSystem().getYHorizAxis();
      CoordinateAxis1D fooLonAxis = (CoordinateAxis1D) fooGrid.getCoordinateSystem().getXHorizAxis();

      // Expected: [0.0, 31.0, 59.0, 90.0, 120.0]
      // Actual:   [0.0, 31.0, 59.0, 90.0, 120.0]
      System.out.println("mid time= " + Arrays.toString(fooTimeAxis.getCoordValues()));
      System.out.println("edge time= " + Arrays.toString(fooTimeAxis.getCoordEdges()));
      System.out.println();

      // Expected: [-90.0, -18.0, 36.0, 72.0, 90.0]
      // Actual:   [-90.0, -18.0, 36.0, 72.0, 90.0]
      System.out.println("mid lat= " + Arrays.toString(fooLatAxis.getCoordValues()));
      System.out.println("edge lat= " + Arrays.toString(fooLatAxis.getCoordEdges()));
      System.out.println();

      // Expected: [0.0, 36.0, 108.0, 216.0, 360.0]
      // Actual:   [0.0, 36.0, 108.0, 216.0, 360.0]
      System.out.println("mid lon= " + Arrays.toString(fooLonAxis.getCoordValues()));
      System.out.println("edge lon= " + Arrays.toString(fooLonAxis.getCoordEdges()));
      System.out.println();


      Range middleRange = new Range(1, 2);
      GridDatatype fooSubGrid = fooGrid.makeSubset(null, null, middleRange, null, middleRange, middleRange);

      CoordinateAxis1D fooSubTimeAxis = fooSubGrid.getCoordinateSystem().getTimeAxis1D();
      CoordinateAxis1D fooSubLatAxis = (CoordinateAxis1D) fooSubGrid.getCoordinateSystem().getYHorizAxis();
      CoordinateAxis1D fooSubLonAxis = (CoordinateAxis1D) fooSubGrid.getCoordinateSystem().getXHorizAxis();

      // Expected: [31.0, 59.0, 90.0]
      // Actual:   [30.25, 59.75, 89.25]
      System.out.println("mid time= " + Arrays.toString(fooSubTimeAxis.getCoordValues()));
      System.out.println("edge time= " + Arrays.toString(fooSubTimeAxis.getCoordEdges()));
      compare(fooSubTimeAxis.getCoordEdges(), new double[] {31.0, 59.0, 90.0} );
      System.out.println();

      // Expected: [-18.0, 36.0, 72.0]
      // Actual:   [-13.5, 31.5, 76.5]
      System.out.println("mid lat= " + Arrays.toString(fooSubLatAxis.getCoordValues()));
      System.out.println("edge lat= " + Arrays.toString(fooSubLatAxis.getCoordEdges()));
      compare(fooSubLatAxis.getCoordEdges(), new double[] {-18.0, 36.0, 72.0} );
      System.out.println();

      // Expected: [36.0, 108.0, 216.0]
      // Actual:   [27.0, 117.0, 207.0]
      System.out.println("mid lon= " + Arrays.toString(fooSubLonAxis.getCoordValues()));
      System.out.println("edge lon= " + Arrays.toString(fooSubLonAxis.getCoordEdges()));
      compare(fooSubLonAxis.getCoordEdges(), new double[] {36.0, 108.0, 216.0} );
      System.out.println();

    } finally {
      fooDataset.close();
    }
  }

  private void compare(double[] d1, double[] d2) {
    System.out.println(" should be= " + Arrays.toString(d2));
    assert d1.length == d2.length;
    for (int i=0; i<d1.length; i++)
      assert d1[i] == d2[i];
  }
}

