package org.seamcat.simulation;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.AntennaPatternConverter;
import org.seamcat.model.functions.Point2D;

import java.util.ArrayList;
import java.util.List;

public class PatternConversionTest {

	private DiscreteFunction function0To360Without180;
	private DiscreteFunction function0To360;
	private DiscreteFunction function180To180;

	@Before
	public void setup() {
		List<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D(   0.0, 0.0));
		points.add(new Point2D(  10.0, -0.182));
		points.add(new Point2D(  20.0, -0.364));
		points.add(new Point2D(  30.0, -1.37));
		points.add(new Point2D(  40.0, -2.73));
		points.add(new Point2D(  50.0, -3.82));
		points.add(new Point2D(  60.0, -5.27));
		points.add(new Point2D(  70.0, -7.18));
		points.add(new Point2D(  80.0, -9.36));
		points.add(new Point2D(  90.0, -11.36));
		points.add(new Point2D( 100.0, -13.73));
		points.add(new Point2D( 110.0, -15.55));
		points.add(new Point2D( 120.0, -17.36));
		points.add(new Point2D( 130.0, -18.64));
		points.add(new Point2D( 140.0, -20.364));
		points.add(new Point2D( 150.0, -23.0));
		points.add(new Point2D( 160.0, -24.27));
		points.add(new Point2D( 170.0, -23.18));
		points.add(new Point2D( 180.0, -23.18));
		points.add(new Point2D( 190.0, -23.18));
		points.add(new Point2D( 200.0, -24.27));
		points.add(new Point2D( 210.0, -23.0));
		points.add(new Point2D( 220.0, -20.364));
		points.add(new Point2D( 230.0, -18.64));
		points.add(new Point2D( 240.0, -17.36));
		points.add(new Point2D( 250.0, -15.55));
		points.add(new Point2D( 260.0, -13.73));
		points.add(new Point2D( 270.0, -11.36));
		points.add(new Point2D( 280.0, -9.36));
		points.add(new Point2D( 290.0, -7.18));
		points.add(new Point2D( 300.0, -5.27));
		points.add(new Point2D( 310.0, -3.82));
		points.add(new Point2D( 320.0, -2.73));
		points.add(new Point2D( 330.0, -1.37));
		points.add(new Point2D( 340.0, -0.364));
		points.add(new Point2D( 350.0, -0.182));
		points.add(new Point2D( 360.0, 0.0));
		function0To360 = new DiscreteFunction( points );

		points = new ArrayList<Point2D>();
		points.add(new Point2D(   0.0, 0.0));
		points.add(new Point2D(  10.0, -0.182));
		points.add(new Point2D(  20.0, -0.364));
		points.add(new Point2D(  30.0, -1.37));
		points.add(new Point2D(  40.0, -2.73));
		points.add(new Point2D(  50.0, -3.82));
		points.add(new Point2D(  60.0, -5.27));
		points.add(new Point2D(  70.0, -7.18));
		points.add(new Point2D(  80.0, -9.36));
		points.add(new Point2D(  90.0, -11.36));
		points.add(new Point2D( 100.0, -13.73));
		points.add(new Point2D( 110.0, -15.55));
		points.add(new Point2D( 120.0, -17.36));
		points.add(new Point2D( 130.0, -18.64));
		points.add(new Point2D( 140.0, -20.364));
		points.add(new Point2D( 150.0, -23.0));
		points.add(new Point2D( 160.0, -24.27));
		points.add(new Point2D( 170.0, 0.0));
		points.add(new Point2D( 190.0, -23.18));
		points.add(new Point2D( 200.0, -24.27));
		points.add(new Point2D( 210.0, -23.0));
		points.add(new Point2D( 220.0, -20.364));
		points.add(new Point2D( 230.0, -18.64));
		points.add(new Point2D( 240.0, -17.36));
		points.add(new Point2D( 250.0, -15.55));
		points.add(new Point2D( 260.0, -13.73));
		points.add(new Point2D( 270.0, -11.36));
		points.add(new Point2D( 280.0, -9.36));
		points.add(new Point2D( 290.0, -7.18));
		points.add(new Point2D( 300.0, -5.27));
		points.add(new Point2D( 310.0, -3.82));
		points.add(new Point2D( 320.0, -2.73));
		points.add(new Point2D( 330.0, -1.37));
		points.add(new Point2D( 340.0, -0.364));
		points.add(new Point2D( 350.0, -0.182));
		points.add(new Point2D( 360.0, 0.0));
		function0To360Without180 = new DiscreteFunction( points );

		
		points = new ArrayList<Point2D>();
		points.add(new Point2D(-180.0, -23.18));
		points.add(new Point2D(-170.0, -24.27));
		points.add(new Point2D(-160.0, -23.0));
		points.add(new Point2D(-150.0, -20.364));
		points.add(new Point2D(-140.0, -18.64));
		points.add(new Point2D(-130.0, -17.36));
		points.add(new Point2D(-120.0, -15.55));
		points.add(new Point2D(-110.0, -13.73));
		points.add(new Point2D(-100.0, -11.36));
		points.add(new Point2D( -90.0, -9.36));
		points.add(new Point2D( -80.0, -7.18));
		points.add(new Point2D( -70.0, -5.27));
		points.add(new Point2D( -60.0, -3.82));
		points.add(new Point2D( -50.0, -2.73));
		points.add(new Point2D( -40.0, -1.37));
		points.add(new Point2D( -30.0, -0.364));
		points.add(new Point2D( -20.0, -0.182));
		points.add(new Point2D( -10.0, 0.0));
		points.add(new Point2D(   0.0, 0.0));
		points.add(new Point2D(  10.0, 0.0));
		points.add(new Point2D(  20.0, -0.182));
		points.add(new Point2D(  30.0, -0.364));
		points.add(new Point2D(  40.0, -1.37));
		points.add(new Point2D(  50.0, -2.73));
		points.add(new Point2D(  60.0, -3.82));
		points.add(new Point2D(  70.0, -5.27));
		points.add(new Point2D(  80.0, -7.18));
		points.add(new Point2D(  90.0, -9.36));
		points.add(new Point2D( 100.0, -11.36));
		points.add(new Point2D( 110.0, -13.73));
		points.add(new Point2D( 120.0, -15.55));
		points.add(new Point2D( 130.0, -17.36));
		points.add(new Point2D( 140.0, -18.64));
		points.add(new Point2D( 150.0, -20.364));
		points.add(new Point2D( 160.0, -23.0));
		points.add(new Point2D( 170.0, -24.27));
		points.add(new Point2D( 180.0, -23.18));
		function180To180 = new DiscreteFunction( points );
	}

	@Test
	public void testConvertFrom0360to180180Without180() {
        DiscreteFunction converted = AntennaPatternConverter.convertFrom0360To180180new(function0To360Without180);
        List<Point2D> list = converted.points();
		Point2D point2d = list.get( list.size()-1 );

        for (Point2D point : converted.points()) {
            System.out.println(point);
        }

		Assert.assertEquals( 180.0, point2d.getX(), 0.01 );
	}
	
	
	private void listEquals( List<Point2D> a, List<Point2D> b ) {
		for (int index = 0; index<a.size(); index++ ) {
			Assert.assertEquals( "X in index "+index, a.get(index).getX(), b.get(index).getX() );
			Assert.assertEquals( "Y in index "+index, a.get(index).getY(), b.get(index).getY() );
		}
		
	}
	
}
