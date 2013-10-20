package thredds.server.ncSubset.view.gridaspoint;

import thredds.server.ncSubset.format.SupportedFormat;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.util.DiskCache2;

import java.io.OutputStream;

/**
 * Describe
 *
 * @author caron
 * @since 10/5/13
 */
public class PointDataWriterFactory {

  static public PointDataWriter factory(SupportedFormat wantFormat, OutputStream outputStream, DiskCache2 diskCache) {

 		if( wantFormat ==  SupportedFormat.XML_FILE || wantFormat ==  SupportedFormat.XML_STREAM){
 		  return XMLPointDataWriter.factory(outputStream);
 		}

    if( wantFormat ==  SupportedFormat.NETCDF3){
      return NetCDFPointDataWriter.factory(NetcdfFileWriter.Version.netcdf3, outputStream, diskCache);
 		}

    if( wantFormat ==  SupportedFormat.NETCDF4){
      return NetCDFPointDataWriter.factory(NetcdfFileWriter.Version.netcdf4, outputStream, diskCache);
 		}

    if( wantFormat ==  SupportedFormat.CSV_FILE ||  wantFormat ==  SupportedFormat.CSV_STREAM){
      return CSVPointDataWriter.factory(outputStream);
 		}

 		throw new IllegalStateException("PointDataWriter does not support "+ wantFormat);

 	}
}
