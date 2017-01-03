package org.seamcat.tabulardataio;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataItem;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.functions.Point3D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.BarChartValue;
import org.seamcat.presentation.DialogDisplaySignalHelper;
import org.seamcat.presentation.propagationtest.PropagationHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDataIO {

    private File file;

    public void setFile(File _file) {
        file = _file;
    }

    public File getFile() {
        return file;
    }

    public void savePropogationHolders(DataResultType type, List<PropagationHolder> propagationHolders) {
        TabularDataSaver dataSaver = TabularDataFactory.newSaverForFile(file);

        int length = 0;
        String[] titles;
        Double[] row;
        if ( type == DataResultType.vector ) {
            titles = new String[propagationHolders.size()];
            row = new Double[propagationHolders.size()];
        } else if ( type == DataResultType.cdf || type == DataResultType.graph || type == DataResultType.pdf ) {
            titles = new String[propagationHolders.size()*2];
            row = new Double[propagationHolders.size()*2];
        } else {
            titles = new String[propagationHolders.size()];
            row = new Double[propagationHolders.size()];
        }

        for (int i = 0; i < propagationHolders.size(); i++) {
            PropagationHolder holder = propagationHolders.get(i);
            length = Math.max(length, holder.getData().length);
            if ( type == DataResultType.vector ) {
                titles[i] = holder.getTitle();
            } else if ( type == DataResultType.cdf) {
                double mean = Mathematics.getAverage(holder.getData());
                titles[2*i] = holder.getTitle();
                titles[2*i + 1] = "Cumulative";
            } else if ( type == DataResultType.graph ) {
                titles[2*i] = holder.getSortedTitle();
                titles[2*i + 1 ] = holder.getTitle();
            } else if ( type == DataResultType.pdf ) {
                titles[2*i] = "Bin";
                titles[2*i +1] = "Probability";
            }
        }
        dataSaver.addRow( titles );
        HistogramDataset data = null;
        if (type == DataResultType.pdf ) {
            int bins = propagationHolders.get(0).binCount;
            data = DialogDisplaySignalHelper.getDensityHistogram(propagationHolders, bins);
            length = bins;
        }


        for (int j=0; j<length; j++) {
            for (int i = 0; i < propagationHolders.size(); i++) {
                PropagationHolder holder = propagationHolders.get(i);
                if ( type == DataResultType.vector ) {
                    if ( holder.getData().length < j ) {
                        row[i] = 0.0;
                    } else {
                        row[i] = holder.getData()[j];
                    }
                } else if ( type == DataResultType.cdf ) {
                    if ( holder.getData().length < j) {
                        row[2*i] = 0.0;
                        row[2*i + 1] = 0.0;
                    } else {
                        XYDataItem xy = holder.getCumulativeDataSeries().getDataItem(j);
                        row[2*i] = xy.getX().doubleValue();
                        row[2*i + 1] = xy.getY().doubleValue();
                    }
                } else if ( type == DataResultType.graph ) {
                    if ( holder.getData().length < j) {
                        row[2*i] = 0.0;
                        row[2*i + 1] = 0.0;
                    } else {
                        row[2*i] = holder.getSortedDistributions()[j];
                        row[2*i + 1] = holder.getData()[j];
                    }
                } else if ( type == DataResultType.pdf) {
                    row[2*i] = data.getX(i, j).doubleValue();
                    row[2*i +1] = data.getY(i, j).doubleValue();
                }
            }
            dataSaver.addRow(row);
        }
        dataSaver.close();
    }

    public void savePoints( List<Point2D> points ) {
        TabularDataSaver dataSaver = TabularDataFactory.newSaverForFile(file);
        for (Point2D point : points) {
            if ( point instanceof Point3D) {
                dataSaver.addRow( point.getX(), point.getY(), ((Point3D) point).getRZ() );
            } else {
                dataSaver.addRow( point.getX(), point.getY() );
            }
        }

        dataSaver.close();
    }

    public void saveValues( List<BarChartValue> values ) {
        TabularDataSaver dataSaver = TabularDataFactory.newSaverForFile(file);
        for (BarChartValue value : values) {
            dataSaver.addRow( value.getName(), value.getValue());
        }
    }

    public String loadVector(List<Point2D> vector) {
        TabularDataLoader loader = TabularDataFactory.newLoaderForFile(file);
        String name = "";
        Object[] row;
        int index = 0;
        while ( null != (row = loader.getRow())) {
            if ( row.length > 0 ) {
                Object o = row[0];
                if ( o instanceof String ) {
                    name = (String) o;
                } else if ( o instanceof Double) {
                    vector.add(new Point2D(index, (Double)o));
                    index++;
                }
            }
        }
        return name;
    }

    public Function loadPoints() {
        List<Point2D> points = new ArrayList<Point2D>();
        List<Double> mask = new ArrayList<Double>();
        TabularDataLoader loader = TabularDataFactory.newLoaderForFile(file);
        Object[] row;
        boolean is3d = false;
        while ( null != (row = loader.getRow())) {
            double[] doubleRow = new double[3];
            int index = 0;
            for (Object o : row) {
                if ( o instanceof Double ) {
                    if ( index <= 2) {
                        doubleRow[index] = (double) o;
                        index++;
                    }
                }
            }
            if ( index > 2 ) {
                points.add( new Point2D(doubleRow[0], doubleRow[1]));
                mask.add(doubleRow[2]);
                is3d = true;
            } else {
                points.add( new Point2D(doubleRow[0], doubleRow[1]));
            }
        }
        if ( is3d ) {
            return new EmissionMaskImpl(points, mask);
        } else {
            return new DiscreteFunction(points);
        }
    }
}
